package com.bot.worker.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Created by Aleks on 11/14/16.
 */
public class BootOptions extends Options {

    static final String THREADS_COUNT = "threads-count";
    static final String TASK_CONFIG_FILE_PATH = "task-config-file";

    public BootOptions() {
        Option threadsCount = new Option("tc", THREADS_COUNT, false, "Threads count to execute task");
        threadsCount.setType(Integer.class);
        addOption(threadsCount);
        addOption(new Option("file", TASK_CONFIG_FILE_PATH, true, "Path to file with tasks configuration"));
    }
}
