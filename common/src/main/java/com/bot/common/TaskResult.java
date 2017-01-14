package com.bot.common;


import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Task execution result.
 */
public class TaskResult {

    private final Status status;
    private final String message;
    private final String taskName;
    private final LocalDateTime timestamp;

    public TaskResult(String taskName, Status status) {
        this(taskName, status, null);
    }

    public TaskResult(String taskName, Status status, String message) {
        checkArgument(!Strings.isNullOrEmpty(taskName));
        this.status = Preconditions.checkNotNull(status);
        this.message = message;
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
        return Optional.ofNullable(message);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TaskResult result = (TaskResult) o;

        return Objects.equal(taskName, result.taskName)
                && Objects.equal(status, result.status)
                && Objects.equal(message, result.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.taskName, this.status, this.message);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("task", taskName)
                .add("status", status)
                .add("message", message)
                .add("time", timestamp)
                .toString();
    }

    public enum Status {
        NoStatusYet,
        Success,
        Fail,
        Exception,
        DeadlineExceed
    }
}
