package com.bot.common;


import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Task execution result.
 */
public class TaskResult {

    private final Status status;

    private final Optional<String> message;

    private final String taskName;

    private final LocalDateTime timestamp;

    public TaskResult(String taskName, Status status) {
        this(taskName, status, null);
    }

    public TaskResult(String taskName, Status status, String message) {
        checkArgument(!Strings.isNullOrEmpty(taskName));
        this.status = Preconditions.checkNotNull(status);
        this.message = Optional.fromNullable(message);
        this.timestamp = LocalDateTime.now();
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public Optional<String> getMessage() {
        return message;
    }

    public enum Status {
        NoStatusYet,
        Success,
        Fail,
        Exception,
        DeadlineExceed
    }
}
