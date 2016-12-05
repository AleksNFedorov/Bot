package com.bot.worker.taskmanager;

import com.bot.common.ITaskExecutor;
import com.bot.common.ITaskResultProcessor;
import com.bot.common.TaskConfig;
import com.bot.common.TaskResult;
import com.bot.worker.EventBusComponent;
import com.bot.worker.common.events.*;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Aleks on 11/19/16.
 */
public class TaskManager extends EventBusComponent {

    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private final ScheduledExecutorService executorService;

    private final Map<String, TaskGroup> idToGroup = new HashMap<>();
    private final Map<String, TaskContext> idToTask = new HashMap<>();
    private final Map<String, ITaskExecutor> executors;
    private final ITaskResultProcessor resultProcessor;

    @Inject
    public TaskManager(ScheduledExecutorService executorService,
                       ImmutableList<ITaskExecutor> executors,
                       ITaskResultProcessor resultProcessor
    ) {
        this.executorService = executorService;
        this.executors = executors.stream().collect(Collectors.toMap(ITaskExecutor::getId, x -> x));
        this.resultProcessor = resultProcessor;
    }

    @Subscribe
    private synchronized void onNewTaskConfig(TaskConfigLoaded newConfigEvent) {
        String taskGroup = newConfigEvent.getGroupName();
        final TaskConfig taskConfig = newConfigEvent.getTaskConfig();
        long scheduleInterval = taskConfig.getRunInterval();
        long deadline = taskConfig.getDeadline();
        final ITaskExecutor executor = executors.get(newConfigEvent.getExecutorId());

        ScheduledFuture<?> taskFuture = executorService.scheduleWithFixedDelay(() -> {
            logger.info("Executing idToGroup ...");
            TimeLimiter limiter = new SimpleTimeLimiter(executorService);
            TaskExecutionComplete.Builder completeEvent = new TaskExecutionComplete.Builder()
                    .setGroupName(taskGroup)
                    .setTaskName(taskConfig.getTaskName());
            TaskResult result;
            try {
                result = limiter.callWithTimeout(() -> {
                            return executor.executeTask(taskConfig);
                        },
                        deadline, TimeUnit.SECONDS, true);
            } catch (Exception e) {
                result = new TaskResult(TaskResult.Status.DeadlineExceed, "Deadline exceed");
            }
            post(completeEvent.setTaskResult(result).build());
        }, 0, scheduleInterval, TimeUnit.SECONDS);

        addTaskToGroup(taskGroup, taskConfig, taskFuture);
    }

    private void addTaskToGroup(String groupName, TaskConfig config, ScheduledFuture future) {
        idToGroup.putIfAbsent(groupName, new TaskGroup(groupName));
        TaskGroup group = idToGroup.get(groupName);
        TaskContext taskContext = new TaskContext(config, future);
        group.addTask(taskContext);
        idToTask.put(config.getTaskName(), taskContext);
    }

    @Subscribe
    private synchronized void onTaskExecutionComplete(TaskExecutionComplete completeEvent) {
        TaskResult result = completeEvent.getTaskResult();
        TaskGroup taskGroup = idToGroup.get(completeEvent.getGroupName());
        taskGroup.addTaskResult(completeEvent.getTaskName(), result);

        resultProcessor.processResult(result, taskGroup);
    }

    @Subscribe
    private synchronized void onTaskUpdateEvent(TaskUpdateEvent event) {
        post(new TaskUpdateResultEvent.Builder()
                .setTaskName(event.getTaskName())
                .setCommand(event.getCommand())
                .setResultMessage("Processed" + event.getCommand() + event.getTaskName())
                .build());
    }

    @Subscribe
    private synchronized void onGetStatusRequest(GetStatusRequest request) {
        List<TaskContext> tasks = new ArrayList<>();
        if (request.getTaskName().isPresent()) {
            TaskContext task = idToTask.get(request.getTaskName().get());
            if (task != null) {
                tasks.add(task);
            }
        } else {
            tasks.addAll(idToTask.values());
        }

        post(new GetStatusResponse.Builder()
                .addAllTaskInfos(tasks.stream().map((context) ->
                        new GetStatusResponse.TaskInfo.Builder()
                                .setTaskName(context.getTaskName())
                                .setStatus(context.getStatus())
                                .build()
                ).collect(Collectors.toList()))
                .build());
    }

    @Override
    public String getComponentName() {
        return "TaskManager";
    }

}
