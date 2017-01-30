package com.bot.worker.taskmanager;

import com.bot.common.TaskConfig;
import com.bot.common.TaskExecutor;
import com.bot.common.TaskResult;
import com.bot.common.TaskResultProcessor;
import com.bot.worker.EventBusComponent;
import com.bot.worker.common.Constants;
import com.bot.worker.common.TaskStatus;
import com.bot.worker.common.events.GetStatusRequest;
import com.bot.worker.common.events.GetStatusResponse;
import com.bot.worker.common.events.TaskConfigLoadedResponse;
import com.bot.worker.common.events.TaskDropRequest;
import com.bot.worker.common.events.TaskHoldRequest;
import com.bot.worker.common.events.TaskScheduleRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master class to manage and execute tasks.
 * For more info see methods annotated with {@code @Subscribe}
 *
 * <p>
 *  Supported operations
 *  <ul>
 *      <li>Schedule new task</li>
 *      <li>Execute task with particular executor</li>
 *      <li>Drop task</li>
 *      <li>Put task on hold</li>
 *      <li>Provide task(s) details</li>
 *  </ul>
 *
 * <p>
 *  All communication with other app components goes though event bus,
 *  look {@link EventBusComponent} for more info
 *
 *  @see com.bot.worker.cli.Command
 *  @see com.bot.worker.config.ConfigLoader
 *  @see com.bot.worker.cli.CliProcessor
 *
 * @author Aleks
 */
public class TaskManager extends EventBusComponent {

  private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

  private final ScheduledExecutorService executorService;
  private final ExecutorService taskExecutor;

  private final Map<String, TaskContext> idToTask = new HashMap<>();
  private final Multimap<String, TaskResult> groupResults = TreeMultimap.create(
      Ordering.natural(),
      Comparator.comparing(TaskResult::getTaskName));

  private final Map<String, TaskExecutor> taskExecutors;
  private final TaskResultProcessor resultProcessor;

  @Inject
  TaskManager(ScheduledExecutorService executorService,
      ImmutableList<TaskExecutor> executors,
      ImmutableList<TaskResultProcessor> resultProcessors) {

    this.executorService = executorService;
    this.taskExecutor = Executors.newCachedThreadPool();
    this.taskExecutors = executors
        .stream()
        .collect(Collectors.toMap(TaskExecutor::getId, x -> x));
    this.resultProcessor = new TaskResultProcessorDecorator(resultProcessors);
  }

  /**
   * Schedule new task config for execution
   * @param newConfigEvent
   */
  @Subscribe
  synchronized void onNewTaskConfig(TaskConfigLoadedResponse newConfigEvent) {
    String taskGroup = newConfigEvent.getGroupName();
    TaskConfig taskConfig = newConfigEvent.getTaskConfig();
    TaskContext taskContext = saveTaskContext(taskGroup, taskConfig);
    scheduleTask(taskContext);
  }

  private void scheduleTask(TaskContext taskContext) {

    final TaskConfig taskConfig = taskContext.getConfig();
    Runnable runnable = createTaskRunnable(taskContext);
    Future<?> taskFuture;
    if (taskConfig.isOneTimeTask()) {
      taskFuture = executorService.submit(runnable);
    } else {
      taskFuture = executorService.scheduleWithFixedDelay(
          runnable, 0, taskConfig.getRunInterval(), TimeUnit.SECONDS);
    }
    taskContext.setFuture(taskFuture);
  }

  private Runnable createTaskRunnable(TaskContext taskContext) {

    final TaskConfig taskConfig = taskContext.getConfig();
    final TaskExecutor executor = taskExecutors.get(taskConfig.getExecutorId());
    long deadline = taskConfig.getDeadline();

    return () -> {
      logger.info("Running {}", taskConfig.getTaskName());
      TimeLimiter limiter = new SimpleTimeLimiter(taskExecutor);
      TaskResult result;
      try {
        taskContext.setStatus(TaskStatus.Running);
        result = limiter.callWithTimeout(
            () -> executor.executeTask(taskConfig),
            deadline,
            TimeUnit.SECONDS,
            true);
      } catch (Exception e) {
        TaskResult.Status status = e instanceof UncheckedTimeoutException ?
            TaskResult.Status.DeadlineExceed : TaskResult.Status.Exception;

        result = new TaskResult(
            taskConfig.getTaskName(),
            status,
            e.getLocalizedMessage());
      }

      processExecutionResult(taskContext, result);
    };
  }

