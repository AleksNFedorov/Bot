package com.bot.common;


import com.google.common.base.Optional;

/**
 * Task execution result.
 */
public class TaskResult {

    private final Status status;

    private final Optional<String> message;

    private final long timestamp;

    public TaskResult(Status status) {
        this(status, null);
    }

    public TaskResult(Status status, String message) {
        this.status = status;
        this.message = message == null ? Optional.<String>absent() : Optional.of(message);
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public Optional<String> getMessage() {
        return message;
    }

    public enum Status {
        Success,
        Fail,
        Exception,
        DeadlineExceed
    }
}
