package com.bot.worker.taskmanager;

import com.bot.common.TaskConfig;
import com.bot.common.TaskResult;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by Aleks on 11/20/16.
 */
class TaskContext {
    private final TaskConfig config;
    private final ScheduledFuture<Void> future;

    private TaskResult lastTaskResult;

    TaskContext(TaskConfig config, ScheduledFuture<Void> future) {
        this.config = config;
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
}
