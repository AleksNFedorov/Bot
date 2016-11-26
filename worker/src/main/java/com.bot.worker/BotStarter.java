package com.bot.worker;

import com.bot.worker.cli.BootOptions;
import com.bot.worker.cli.CliModule;
import com.google.inject.Guice;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by Aleks on 11/14/16.
 */
public class BotStarter {

    private static final Logger logger = LoggerFactory.getLogger(BotStarter.class);

    public static void main(String... args) {
        logger.info("Starting application with arguments " + Arrays.toString(args));
        try {
            CommandLine commandLine = new DefaultParser().parse(new BootOptions(), args);
            Guice.createInjector(
                    new CliModule(commandLine),
                    new BotModule())
                    .getInstance(BotApplication.class).start();
        } catch (Throwable th) {
            logger.error("Application running exception ", th);
        }
    }
}
