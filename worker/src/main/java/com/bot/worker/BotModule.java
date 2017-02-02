package com.bot.worker;

import com.bot.common.TaskExecutor;
import com.bot.common.TaskResultProcessor;
import com.bot.worker.config.ConfigLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.ServiceLoader;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bot application guice module, initializes global bindings.
 * Responsible for loading registered implementations of {@link TaskResultProcessor} and {@link TaskExecutor}
 *
 * @author Aleks
 */
class BotModule extends AbstractModule {

  protected void configure() {
    bind(ConfigLoader.class).asEagerSingleton();
  }


  @Singleton
  @Provides
  ImmutableList<TaskExecutor> provideTaskExecutors() {
    return ImmutableList.copyOf(
        ServiceLoader
            .load(TaskExecutor.class)
            .iterator());
  }

  @Singleton
  @Provides
  ImmutableList<TaskResultProcessor> provideResultProcessors() {
    return ImmutableList.copyOf(
            ServiceLoader.load(TaskResultProcessor.class)
                    .iterator());
  }

  @Singleton
  @Provides
  EventBus provideEventBus() {
    return new EventBus(new SubscriberExceptionHandler() {

      private final Logger LOG = LoggerFactory.getLogger("ExceptionLogger");

      @Override
      public void handleException(final Throwable exception,
          final SubscriberExceptionContext context) {

        LOG.error("EXCEPTION", exception);
      }
    });
  }
}
