package com.bot.common;

/**
 * Interface for task executors implementations.
 * <p>
 * Every instance must be stateless to be ready accept concurrent execution calls.
 *
 * <p>
 * <b>Important!</b>It is implementation responsibility to execute task within a given deadline or throw exception.
 *
 * <p>
 * <b>Important</b> {@link #executeTask(TaskConfig)} method should be responsive on Thread interruption calls
 *
 */
public interface TaskExecutor {

  /**
   * Executor unique identifier.
   * Must be unique among all executors inside one running Bot app
   *
   * @return string executor id;
   */
  String getId();

  /**
   * Method to execute task specified by config. Task must be executed within a deadline form
   *  {@link TaskConfig#getDeadline()}
   *
   * @param config task config
   * @return task execution result
   */
  TaskResult executeTask(TaskConfig config);

}
