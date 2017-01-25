package com.bot.worker.taskmanager;

import com.bot.worker.common.Annotations;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Task manager Google guice module, initializes required bindings for task management and execution
 *
 * @author Aleks
 */
public class TaskManagerModule extends AbstractModule {

  @Override
  protected void configure() {
    requireBinding(EventBus.class);
    bind(TaskManager.class).asEagerSingleton();
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
