package com.bot.common;

import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.guard.Guarded;

import java.util.Map;

/**
 * Task config.
 * <p>
 * {@link #getDeadline()} must be strongly less than {@link #getRunInterval()}
 * All time properties should be specified in SECOND time unit.
 * <p>
 * To make task as on time run task schedule interval must be eqal to {@link #ONE_TIME_TASK}
 */
@Guarded
public class TaskConfig {

    public static final long ONE_TIME_TASK = -1;

    @NotEmpty
    protected String taskName;

    @NotEmpty
    protected String executorId;

    @Min(1)
    protected long runInterval;

    @Min(1)
    protected long deadline = 30;

    protected Map<String, String> config;

    public String getTaskName() {
        return taskName;
    }

    public String getExecutorId() {
        return executorId;
    }

    public long getRunInterval() {
        return runInterval;
    }

    public long getDeadline() {
        return deadline;
    }

    public boolean isOneTimeTask() {
        return runInterval == ONE_TIME_TASK;
    }

}
