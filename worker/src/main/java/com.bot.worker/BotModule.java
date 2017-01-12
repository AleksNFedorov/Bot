package com.bot.worker;

import com.bot.common.ITaskExecutor;
import com.bot.common.ITaskResultProcessor;
import com.bot.worker.config.ConfigLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.ServiceLoader;

/**
 * Created by Aleks on 11/14/16.
 */

class BotModule extends AbstractModule {

    protected void configure() {
        bind(ConfigLoader.class).asEagerSingleton();
    }

    @Provides
    ImmutableList<ITaskExecutor> provideTaskExecutors() {
        return ImmutableList.copyOf(ServiceLoader.load(ITaskExecutor.class).iterator());
    }

    @Provides
    ImmutableList<ITaskResultProcessor> provideResultProcessors() {
        return ImmutableList.copyOf(ServiceLoader.load(ITaskResultProcessor.class).iterator());
    }

    @Singleton
    @Provides
    EventBus provideEventBus() {
        return new EventBus(new SubscriberExceptionHandler() {

            private final Logger logger = LoggerFactory.getLogger("ExceptionLogger");

            @Override
            public void handleException(Throwable exception, SubscriberExceptionContext context) {
                logger.error("Exception", exception);
            }
        });
    }
}
