package com.bot.common;

import java.util.List;

/**
 * Processes task result.
 * Intended for custom result reporting implementation.
 *
 * <p>
 *  Call to {@link #processResult(TaskResult, List)} should be non-blocking to avoid application performance degradation.
 *  In case of network/long-running calls consider using {@link Thread}
 */
public interface TaskResultProcessor {

  /**
   * Callback method to process task execution result.
   * If task belongs to group - second parameter contains results of other tasks from this group, otherwise it is an empty list
   *
   * @param result - task execution result
   * @param groupResults - task results from the same group
   */
  void processResult(TaskResult result, List<TaskResult> groupResults);

}
