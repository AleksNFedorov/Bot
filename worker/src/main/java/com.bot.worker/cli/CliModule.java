package com.bot.worker.cli;

import com.bot.worker.common.Annotations;
import com.google.inject.AbstractModule;
import org.apache.commons.cli.CommandLine;

/**
 * Created by Aleks on 11/14/16.
 */
public class CliModule extends AbstractModule {

    private static final int THREADS_COUNT_DEFAULT = 20;

    private final CommandLine commandLine;

    public CliModule(CommandLine options) {
        this.commandLine = options;
    }

    protected void configure() {
        bind(Integer.class).annotatedWith(Annotations.ThreadsCount.class).toInstance(getThreadsCount());
        bind(String.class).annotatedWith(Annotations.TaskConfigFile.class).toInstance(commandLine.getOptionValue(BotOptions.TASK_CONFIG_FILE_PATH));
    }

    private int getThreadsCount() {
        if (commandLine.hasOption(BotOptions.THREADS_COUNT)) {
            return Integer.parseInt(commandLine.getOptionValue(BotOptions.THREADS_COUNT));
        }
        return THREADS_COUNT_DEFAULT;
    }
}
