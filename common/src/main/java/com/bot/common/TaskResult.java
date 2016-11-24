package com.bot.common;

/**
 * Task execution result.
 */
public class TaskResult {

    private final Status status;
    private final String message;
    private final long timestamp;

    public TaskResult(Status status, String message) {
        checkParameters(status, message);
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    private void checkParameters(Status status, String message) {
        if (status == null) {
            throw new NullPointerException("Status is null");
        }

        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Result message null or empty");
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public enum Status {
        Success,
        Fail,
        Exception,
        DeadlineExceed
    }
}
