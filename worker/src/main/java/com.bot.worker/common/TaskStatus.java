package com.bot.worker.common;

/**
 * Flow for scheduling task: Scheduled -> Running/Hold -> Scheduled
 * Flow for 'one time task' task: Scheduled -> Running/Hold -> Finished
 *
 * @author Aleks
 */
//TODO rename to status
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
