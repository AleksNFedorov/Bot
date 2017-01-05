package com.bot.worker.taskmanager;

import com.bot.common.ITaskResultProcessor;
import com.bot.common.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by Aleks on 11/23/16.
 */
//TODO consider to convert into nested class
public class TaskResultLogger implements ITaskResultProcessor {

    private static final Logger logger = LoggerFactory.getLogger
            (TaskResultLogger.class);

    @Override
    public void processResult(TaskResult result, List<TaskResult> group) {
        logger.info("Result processed [{}]", result);
    }
}
