package com.bot.worker.taskmanager;

import com.bot.common.ITaskGroup;
import com.bot.common.ITaskResultProcessor;
import com.bot.common.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Aleks on 11/23/16.
 */
public class TaskResultLogger implements ITaskResultProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TaskResultLogger.class);

    @Override
    public void processResult(TaskResult result, ITaskGroup group) {
        logger.info("Result processed [{}]", result);
    }
}
