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
 *
 * @author Aleks
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

    /**
     * Task name from {@link TaskConfig#getTaskName()}
     * @return task name
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Timestamp of result creation
     * @return local data time
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Task execution status
     *
     * @see Status for more info
     * @return status
     */
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

    /**
     * Status of task execution
     */
    public enum Status {
        /**
         * Initial value for newly created task
         */
        NoStatusYet,
        /**
         * Task executed, result - success.
         * Should be set by {@link TaskExecutor}
         */
        Success,
        /**
         * Task executed, result - fail.
         * Should be set by {@link TaskExecutor}
         */
        Fail,
        /**
         * Exception during task execution
         */
        Exception,
        /**
         * Task failed to be executed within a given deadline.
         * @see TaskConfig#getDeadline()
         */
        DeadlineExceed
    }
}
