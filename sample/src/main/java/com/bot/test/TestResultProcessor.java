package com.bot.test;

import com.bot.common.TaskResult;
import com.bot.common.TaskResultProcessor;
import com.google.auto.service.AutoService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample task results processor.
 * Just logs results
 */
@AutoService(TaskResultProcessor.class)
public class TestResultProcessor implements TaskResultProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(TestResultProcessor.class);

  public void processResult(TaskResult result, List<TaskResult> groupResults) {
    LOG.info("Processing result [{}], group tasks [{}]", result, groupResults.size());
  }
}
