package com.bot.common;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.guard.Guarded;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Task execution result.
 *
 * @author Aleks
 */
@Guarded
public class TaskResult {

    @NotNull
    private final Status status;
    private final String message;
    @NotNull
    @NotEmpty
    private final String taskName;
    private final LocalDateTime timestamp;

    /**
     * Status of task execution
     */
    public enum Status {
        /**
         * Initial value for newly created task
         */
        NO_STATUS_YET,
        /**
         * Task executed, result - success.
         * Should be set by {@link TaskExecutor}
         */
        SUCCESS,
        /**
         * Task executed, result - fail.
         * Should be set by {@link TaskExecutor}
         */
        FAIL,
        /**
         * EXCEPTION during task execution
         */
        EXCEPTION,
        /**
         * Task failed to be executed within a given deadline.
         * @see TaskConfig#getDeadline()
         */
        DEADLINE_EXCEED
    }

    public TaskResult(String taskName, Status status) {
        this(taskName, status, null);
    }

    public TaskResult(String taskName, Status status, String message) {
        this.taskName = taskName;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
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
}
