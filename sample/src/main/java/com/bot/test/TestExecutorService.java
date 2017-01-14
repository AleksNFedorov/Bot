package com.bot.test;

import com.bot.common.TaskConfig;
import com.bot.common.TaskExecutor;
import com.bot.common.TaskResult;
import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Aleks on 11/20/16.
 */
@AutoService(TaskExecutor.class)
public class TestExecutorService implements TaskExecutor {

  private static final Logger logger = LoggerFactory.getLogger(TestExecutorService.class);

  public String getId() {
    return "testExecutor";
  }

  public TaskResult executeTask(TaskConfig config) {
    logger.info("Executing task in " + getId());
    return new TaskResult(config.getTaskName(), TaskResult.Status.Success, "Test execution " +
        "success");

  }
}
