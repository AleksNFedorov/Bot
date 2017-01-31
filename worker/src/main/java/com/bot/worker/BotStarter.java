package com.bot.worker;

import com.bot.worker.cli.CliModule;
import com.bot.worker.common.events.AppInitEvent;
import com.bot.worker.taskmanager.TaskManagerModule;
import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Arrays;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts Bot application
 * @author Aleks
 */
public class BotStarter {

  private static final Logger logger = LoggerFactory.getLogger(BotStarter.class);

  public static void main(String... args) throws ParseException {
    logger.info("Starting application with arguments " +
        Arrays.toString(args));
    try {
      Injector injector = Guice.createInjector(
          new CliModule(args),
          new BotModule(),
          new TaskManagerModule());

      EventBus bus = injector.getInstance(EventBus.class);
      bus.post(AppInitEvent.create());

    } finally {
      logger.error("Application stopped ");
    }
  }
}
