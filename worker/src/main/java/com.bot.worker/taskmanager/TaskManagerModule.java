package com.bot.worker.taskmanager;

import com.bot.common.ITaskExecutor;
import com.bot.common.ITaskResultProcessor;
import com.bot.worker.common.Annotations;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Created by Aleks on 1/12/17.
 */
public class TaskManagerModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(EventBus.class);
        bind(TaskManager.class).asEagerSingleton();
        requireBinding(new TypeLiteral<ImmutableList<ITaskExecutor>>() {
        }.getRawType());
        requireBinding(new TypeLiteral<ImmutableList<ITaskResultProcessor>>() {
        }.getRawType());
    }

    @Provides
    ScheduledExecutorService provideScheduledExecutorService(
            @Annotations.ThreadsCount int threadsCount) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(threadsCount);
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    @Provides
    ExecutorService provideScheduledExecutorService() {
        return Executors.newCachedThreadPool();
    }


}
