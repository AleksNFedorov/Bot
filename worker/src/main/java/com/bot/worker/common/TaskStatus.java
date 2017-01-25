package com.bot.worker.common;

/**
 * Flow for scheduling task:
 * <ul>
 *     <li>1. Scheduled</li>
 *     <li>2. Running/Hold</li>
 *     <li>3. Scheduled</li>
 * </ul>
 *
 * <p>
 * Flow for 'one time task' task
 * <ul>
 *     <li>1. Scheduled</li>
 *     <li>2. Running/Hold</li>
 *     <li>3. Finished</li>
 * </ul>
 *
 * @author Aleks
 */
//TODO rename to state
public enum TaskStatus {
  /**
   * When task is scheduled but not being executed yet
   */
  Scheduled,
  /**
   * Task put on hold
   */
  Hold,
  /**
   * Task is being executed by corresponding executor
   */
  Running,
  /**
   * Task executed, particular execution status in {@link com.bot.common.TaskResult}
   */
  Finished,
  /**
   * Reserved for exceptional case
   */
  Unknown,
}
