package com.bot.worker.config;

import com.bot.worker.common.events.*;
import com.google.common.eventbus.EventBus;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.xml.bind.DataBindingException;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static com.google.common.truth.Truth.assertThat;
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

    @Mock
    private EventBus eventBus;

    @Captor
    private ArgumentCaptor<Object> postEventCaptor;

    private ConfigLoader loader;

    private File configFile;

    private static void writeContentToFile(String content, File dst) throws
            IOException {

        Writer stream = Files.asCharSink(dst,
                StandardCharsets.UTF_8,
                FileWriteMode
                        .APPEND).openBufferedStream();

        stream.write(content);
        stream.close();
    }

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
                .setTaskName(task.getTaskName())
                .setExecutorId(task.getExecutorId())
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

    @Before
    public void initTest() throws IOException {
        doNothing().when(eventBus).post(postEventCaptor.capture());

        configFile = createTmpConfigFile();

        loader = new ConfigLoader(configFile.getAbsolutePath());
        loader.setEventBus(eventBus);
    }

    @After
    public void finalizeTest() {
        configFile.delete();
    }

    @Test
    public void testOnInit_fileExists_configLoaded() throws IOException {

        writeContentToFile(CONFIG_CONTENT, configFile);

        loader.onInit(AppInitEvent.create());

        assertThat(postEventCaptor.getAllValues()).containsExactly
                (GROUP1_TASK1, GROUP1_TASK2, NO_GROUP_TASK2);
    }

    @Test
    public void testOnInit_noFile_throwsException() throws IOException {

        loader = new ConfigLoader("SomeUnExistingFile_" + System
                .currentTimeMillis());
        loader.setEventBus(eventBus);

        loader.onInit(AppInitEvent.create());

        assertThat(postEventCaptor.getAllValues()).hasSize(1);
        assertThat(((ExecutionExceptionEvent) postEventCaptor.getValue())
                .getComponentName()).isEqualTo(loader.getComponentName());
        assertThat(((ExecutionExceptionEvent) postEventCaptor.getValue())
                .getCause()).isInstanceOf(DataBindingException.class);
    }

    @Test
    public void testOnInit_brokenFile_throwsException() throws IOException {

        writeContentToFile("Broken content", configFile);

        loader.onInit(AppInitEvent.create());

        assertThat(postEventCaptor.getAllValues()).hasSize(1);
        assertThat(((ExecutionExceptionEvent) postEventCaptor.getValue())
                .getComponentName()).isEqualTo(loader.getComponentName());
        assertThat(((ExecutionExceptionEvent) postEventCaptor.getValue())
                .getCause()).isInstanceOf(DataBindingException.class);
    }

    @Test
    public void testOnInit_emptyFile_throwsException() throws IOException {

        writeContentToFile("", configFile);

        loader.onInit(AppInitEvent.create());

        assertThat(postEventCaptor.getAllValues()).hasSize(1);
        assertThat(((ExecutionExceptionEvent) postEventCaptor.getValue())
                .getComponentName()).isEqualTo(loader.getComponentName());
        assertThat(((ExecutionExceptionEvent) postEventCaptor.getValue())
                .getCause()).isInstanceOf(DataBindingException.class);

    }

    @Test
    public void testOnConfigReload_nullTaskName_allTasksLoaded() throws
            IOException {

        TaskHoldRequest holdRequest = TaskHoldRequest.create(null);
        GetStatusRequest taskStatusRequest = GetStatusRequest.create(null);
        writeContentToFile(CONFIG_CONTENT, configFile);

        loader.onConfigReload(TaskConfigReloadRequest.create(null));

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
            IOException {
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
            IOException {

        TaskHoldRequest holdRequest = TaskHoldRequest.create
                (NO_GROUP_TASK2.getTaskName());
        GetStatusRequest taskStatusRequest = GetStatusRequest.create
                (NO_GROUP_TASK2.getTaskName());
        writeContentToFile(CONFIG_CONTENT, configFile);

        loader.onConfigReload(TaskConfigReloadRequest.create
                (NO_GROUP_TASK2.getTaskName()));

        assertThat(postEventCaptor.getAllValues()).containsExactly(
                holdRequest,
                NO_GROUP_TASK2,
                taskStatusRequest
        );

    }
}
