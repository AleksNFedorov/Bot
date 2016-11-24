package com.bot.worker;

import com.bot.common.ITaskExecutor;
import com.bot.common.ITaskResultProcessor;
import com.bot.worker.common.Annotations.ThreadsCount;
import com.bot.worker.taskmanager.TaskResultProcessorDecorator;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Singleton;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * Created by Aleks on 11/14/16.
 */
class BotModule extends AbstractModule {

    protected void configure() {
        bind(ITaskResultProcessor.class).to(TaskResultProcessorDecorator.class);
    }

    @Provides
    Map<String, ITaskExecutor> provideTaskExecutors() {
        ServiceLoader<ITaskExecutor> service = ServiceLoader.load(ITaskExecutor.class);

        return ImmutableList.copyOf(service.iterator()).stream().collect(Collectors.toMap(ITaskExecutor::getId, x -> x));
    }

    @Provides
    ImmutableList<ITaskResultProcessor> provideResultProcessors() {
        return ImmutableList.of();
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
