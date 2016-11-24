package com.bot.common;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.guard.Guarded;

/**
 * Task execution result.
 */
@Guarded
public class TaskResult {

    @NotNull
    private final Status status;

    @NotEmpty
    private final String message;

    private final long timestamp;

    public TaskResult(Status status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
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
