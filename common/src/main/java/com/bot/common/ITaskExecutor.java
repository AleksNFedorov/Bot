package com.bot.common;

/**
 * General interface for task Executors.
 * Every instance must be stateless to be ready accept concurrent execution call.
 * <p>
 * Parametrized with custom config to allow utilize executor specific fields in config.
 */
public interface ITaskExecutor<C extends TaskConfig> {

    /**
     * Executor unique identifier.
     * Must be unique among all executors inside one running Bot app
     *
     * @return string executor id;
     */
    String getId();

    /**
     * Method to execute task specified by config.
     * Task must be executed within a deadline specified in {@link TaskConfig#getDeadline()}
     * If unable to execute task within given deadline, TaskResult with fail status should be returned.
     *
     * @param config task config
     * @return task execution result
     */
    TaskResult executeTask(TaskConfig config);

}
