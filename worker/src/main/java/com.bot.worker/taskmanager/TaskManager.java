package com.bot.worker.taskmanager;

import com.bot.common.ITaskExecutor;
import com.bot.common.TaskConfig;
import com.bot.common.TaskResult;
import com.bot.common.TaskResultProcessor;
import com.bot.worker.EventBusComponent;
import com.bot.worker.common.events.TaskConfigLoaded;
import com.bot.worker.common.events.TaskExecutionComplete;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aleks on 11/19/16.
 */
public class TaskManager extends EventBusComponent {

    private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);

    private final ScheduledExecutorService executorService;

    private final Map<String, TaskGroup> tasks = new HashMap<>();
    private final Map<String, ITaskExecutor> executors;
    private final failAlerter

    @Inject
    public TaskManager(ScheduledExecutorService executorService,
                       Map<String, ITaskExecutor> executors,
                       TaskResultProcessor failAlerter
    ) {
        this.executorService = executorService;
        this.executors = executors;
        this.failAlerter = failAlerter;
    }

    @Subscribe
    private void onNewTaskConfig(TaskConfigLoaded newConfigEvent) {
        String taskGroup = newConfigEvent.getGroupName();
        final TaskConfig taskConfig = newConfigEvent.getTaskConfig();
        long scheduleInterval = taskConfig.getRunInterval();
        long deadline = taskConfig.getDeadline();
        final ITaskExecutor executor = executors.get(newConfigEvent.getExecutorId());

        ScheduledFuture<?> taskFuture = executorService.schedule(() -> {
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
        }, scheduleInterval, TimeUnit.SECONDS);

        addTaskToGroup(taskGroup, taskConfig, taskFuture);
    }

    private synchronized void addTaskToGroup(String groupName, TaskConfig config, ScheduledFuture future) {
        TaskGroup group = tasks.putIfAbsent(groupName, new TaskGroup(groupName));
        group.addTask(new TaskContext(config, future));
    }

    @Subscribe
    private void onTaskExecutionComplete(TaskExecutionComplete completeEvent) {
        TaskResult result = completeEvent.getTaskResult();
        TaskGroup taskGroup = tasks.get(completeEvent.getGroupName());
        taskGroup.addTaskResult(completeEvent.getTaskName(), result);

        if (failAlerter.isTaskFailed(result)) {
            failAlerter.processResult(result, taskGroup);
        }
    }

    @Override
    public String getComponentName() {
        return "TaskManager";
    }

}
