package com.bot.worker.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Created by Aleks on 11/14/16.
 */
class BootOptions extends Options {

  static final String THREADS_COUNT = "threads-count";
  static final String TASK_CONFIG_FILE_PATH = "task-config-file";

  BootOptions() {
    addOption(Option
        .builder("tc")
        .longOpt(THREADS_COUNT)
        .desc("Task execution thread pool size")
        .hasArg()
        .type(Integer.class)
        .build());

    addOption(Option
        .builder("config")
        .longOpt(TASK_CONFIG_FILE_PATH)
        .desc("Path to configuration file")
        .hasArg()
        .type(String.class)
        .build());
  }
}