  private synchronized void processExecutionResult(TaskContext context,
      TaskResult result) {
    final String groupName = context.getGroupName();
    groupResults.remove(groupName, result);
    groupResults.put(groupName, result);

    TaskStatus newStatus = getNewTaskStatus(context);

    context.setStatus(newStatus);
    context.setLastTaskResult(result);

    List<TaskResult> filteredGroupResults =
        Constants.NO_GROUP.equals(groupName) ? ImmutableList.of() :
            groupResults.get(groupName)
                .stream()
                .filter(r -> (r != result))
                .collect(Collectors.toList());

    resultProcessor.processResult(result, filteredGroupResults);
  }

  private TaskContext saveTaskContext(String groupName, TaskConfig config) {
    idToTask.putIfAbsent(config.getTaskName(), new TaskContext(config,
        groupName));
    return idToTask.get(config.getTaskName());
  }

  /**
   * Stops task execution and remove all information about task
   *
   * @param event {@link TaskDropRequest}
   */
  @Subscribe
  synchronized void onTaskDropEvent(TaskDropRequest event) {
    List<TaskContext> tasks = getTasksById(event.getTaskName());
    tasks.forEach(context -> {
      context.putOnHold();
      idToTask.remove(context.getTaskName());
      //Clean group results from task`s result
      List<TaskResult> cleanedResults = groupResults.get(context.getGroupName())
          .stream()
          .filter((r) -> !r.getTaskName().equals(context.getTaskName()))
          .collect(Collectors.toList());
      groupResults.replaceValues(context.getGroupName(), cleanedResults);
    });
    onGetStatusRequest(GetStatusRequest.create(event.getTaskName()));
  }

  /**
   * Tries to re-schedule task. Task should have either Hold of Finished status
   *
   * @param event {@link TaskScheduleRequest}
   */
  @Subscribe
  synchronized void onTaskScheduleEvent(TaskScheduleRequest event) {
    getTasksById(event.getTaskName())
        .stream()
        .filter(k ->
            k.getStatus().equals(TaskStatus.Hold) ||
                k.getStatus().equals(TaskStatus.Finished))
        .forEach(this::scheduleTask);

    onGetStatusRequest(GetStatusRequest.create(event.getTaskName()));
  }

  /**
   * Puts task on hold, {@link TaskStatus} should be either Running or Scheduled
   *
   * @param event {@link TaskHoldRequest}
   */
  @Subscribe
  synchronized void onTaskHoldEvent(TaskHoldRequest event) {
    getTasksById(event.getTaskName())
        .stream()
        .filter(k ->
            (k.getStatus().equals(TaskStatus.Running) ||
                k.getStatus().equals(TaskStatus.Scheduled)))
        .forEach(TaskContext::putOnHold);

    onGetStatusRequest(GetStatusRequest.create(event.getTaskName()));
  }

  /**
   * Posts information about requested task
   *
   * @param request {@link GetStatusRequest}
   */
  @Subscribe
  synchronized void onGetStatusRequest(GetStatusRequest request) {
    post(new GetStatusResponse.Builder()
        .addAllTasksInfo(getTasksById(request.getTaskName())
            .stream()
            .map((context) ->
                new GetStatusResponse.TaskInfo.Builder()
                    .setTaskName(context.getTaskName())
                    .setStatus(context.getStatus())
                    .setResultStatus(context.getLastTaskResult()
                        .getStatus())
                    .setResultTimestamp(context.getLastTaskResult
                        ().getTimestamp())
                    .build())
            .collect(Collectors.toList()))
        .build());
  }

  private ImmutableList<TaskContext> getTasksById(String taskId) {
    ImmutableList.Builder<TaskContext> tasks = ImmutableList.builder();
    if (Constants.ALL.equals(taskId)) {
      tasks = tasks.addAll(idToTask.values());
    } else {
      TaskContext task = idToTask.get(taskId);
      if (task != null) {
        tasks = tasks.add(task);
      }
    }

    return tasks.build();
  }

  private static TaskStatus getNewTaskStatus(TaskContext context) {
    TaskStatus newStatus = TaskStatus.Unknown;
    switch (context.getStatus()) {
      case Running:
        newStatus = context.getConfig().isOneTimeTask() ?
                TaskStatus.Finished : TaskStatus.Scheduled;
        break;
      case Hold:
        newStatus = TaskStatus.Hold;
        break;
    }
    return newStatus;
  }
}