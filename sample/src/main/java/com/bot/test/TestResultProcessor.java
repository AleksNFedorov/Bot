package com.bot.test;

import com.bot.common.TaskResult;
import com.bot.common.TaskResultProcessor;
import com.google.auto.service.AutoService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Aleks on 1/12/17.
 */
@AutoService(TaskResultProcessor.class)
public class TestResultProcessor implements TaskResultProcessor {

  private static final Logger logger = LoggerFactory.getLogger(TestResultProcessor.class);

  public void processResult(TaskResult result, List<TaskResult> groupResults) {
    logger.info("Processing result [{}], group tasks [{}]", result, groupResults.size());
  }
}
