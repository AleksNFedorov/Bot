package com.bot.worker.cli;

import com.bot.common.TaskResult;
import com.bot.worker.common.Command;
import com.bot.worker.common.TaskStatus;
import com.bot.worker.common.events.*;
import com.google.common.eventbus.EventBus;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    private Executor cliExecutor;

    @Before
    public void initTest() {
        cliExecutor = Executors.newSingleThreadExecutor();
        processor = new CliProcessor(cliExecutor);
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
        processor.onInit(AppInitEvent.create());

        sendCommand("help", "");

        assertThat(out.toString()).startsWith("help");
    }

    @Test
    public void testStatusCommand_specificTask_statusRequestSent() {
        GetStatusRequest expectedRequest = GetStatusRequest.create("1");
        testCommand("status", "1", expectedRequest);
    }

    @Test
    public void testStatusCommand_allTasks_statusRequestSent() {
        GetStatusRequest expectedRequest = GetStatusRequest.create(null);
        testCommand("status", "all", expectedRequest);
    }

    @Test
    public void testHoldCommand_specificTask_statusRequestSent() {
        TaskHoldRequest expected = TaskHoldRequest.create("1");
        testCommand("hold", "1", expected);
    }

    @Test
    public void testHoldCommand_allTasks_statusRequestSent() {
        TaskHoldRequest expected = TaskHoldRequest.create(null);
        testCommand("hold", "all", expected);
    }

    @Test
    public void testScheduleCommand_specificTask_statusRequestSent() {
        TaskScheduleRequest expected = new TaskScheduleRequest.Builder()
                .setTaskName("1").build();
        testCommand("schedule", "1", expected);
    }

    @Test
    public void testScheduleCommand_allTasks_statusRequestSent() {
        TaskScheduleRequest expected = new TaskScheduleRequest.Builder()
                .setNullableTaskName(null).build();
        testCommand("schedule", "all", expected);
    }

    @Test
    public void testReloadCommand_specificTask_statusRequestSent() {
        TaskConfigReloadRequest expected = new TaskConfigReloadRequest.Builder()
                .setTaskName("1").build();
        testCommand("reload", "1", expected);
    }

    @Test
    public void testReloadCommand_allTasks_statusRequestSent() {
        TaskConfigReloadRequest expected = new TaskConfigReloadRequest.Builder()
                .setNullableTaskName(null).build();
        testCommand("reload", "all", expected);
    }

    @Test
    public void testDropCommand_specificTask_statusRequestSent() {
        TaskDropRequest expected = new TaskDropRequest.Builder()
                .setTaskName("1").build();
        testCommand("drop", "1", expected);
    }

    @Test
    public void testDropCommand_allTasks_statusRequestSent() {
        TaskDropRequest expected = new TaskDropRequest.Builder()
                .setNullableTaskName(null).build();
        testCommand("drop", "all", expected);
    }

    @Test
    public void testUnknownCommand_helpMessageDisplayed() {
        processor.onInit(AppInitEvent.create());

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
                                .build()).build());

        assertThat(out.toString()).contains("taskId");
        assertThat(out.toString()).contains("Finished");
        assertThat(out.toString()).contains("Success");
    }

    @Test
    public void testOnTaskUpdateStatus_resultPrinted() {

        String taskUpdateStatusMessage = "UpdateMessage" + System
                .currentTimeMillis();

        processor.onTaskUpdateStatus(new TaskUpdateResponse.Builder()
                .setTaskName("Task")
                .setResultMessage(taskUpdateStatusMessage)
                .setCommand(Command.drop)
                .build());

        assertThat(out.toString()).contains(taskUpdateStatusMessage);

    }

    private void testCommand(String command, String taskId, Object
            expectedEvent) {
        processor.onInit(AppInitEvent.create());
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

        CountDownLatch waitLatch = new CountDownLatch(1);
        cliExecutor.execute(waitLatch::countDown);
        try {
            waitLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
