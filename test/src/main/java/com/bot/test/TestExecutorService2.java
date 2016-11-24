package com.bot.test;

import com.bot.common.ITaskExecutor;
import com.bot.common.TaskConfig;
import com.bot.common.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Aleks on 11/23/16.
 */
public class TestExecutorService2 implements ITaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TestExecutorService2.class);

    public String getId() {
        return "TaskExeutorService2";
    }

    public TaskResult executeTask(TaskConfig config) {
        logger.info("Executing task in TaskExecutor 2");
        return new TaskResult(TaskResult.Status.Success, "Test execution success");
    }
}
