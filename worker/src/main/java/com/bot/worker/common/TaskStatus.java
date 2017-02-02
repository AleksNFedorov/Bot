package com.bot.worker.common;

/**
 * Flow for scheduling task:
 * <ul>
 *     <li>1. SCHEDULED</li>
 *     <li>2. RUNNING/HOLD</li>
 *     <li>3. SCHEDULED</li>
 * </ul>
 *
 * <p>
 * Flow for 'one time task' task
 * <ul>
 *     <li>1. SCHEDULED</li>
 *     <li>2. RUNNING/HOLD</li>
 *     <li>3. FINISHED</li>
 * </ul>
 *
 * @author Aleks
 */
//TODO rename to state
public enum TaskStatus {
  /**
   * When task is scheduled but not yet executed
   */
  SCHEDULED,
  /**
   * Task put on hold
   */
  HOLD,
  /**
   * Task is being executed by corresponding executor
   */
  RUNNING,
  /**
   * Task executed, particular execution STATUS in {@link com.bot.common.TaskResult}
   */
  FINISHED,
  /**
   * Reserved for exceptional case
   */
  UNKNOWN,
}
