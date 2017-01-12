package com.bot.worker.config;

import com.bot.worker.EventBusComponent;
import com.bot.worker.common.Annotations.TaskConfigFile;
import com.bot.worker.common.Constants;
import com.bot.worker.common.events.AppInitEvent;
import com.bot.worker.common.events.GetStatusRequest;
import com.bot.worker.common.events.TaskConfigLoadedResponse;
import com.bot.worker.common.events.TaskConfigReloadRequest;
import com.bot.worker.common.events.TaskHoldRequest;
import com.bot.worker.config.XmlConfig.XmlTaskConfig;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Aleks on 11/17/16.
 */
@Singleton
public class ConfigLoader extends EventBusComponent {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    private final String pathToConfigFile;

    @Inject
    ConfigLoader(@TaskConfigFile String pathToConfigFile) {
        this.pathToConfigFile = pathToConfigFile;
    }

    @Subscribe
    void onInit(AppInitEvent event) throws JAXBException, IOException {
        loadTaskConfigs(Constants.ALL);
    }

    @Subscribe
    void onConfigReload(TaskConfigReloadRequest reloadEvent) throws JAXBException, IOException {
        String taskName = reloadEvent.getTaskName();
        post(TaskHoldRequest.create(taskName));
        loadTaskConfigs(taskName);
        post(GetStatusRequest.create(taskName));
    }

    private void loadTaskConfigs(String taskName) throws JAXBException, IOException {

        XmlConfig config = parseConfig();

        logger.info("Total group confings: {}", config.getGroups());
        logger.info("Total non-grouped configs : {}", config.getTasks());

        config.getGroups()
                .forEach(group -> group.getTasks()
                        .stream()
                        .filter((c) -> isValidTaskConfig(taskName, c))
                        .forEach((task) -> {
                                    processTaskConfig(group.getGroupName(), task);
                                }
                        )
                );

        //process un-grouped configs
        config.getTasks()
                .stream()
                .filter(c -> isValidTaskConfig(taskName, c))
                .forEach(task -> processTaskConfig(Constants.NO_GROUP, task));

        logger.info("All configs loaded");
    }

    private void processTaskConfig(String groupName, XmlTaskConfig task) {
        logger.info("Loading config {}", task);
        post(TaskConfigLoadedResponse.create(groupName, task));
    }

    private XmlConfig parseConfig() throws JAXBException, IOException {
        try (BufferedInputStream configStream =
                     new BufferedInputStream(new FileInputStream(pathToConfigFile))) {
            return JAXB.unmarshal(configStream, XmlConfig.class);
        }
    }

    private static boolean isValidTaskConfig(String taskName, XmlTaskConfig config) {
        return Constants.ALL.equals(taskName) || taskName.equals(config
                .getTaskName());
    }

}
