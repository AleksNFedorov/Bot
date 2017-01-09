package com.bot.worker.taskmanager;

import com.bot.common.TaskConfig;
import com.bot.common.TaskResult;
import com.bot.worker.common.TaskStatus;

import java.util.concurrent.Future;

/**
 * Created by Aleks on 11/20/16.
 */
//TODO change visibility
class TaskContext {

    private final TaskConfig config;

    private final String groupName;

    private Future<?> future;

    private TaskResult lastTaskResult;

    private TaskStatus status = TaskStatus.Scheduled;

    public TaskContext(TaskConfig config, String groupName) {
        this.config = config;
        this.groupName = groupName;
        setLastTaskResult(new TaskResult(config.getTaskName(), TaskResult
                .Status.NoStatusYet));
    }

    public String getGroupName() {
        return groupName;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    String getTaskName() {
        return config.getTaskName();
    }

    TaskResult getLastTaskResult() {
        return lastTaskResult;
    }

    void setLastTaskResult(TaskResult lastTaskResult) {
        this.lastTaskResult = lastTaskResult;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    //TODO consdier to simplify
    void putOnHold() {
        boolean success = future.cancel(true);
        if (config.isOneTimeTask() && success) {
            setStatus(TaskStatus.Hold);
        } else if (!config.isOneTimeTask()) {
            setStatus(TaskStatus.Hold);
        }
    }

    public TaskConfig getConfig() {
        return config;
    }
}
