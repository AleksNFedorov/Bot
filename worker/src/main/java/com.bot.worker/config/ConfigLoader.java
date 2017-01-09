package com.bot.worker.config;

import com.bot.worker.EventBusComponent;
import com.bot.worker.common.Annotations.TaskConfigFile;
import com.bot.worker.common.Constants;
import com.bot.worker.common.events.*;
import com.bot.worker.config.XmlConfig.XmlTaskConfig;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
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
//TODO consdier to make thread safe
@Singleton
@NotThreadSafe
public class ConfigLoader extends EventBusComponent {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader
            .class);

    private final String pathToConfigFile;

    @Inject
    public ConfigLoader(@TaskConfigFile String pathToConfigFile) {
        this.pathToConfigFile = pathToConfigFile;
    }

    @Subscribe
    void onInit(AppInitEvent event) {
        loadTaskConfigs(Constants.ALL);
    }

    @Subscribe
    void onConfigReload(TaskConfigReloadRequest reloadEvent) {
        String taskName = reloadEvent.getTaskName().or(Constants.ALL);
        //TODO replace with create method
        post(new TaskHoldRequest.Builder().setNullableTaskName(reloadEvent
                .getTaskName().orNull()).build());
        loadTaskConfigs(taskName);
        //TODO replace with create
        post(new GetStatusRequest.Builder().setNullableTaskName(reloadEvent
                .getTaskName().orNull()).build());
    }

    private void loadTaskConfigs(String taskName) {

        try {

            XmlConfig config = parseConfig();

            logger.info("Total group confings: {}", config.getGroups());
            logger.info("Total non-grouped configs : {}", config.getTasks());

            //TODO flatten map
            config.getGroups().forEach((group) ->
                    group.getTasks().stream().filter((c) -> isValidTaskConfig
                            (taskName, c)).forEach((task) ->
                            processTaskConfig(group.getGroupName(), task)
                    )
            );

            //process un-grouped configs
            config.getTasks().stream().filter(c -> isValidTaskConfig
                    (taskName, c)).forEach(this::processTaskConfig);

            logger.info("All configs loaded");

        } catch (Exception e) {
            //TODO remove
            postException(e);
        }
    }

    //TODO convert to static
    private boolean isValidTaskConfig(String taskName, XmlTaskConfig config) {
        return Constants.ALL.equals(taskName) || taskName.equals(config
                .getTaskName());
    }

    //TODO delete method
    private void processTaskConfig(XmlTaskConfig config) {
        processTaskConfig(Constants.NO_GROUP, config);
    }

    private void processTaskConfig(String groupName, XmlTaskConfig task) {
        logger.info("Loading config {}", task);

        //TODO replace with create
        post(new TaskConfigLoadedResponse.Builder()
                .setGroupName(groupName)
                .setTaskConfig(task)
                .build()
        );
    }

    private XmlConfig parseConfig() throws JAXBException, IOException {
        try (BufferedInputStream configStream = new BufferedInputStream(new
                FileInputStream(pathToConfigFile))) {
            return JAXB.unmarshal(configStream, XmlConfig.class);
        }
    }

    @Override
    public String getComponentName() {
        return "ConfigLoader";
    }

}
