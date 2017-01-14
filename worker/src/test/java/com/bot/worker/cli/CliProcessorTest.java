package com.bot.worker.cli;

import com.bot.common.TaskResult;
import com.bot.worker.common.TaskStatus;
import com.bot.worker.common.events.*;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doNothing;

/**
 * JUnit test for {@link CliProcessor}
 */

public class CliProcessorTest {

    @Rule
    public final MockitoRule mockitoInit = MockitoJUnit.rule();

    @Mock
    private EventBus eventBus;

    @Captor
    private ArgumentCaptor<Object> postEventCaptor;

    private CliProcessor processor;

    private InputStream originalIn;
    private PrintStream originalOut;

    private ByteArrayOutputStream out;
    private ByteArrayInputStream in;

    private byte[] commandBuffer = new byte[20];

    @Before
    public void initTest() {

        processor = new CliProcessor(MoreExecutors.directExecutor());
        processor.setEventBus(eventBus);
        out = new ByteArrayOutputStream(1000);
        in = new ByteArrayInputStream(commandBuffer);
        in.mark(commandBuffer.length);
        in.reset();

        originalIn = System.in;
        originalOut = System.out;

        System.setOut(new PrintStream(out));
        System.setIn(in);

        doNothing().when(eventBus).post(postEventCaptor.capture());
    }

    @After
    public void finishTest() {

        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    public void testHelpCommand_helpPrinted() {

        sendCommand("help", "");

        assertThat(out.toString()).startsWith("help");
    }

    @Test
    public void testStatusCommand_specificTask_statusRequestSent() {
        testCommand("status", "1", GetStatusRequest.create("1"));
    }

    @Test
    public void testStatusCommand_allTasks_statusRequestSent() {
        testCommand("status", "all", GetStatusRequest.create());
    }

    @Test
    public void testHoldCommand_specificTask_statusRequestSent() {
        testCommand("hold", "1", TaskHoldRequest.create("1"));
    }

    @Test
    public void testHoldCommand_allTasks_statusRequestSent() {
        testCommand("hold", "all", TaskHoldRequest.create());
    }

    @Test
    public void testScheduleCommand_specificTask_statusRequestSent() {
        testCommand("schedule", "1", TaskScheduleRequest.create("1"));
    }

    @Test
    public void testScheduleCommand_allTasks_statusRequestSent() {
        testCommand("schedule", "all", TaskScheduleRequest.create());
    }

    @Test
    public void testReloadCommand_specificTask_statusRequestSent() {
        testCommand("reload", "1", TaskConfigReloadRequest.create("1"));
    }

    @Test
    public void testReloadCommand_allTasks_statusRequestSent() {
        testCommand("reload", "all", TaskConfigReloadRequest.create());
    }

    @Test
    public void testDropCommand_specificTask_statusRequestSent() {
        testCommand("drop", "1", TaskDropRequest.create("1"));
    }

    @Test
    public void testDropCommand_allTasks_statusRequestSent() {
        testCommand("drop", "all", TaskDropRequest.create());
    }

    @Test
    public void testUnknownCommand_helpMessageDisplayed() {

        sendCommand("unk" + System.currentTimeMillis(), "");

        assertThat(out.toString()).contains("Available commands");
    }

    @Test
    public void testOnStatusResponse_responsePrinted() {

        LocalDateTime localTime = LocalDateTime.now();

        processor.onStatusResponse(new GetStatusResponse.Builder()
                .addTasksInfo(
                        new GetStatusResponse.TaskInfo.Builder()
                                .setResultTimestamp(localTime)
                                .setResultStatus(TaskResult.Status.Success)
                                .setTaskName("taskId")
                                .setStatus(TaskStatus.Finished)
                                .build())
                .build());

        assertThat(out.toString()).contains("taskId");
        assertThat(out.toString()).contains("Finished");
        assertThat(out.toString()).contains("Success");
    }

    @Test
    public void testOnTaskUpdateStatus_resultPrinted() {

        String taskUpdateStatusMessage = "UpdateMessage"
                + System.currentTimeMillis();

        processor.onTaskUpdateStatus(new TaskUpdateResponse.Builder()
                .setTaskName("Task")
                .setResultMessage(taskUpdateStatusMessage)
                .setCommand(Command.drop)
                .build());

        assertThat(out.toString()).contains(taskUpdateStatusMessage);
    }

    private void testCommand(String command, String taskId,
                             Object expectedEvent) {

        sendCommand(command, taskId);
        assertThat(postEventCaptor.getValue()).isEqualTo(expectedEvent);
    }

    private void sendCommand(String command, String task) {

        String commandString = String.format("--%s %s\n", command, task);
        int copySize = Math.min(commandString.length(), commandBuffer.length);
        System.arraycopy(commandString.getBytes(), 0, commandBuffer, 0,
                copySize);

        in.mark(0);
        in.reset();

        try {
            processor.onInit(AppInitEvent.create());
        } catch (NoSuchElementException expected) {
        }
    }
}