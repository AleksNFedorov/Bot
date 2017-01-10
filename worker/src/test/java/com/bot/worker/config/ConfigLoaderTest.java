package com.bot.worker.config;

import com.bot.worker.common.events.*;
import com.google.common.eventbus.EventBus;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Mockito.doNothing;

/**
 * Unit test for {@link ConfigLoader}
 */
public class ConfigLoaderTest {

    private static final String CONFIG_CONTENT = "<config>" +
            "    <group id=\"group1\">" +
            "        <task id=\"g1_task_1\" executor=\"executor 1\">" +
            "            <run>10</run>" +
            "            <deadline>3</deadline>" +
            "        </task>" +
            "        <task id=\"g1_task_2\" executor=\"executor 1\">" +
            "            <run>14</run>" +
            "            <deadline>8</deadline>" +
            "        </task>" +
            "    </group>" +
            "    <task id=\"task_2\" executor=\"testExecutor\">" +
            "        <run>2</run>" +
            "        <deadline>1</deadline>" +
            "    </task>" +
            "</config>";
    private static final TaskConfigLoadedResponse GROUP1_TASK1 =
            createResponse("group1",
                    "g1_task_1", "executor 1", 10L, 3L);
    private static final TaskConfigLoadedResponse GROUP1_TASK2 =
            createResponse("group1",
                    "g1_task_2", "executor 1", 14L, 8L);
    private static final TaskConfigLoadedResponse NO_GROUP_TASK2 =
            createResponse("",
                    "task_2", "testExecutor", 2L, 1L);
    private static int fileSequence = 0;

    @Rule
    public final MockitoRule mockitoInit = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private EventBus eventBus;

    @Captor
    private ArgumentCaptor<Object> postEventCaptor;

    private ConfigLoader loader;

    private File configFile;

    private static TaskConfigLoadedResponse createResponse(String groupName,
                                                           String taskName,
                                                           String executorId,
                                                           long runInterval,
                                                           long deadline) {

        XmlConfig.XmlTaskConfig task = new XmlConfig.XmlTaskConfig();
        task.setTaskName(taskName);
        task.setDeadline(deadline);
        task.setRunInterval(runInterval);
        task.setExecutorId(executorId);

        return new TaskConfigLoadedResponse.Builder()
                .setGroupName(groupName)
                .setTaskConfig(task)
                .build();
    }

    private static File createTmpConfigFile() throws IOException {
        File configFile = new File("./", String.format
                ("configLoaderTest_%d_%d" +
                                ".xml",
                        System.currentTimeMillis(),
                        fileSequence++));

        if (!configFile.createNewFile()) {
            throw new IOException(String.format("Unable to create new file: " +
                    "[%s]", configFile.getName()));
        }

        configFile.deleteOnExit();

        return configFile;
    }

    private static void writeContentToFile(String content, File dst) throws
            IOException {

        Writer stream = Files.asCharSink(dst,
                StandardCharsets.UTF_8,
                FileWriteMode
                        .APPEND).openBufferedStream();

        stream.write(content);
        stream.close();
    }

    @Before
    public void setUp() throws IOException {
        doNothing().when(eventBus).post(postEventCaptor.capture());

        configFile = createTmpConfigFile();

        loader = new ConfigLoader(configFile.getAbsolutePath());
        loader.setEventBus(eventBus);
    }

    @After
    public void tearDown() {
        configFile.delete();
    }

    @Test
    public void testOnInit_fileExists_configLoaded() throws IOException, JAXBException {

        writeContentToFile(CONFIG_CONTENT, configFile);

        loader.onInit(AppInitEvent.create());

        assertThat(postEventCaptor.getAllValues()).containsExactly
                (GROUP1_TASK1, GROUP1_TASK2, NO_GROUP_TASK2);
    }

    @Test
    public void testOnInit_noFile_throwsException() throws IOException, JAXBException {

        loader = new ConfigLoader(
                "SomeUnExistingFile_" + System.currentTimeMillis());
        loader.setEventBus(eventBus);
        thrown.expect(FileNotFoundException.class);

        loader.onInit(AppInitEvent.create());
    }

    @Test
    public void testOnInit_brokenFile_throwsException() throws IOException, JAXBException {

        writeContentToFile("Broken content", configFile);

        thrown.expect(DataBindingException.class);
        thrown.expectCause(isA(UnmarshalException.class));

        loader.onInit(AppInitEvent.create());
    }

    @Test
    public void testOnInit_emptyFile_throwsException() throws IOException, JAXBException {

        thrown.expect(DataBindingException.class);
        thrown.expectCause(isA(UnmarshalException.class));

        loader.onInit(AppInitEvent.create());
    }

    @Test
    public void testOnConfigReload_nullTaskName_allTasksLoaded() throws
            IOException, JAXBException {

        TaskHoldRequest holdRequest = TaskHoldRequest.create();
        GetStatusRequest taskStatusRequest = GetStatusRequest.create();
        writeContentToFile(CONFIG_CONTENT, configFile);

        loader.onConfigReload(TaskConfigReloadRequest.create());

        assertThat(postEventCaptor.getAllValues()).containsExactly(
                holdRequest,
                GROUP1_TASK1,
                GROUP1_TASK2,
                NO_GROUP_TASK2,
                taskStatusRequest
        );
    }

    @Test
    public void testOnConfigReload_unknownTaskName_noTasksLoaded() throws
            IOException, JAXBException {
        String unknownTaskName = "SomeUnknownTask_" + System
                .currentTimeMillis();

        TaskHoldRequest holdRequest = TaskHoldRequest.create(unknownTaskName);
        GetStatusRequest taskStatusRequest = GetStatusRequest.create
                (unknownTaskName);
        writeContentToFile(CONFIG_CONTENT, configFile);

        loader.onConfigReload(TaskConfigReloadRequest.create(unknownTaskName));

        assertThat(postEventCaptor.getAllValues()).containsExactly
                (holdRequest, taskStatusRequest);
    }

    @Test
    public void testOnConfigReload_knownTaskName_taskConfigLoaded() throws
            IOException, JAXBException {

        TaskHoldRequest holdRequest = TaskHoldRequest.create
                (NO_GROUP_TASK2.getTaskConfig().getTaskName());
        GetStatusRequest taskStatusRequest = GetStatusRequest.create
                (NO_GROUP_TASK2.getTaskConfig().getTaskName());
        writeContentToFile(CONFIG_CONTENT, configFile);

        loader.onConfigReload(TaskConfigReloadRequest.create
                (NO_GROUP_TASK2.getTaskConfig().getTaskName()));

        assertThat(postEventCaptor.getAllValues()).containsExactly(
                holdRequest,
                NO_GROUP_TASK2,
                taskStatusRequest
        );
    }
}
