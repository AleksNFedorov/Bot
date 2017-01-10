package com.bot.worker;

import com.bot.worker.cli.CliModule;
import com.bot.worker.common.events.AppInitEvent;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by Aleks on 11/14/16.
 */
public class BotStarter {

    private static final Logger logger = LoggerFactory.getLogger(BotStarter.class);

    public static void main(String... args) {
        logger.info("Starting application with arguments " +
                Arrays.toString(args));
        try {
            Injector injector = Guice.createInjector(
                    new CliModule(args),
                    new BotModule());

            EventBus bus = injector.getInstance(EventBus.class);
            bus.post(AppInitEvent.create());

        } catch (Throwable th) {
            logger.error("Application running exception ", th);
        }
    }
}
