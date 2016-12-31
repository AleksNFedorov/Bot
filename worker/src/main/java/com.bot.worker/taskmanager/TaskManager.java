package com.bot.worker.taskmanager;

import com.bot.common.ITaskExecutor;
import com.bot.common.ITaskResultProcessor;
import com.bot.common.TaskConfig;
import com.bot.common.TaskResult;
import com.bot.worker.EventBusComponent;
import com.bot.worker.common.TaskStatus;
import com.bot.worker.common.events.*;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Aleks on 11/19/16.
 */
public class TaskManager extends EventBusComponent {

    private static final Logger logger = LoggerFactory.getLogger(TaskManager
            .class);

    private final ScheduledExecutorService executorService;

    private final Map<String, TaskContext> idToTask = new HashMap<>();
    private final Multimap<String, TaskResult> groupResults = TreeMultimap
            .create(
            Ordering.natural(),
            Comparator.comparing(TaskResult::getTaskName));
    private final Map<String, ITaskExecutor> executors;
    private final ITaskResultProcessor resultProcessor;

    @Inject
    public TaskManager(ScheduledExecutorService executorService,
                       ImmutableList<ITaskExecutor> executors,
                       ITaskResultProcessor resultProcessor
    ) {
        this.executorService = executorService;
        this.executors = executors.stream().collect(Collectors.toMap
                (ITaskExecutor::getId, x -> x));
        this.resultProcessor = resultProcessor;
    }

    @Subscribe
    synchronized void onNewTaskConfig(TaskConfigLoadedResponse
                                                          newConfigEvent) {
        String taskGroup = newConfigEvent.getGroupName();
        final TaskConfig taskConfig = newConfigEvent.getTaskConfig();
        final TaskContext taskContext = createTaskContent(taskGroup,
                taskConfig);
        scheduleTask(taskContext);
    }

    private void scheduleTask(TaskContext taskContext) {
        final TaskConfig taskConfig = taskContext.getConfig();
        final ITaskExecutor executor = executors.get(taskConfig.getExecutorId
                ());
        Runnable runnable = createTaskRunnable(executor, taskContext
                .getGroupName(), taskContext);
        Future<?> taskFuture;
        if (taskConfig.isOneTimeTask()) {
            taskFuture = executorService.submit(runnable);
        } else {
            taskFuture = executorService.scheduleWithFixedDelay(runnable, 0,
                    taskConfig
                    .getRunInterval(), TimeUnit.SECONDS);
        }

        taskContext.setFuture(taskFuture);
    }

    private Runnable createTaskRunnable(ITaskExecutor executor, String
            taskGroup, TaskContext
            taskContext) {

        final TaskConfig taskConfig = taskContext.getConfig();
        long deadline = taskConfig.getDeadline();

        return () -> {
            logger.info("Running {}", taskConfig.getTaskName());
            TimeLimiter limiter = new SimpleTimeLimiter(executorService);
            TaskResult result;
            try {
                taskContext.setStatus(TaskStatus.Running);
                result = limiter.callWithTimeout(() -> {
                            return executor.executeTask(taskConfig);
                        },
                        deadline, TimeUnit.SECONDS, true);
            } catch (Exception e) {
                result = new TaskResult(taskConfig.getTaskName(), TaskResult
                        .Status.Exception, e
                        .getLocalizedMessage());
                post(new ExecutionExceptionEvent.Builder().setCause(e)
                        .setComponentName(this
                        .getComponentName()).build());
            }
            post(new TaskExecutionComplete.Builder()
                    .setGroupName(taskGroup)
                    .setTaskName(taskConfig.getTaskName())
                    .setTaskResult(result).build());
        };

    }

    private TaskContext createTaskContent(String groupName, TaskConfig config) {
        idToTask.putIfAbsent(config.getTaskName(), new TaskContext(config,
                groupName));
        TaskContext taskContext = idToTask.get(config.getTaskName());
        return taskContext;
    }

