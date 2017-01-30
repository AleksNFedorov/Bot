package com.bot.common;

import java.util.Map;


//Aleks
//
//This is a test comment

/**
 * Task config. Contains key parameters to schedule and execute task
 *
 * <p>
 * <b>Important!</b> {@link #getDeadline()} must be strongly less than {@link #getRunInterval()}
 *
 * <p>
 * To make task as on time run task schedule interval must be equal to {@link #ONE_TIME_TASK}
 *
 * <p>
 * Use {@link #config} to specify executor specific parameters
 * {@code
 *  <executorConfig>
 *    <property key="key">value</property>
 *   </executorConfig>
 * }
 *
 * @see TaskExecutor
 */
public class TaskConfig {

  public static final long ONE_TIME_TASK = -1;

  protected String taskName;

  protected String executorId;

  protected long runInterval;

  protected long deadline = 30;

  protected Map<String, String> config;

  /**
   * Unique across all registred tasks name
   * @return task name
   */
  public String getTaskName() {
    return taskName;
  }

  /**
   * Executor id to call for execute task
   *
   * @see TaskExecutor#executeTask(TaskConfig)
   * @return executor id
   */
  public String getExecutorId() {
    return executorId;
  }

  /**
   * Interval in seconds to run a task, every {@link #getRunInterval()} seconds
   * @return run interval in seconds
   */
  public long getRunInterval() {
    return runInterval;
  }

  /**
   * Task run deadline, should be strongly less then {@link #getRunInterval()}
   * @return deadline interval in seconds
   */
  public long getDeadline() {
    return deadline;
  }

  /**
   * Method to check that current task is a one time task, should be run only once.
   * Possible to run multiple times with explicit call via CLI
   * @return true if this is one time task
   */
  public boolean isOneTimeTask() {
    return runInterval == ONE_TIME_TASK;
  }

}
