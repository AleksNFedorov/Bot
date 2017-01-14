package com.bot.common;

import java.util.Map;

/**
 * Task config.
 * <p>
 * {@link #getDeadline()} must be strongly less than {@link #getRunInterval()}
 * All time properties should be specified in SECOND time unit.
 * <p>
 * To make task as on time run task schedule interval must be eqal to {@link #ONE_TIME_TASK}
 */
public class TaskConfig {

  public static final long ONE_TIME_TASK = -1;

  protected String taskName;

  protected String executorId;

  protected long runInterval;

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
