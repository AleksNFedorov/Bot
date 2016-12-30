package com.bot.worker;

import com.bot.worker.cli.CliProcessor;
import com.bot.worker.common.events.AppInitEvent;
import com.bot.worker.config.ConfigLoader;
import com.bot.worker.taskmanager.TaskManager;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Created by Aleks on 11/14/16.
 */
public class BotApplication extends EventBusComponent {

    private static final Logger logger = LoggerFactory.getLogger
            (BotApplication.class);

    private final ConfigLoader configLoader;

    private final TaskManager taskManager;

    private final CliProcessor cliProcessor;

    @Inject
    public BotApplication(ConfigLoader configLoader, TaskManager taskManager,
                          CliProcessor cliProcessor) {
        this.configLoader = configLoader;
        this.taskManager = taskManager;
        this.cliProcessor = cliProcessor;
    }

    public void start() {
        post(AppInitEvent.create());
    }

    @Subscribe
    private void onStartEvent(Object o) {
        logger.info("onStartEvent {}", o);
    }

    @Override
    public String getComponentName() {
        return "BotApplication";
    }
}