    @Subscribe
    private synchronized void onTaskExecutionComplete(TaskExecutionComplete
                                                              completeEvent) {
        TaskResult result = completeEvent.getTaskResult();
        TaskContext context = idToTask.get(completeEvent.getTaskName());
        groupResults.remove(completeEvent.getGroupName(), result);
        groupResults.put(completeEvent.getGroupName(), result);

        TaskStatus newStatus = TaskStatus.Unknown;
        switch (context.getStatus()) {
            case Running:
                newStatus = context.getConfig().isOneTimeTask() ? TaskStatus
                        .Finished :
                        TaskStatus.Scheduled;
                break;
            case Hold:
                newStatus = TaskStatus.Hold;
                break;

        }

        context.setStatus(newStatus);
        context.setLastTaskResult(result);

        resultProcessor.processResult(result, groupResults.get(completeEvent
                .getGroupName())
                .stream().filter(r -> r != result).collect(Collectors.toList
                        ()));
    }

    @Subscribe
    private synchronized void onTaskDropEvent(TaskDropRequest event) {
        List<TaskContext> tasks = getTasksById(event.getTaskName());
        tasks.forEach(context -> {
            context.putOnHold();
            idToTask.remove(context.getTaskName());
            List<TaskResult> cleanedResults = groupResults.get(context
                    .getGroupName()).stream()
                    .filter((r) -> !r
                            .getTaskName()
                            .equals(context.getTaskName())).collect
                            (Collectors.toList());
            groupResults.replaceValues(context.getGroupName(), cleanedResults);
        });
        onGetStatusRequest(new GetStatusRequest.Builder().setNullableTaskName
                (event.getTaskName()
                .orNull()).build());
    }

    @Subscribe
    private synchronized void onTaskScheduleEvent(TaskScheduleRequest event) {
        List<TaskContext> tasks = getTasksById(event.getTaskName());
        tasks.stream().filter(k -> k.getStatus().equals(TaskStatus.Hold))
                .forEach
                (this::scheduleTask);
        onGetStatusRequest(new GetStatusRequest.Builder().setNullableTaskName
                (event.getTaskName()
                .orNull()).build());
    }

    @Subscribe
    private synchronized void onTaskHoldEvent(TaskHoldRequest event) {
        List<TaskContext> tasks = getTasksById(event.getTaskName());
        tasks.stream().filter(k -> (
                k.getStatus().equals(TaskStatus.Running) ||
                        k.getStatus().equals(TaskStatus.Scheduled))).forEach
                (TaskContext::putOnHold);
        onGetStatusRequest(new GetStatusRequest.Builder().setNullableTaskName
                (event.getTaskName()
                .orNull()).build());

    }

    @Subscribe
    private synchronized void onGetStatusRequest(GetStatusRequest request) {
        List<TaskContext> tasks = getTasksById(request.getTaskName());

        post(new GetStatusResponse.Builder()
                .addAllTasksInfo(tasks.stream().map((context) ->
                        new GetStatusResponse.TaskInfo.Builder()
                                .setTaskName(context.getTaskName())
                                .setStatus(context.getStatus())
                                .setResultStatus(context.getLastTaskResult()
                                        .getStatus())
                                .setResultTimestamp(context.getLastTaskResult
                                        ().getTimestamp())
                                .build()
                ).collect(Collectors.toList()))
                .build());
    }

    private ImmutableList<TaskContext> getTasksById(Optional<String> taskId) {
        ImmutableList.Builder<TaskContext> tasks = ImmutableList.builder();
        if (taskId.isPresent()) {
            TaskContext task = idToTask.get(taskId.get());
            if (task != null) {
                tasks = tasks.add(task);
            }
        } else {
            tasks = tasks.addAll(idToTask.values());
        }

        return tasks.build();
    }

    @Override
    public String getComponentName() {
        return "TaskManager";
    }

}
