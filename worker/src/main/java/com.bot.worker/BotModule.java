package com.bot.worker;

import com.bot.worker.common.Annotations.ThreadsCount;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Singleton;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Aleks on 11/14/16.
 */
public class BotModule extends AbstractModule {

    protected void configure() {
    }


    @Provides
    ScheduledExecutorService provideScheduledExecutorService(@ThreadsCount int threadsCount) {
        return Executors.newScheduledThreadPool(threadsCount);
    }

    @Provides
    Executor provideScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        return scheduledExecutorService;
    }

    @Singleton
    @Provides
    EventBus provideEventBus(Executor executor) {
        return new EventBus();
    }
}
