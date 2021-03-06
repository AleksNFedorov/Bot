package com.bot.test;

import com.bot.common.TaskConfig;
import com.bot.common.TaskExecutor;
import com.bot.common.TaskResult;
import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample task executor.
 * Creates success result for every execution call
 */
@AutoService(TaskExecutor.class)
public class TestExecutorService2 implements TaskExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(TestExecutorService2.class);

  public String getId() {
    return "executor2";
  }

  public TaskResult executeTask(TaskConfig config) {
    LOG.info("Executing task in " + getId());
    return new TaskResult(config.getTaskName(), TaskResult.Status.SUCCESS,
            "Test execution success");
  }
}
