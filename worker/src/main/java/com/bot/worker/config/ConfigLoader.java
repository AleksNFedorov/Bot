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
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads task configs from configuration file
 * Initial config loading happens on receiving {@link AppInitEvent}
 *
 * <p>
 * Config example
 * {@code
 *
 * <config>
 *      <group id="GitHub.com">
 *          <task id="gitHubPing" executor="PING">
 *              <run>10</run>
 *              <deadline>3</deadline>
 *          </task>
 *          <task id="gitHubTrace" executor="TRACE">
 *              <run>30</run>
 *              <deadline>10</deadline>
 *          </task>
 *      </group>
 *      <task id="Java.com" executor="CUSTOM_EX">
 *          <run>2</run>
 *          <deadline>1</deadline>
 *          <executorConfig>
 *              <property key="key">value</property>
 *          </executorConfig>
 *      </task>
 *      <task id="runOnce" executor="CUSTOM_ONE_RUN">
 *          <run>-1</run>
 *      </task>
 * </config>
 * }
 *
 * <p>
 *  All communication with other app components goes though event bus,
 *  look {@link EventBusComponent} for more info
 *
 * @author Aleks
 */
@Singleton
public class ConfigLoader extends EventBusComponent {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);

    private final String pathToConfigFile;

    @Inject
    ConfigLoader(@TaskConfigFile String pathToConfigFile) {
        this.pathToConfigFile = pathToConfigFile;
    }

    private static boolean isValidTaskConfig(String taskName, XmlTaskConfig
            config) {
        return Constants.ALL.equals(taskName) || taskName.equals(config
                .getTaskName());
    }

    /**
     * Initial config loading on app init event
     * @param event app init event
     * @throws JAXBException in case of config parse exception
     * @throws IOException in case of config file IO exception
     */
    @Subscribe
    void onInit(AppInitEvent event) throws JAXBException, IOException {
        loadTaskConfigs(Constants.ALL);
    }

    /**
     * Handler for task config RELOAD requests
     *
     * @see TaskConfigReloadRequest
     * @param reloadEvent events with task name to RELOAD
     * @throws JAXBException in case of config parse exception
     * @throws IOException in case of config file IO exception
     */
    @Subscribe
    void onConfigReload(TaskConfigReloadRequest reloadEvent) throws JAXBException, IOException {
        String taskName = reloadEvent.getTaskName();
        post(TaskHoldRequest.create(taskName));
        loadTaskConfigs(taskName);
        post(GetStatusRequest.create(taskName));
    }

    private void loadTaskConfigs(String taskName) throws JAXBException, IOException {

        XmlConfig config = parseConfig();

        LOG.info("Total group confings: {}", config.getGroups());
        LOG.info("Total non-grouped configs : {}", config.getTasks());

        config.getGroups()
                .forEach(group -> group.getTasks()
                        .stream()
                        .filter(c -> isValidTaskConfig(taskName, c))
                        .forEach(task -> {
                                    processTaskConfig(group.getGroupName(), task);
                                }
                        )
                );

        //process un-grouped configs
        config.getTasks()
                .stream()
                .filter(c -> isValidTaskConfig(taskName, c))
                .forEach(task -> processTaskConfig(Constants.NO_GROUP, task));

        LOG.info("All configs loaded");
    }

    private void processTaskConfig(String groupName, XmlTaskConfig task) {
        LOG.info("Loading config {}", task);
        post(TaskConfigLoadedResponse.create(groupName, task));
    }

    private XmlConfig parseConfig() throws JAXBException, IOException {
        try (BufferedInputStream configStream =
                new BufferedInputStream(new FileInputStream(pathToConfigFile))) {
            return JAXB.unmarshal(configStream, XmlConfig.class);
        }
    }

}
