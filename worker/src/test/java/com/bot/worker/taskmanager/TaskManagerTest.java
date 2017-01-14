package com.bot.worker.taskmanager;

import com.bot.common.TaskConfig;
import com.bot.common.TaskExecutor;
import com.bot.common.TaskResult;
import com.bot.common.TaskResultProcessor;
import com.bot.worker.common.TaskStatus;
import com.bot.worker.common.events.*;
import com.bot.worker.common.events.GetStatusResponse.TaskInfo;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

/**
 * Unit test for {@link TaskManager}
 */
public class TaskManagerTest {

    private static int taskIdSequence;

    @Rule
    public final MockitoRule mockitoInit = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private EventBus eventBus;

    @Mock
    private TaskResultProcessor resultProcessor;

    @Captor
    private ArgumentCaptor eventBusPostCaptor;

    @Captor
    private ArgumentCaptor<TaskResult> resultProcessorCaptor;

    @Captor
    private ArgumentCaptor<List<TaskResult>> groupResultProcessorCaptor;

    private TaskExecutor executor;

    private TaskManager taskManager;

    private AtomicReference<CountDownLatch> latchReference = new AtomicReference<>();

    private ScheduledExecutorService executorService;

    private static TaskInfo buildTaskInfo(TaskStatus status, TaskResult
            result) {
        return new TaskInfo.Builder()
                .setTaskName(result.getTaskName())
                .setStatus(status)
                .setResultStatus(result.getStatus())
                .setResultTimestamp(result.getTimestamp())
                .build();
    }

    private static TaskResult createTaskResult(TestTaskConfig config) {
        return new TaskResult(config.getTaskName(), config.expectedStatus);
    }

    @Before
    public void setUp() throws Exception {

        executorService = new ScheduledThreadPoolExecutor(1);
        executor = new TestExecutor();
        doAnswer((x) -> {
            latchReference.get().countDown(); return null;
        }).when(resultProcessor).processResult
                (resultProcessorCaptor.capture(), groupResultProcessorCaptor
                        .capture());
        doNothing().when(eventBus).post(eventBusPostCaptor.capture());
        taskManager = new TaskManager(executorService,
                ImmutableList.of(executor),
                ImmutableList.of(resultProcessor));
        taskManager.setEventBus(eventBus);
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdownNow();
    }

    @Test
    public void testExecuteTask_onNewTaskConfig_scheduledAndExecuted()
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(2);
        latchReference.set(latch);
        TestTaskConfig config = new TestTaskConfig(TaskResult.Status.Success,
                1, 2L);
        TaskResult expectedResult = createTaskResult(config);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        latch.await(10, TimeUnit.SECONDS);

        assertThat(resultProcessorCaptor.getValue()).isEqualTo(expectedResult);
        assertThat(groupResultProcessorCaptor.getValue()).isEmpty();

        assertThat(resultProcessorCaptor.getAllValues()).hasSize(2);
        assertThat(groupResultProcessorCaptor.getAllValues()).hasSize(2);
    }

    @Test
    public void testExecuteTask_onNewTaskConfig_onTimeTask_Executed()
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        latchReference.set(latch);
        TestTaskConfig config = new TestTaskConfig(TaskResult.Status.Success,
                1, -1L);
        TaskResult expectedResult = createTaskResult(config);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        latch.await(10, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(config.getRunInterval() * 3);

        assertThat(resultProcessorCaptor.getValue()).isEqualTo(expectedResult);
        assertThat(groupResultProcessorCaptor.getValue()).isEmpty();

        assertThat(resultProcessorCaptor.getAllValues()).hasSize(1);
        assertThat(groupResultProcessorCaptor.getAllValues()).hasSize(1);
    }

    @Test
    public void testExecuteTask_onNewTaskConfig_groupResult_Executed()
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(2);
        latchReference.set(latch);
        TestTaskConfig config = new TestTaskConfig(TaskResult.Status.Success,
                1, -1L);
        TestTaskConfig config2 = new TestTaskConfig(TaskResult.Status.Success,
                1, -1L);

        TaskResult expectedGroupResult = createTaskResult(config2);
        TaskResult expectedResult = createTaskResult(config);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config2)
                .build());

        config2.waitRunCompleteEqual(1);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        latch.await(50, TimeUnit.SECONDS);

        assertThat(resultProcessorCaptor.getValue()).isEqualTo(expectedResult);
        assertThat(groupResultProcessorCaptor.getValue()).containsExactly
                (expectedGroupResult);

        assertThat(resultProcessorCaptor.getAllValues()).hasSize(2);
        assertThat(groupResultProcessorCaptor.getAllValues()).hasSize(2);
    }

    @Test
    public void
    testExecuteTask_onNewTaskConfig_deadlineExceed_resultWithException()
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        latchReference.set(latch);
        TestTaskConfig config = new TestTaskConfig(TaskResult.Status.Success,
                100, -1L);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        latch.await(50, TimeUnit.SECONDS);

        assertThat(eventBusPostCaptor.getAllValues()).isEmpty();
        assertThat(resultProcessorCaptor.getValue().getStatus()).isEqualTo
                (TaskResult.Status.DeadlineExceed);
    }

    @Test
    public void
    testExecuteTask_onNewTaskConfig_executionException_resultWithException()
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        latchReference.set(latch);
        TaskConfig config = new ExceptionTestTaskConfig();

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        latch.await(5, TimeUnit.SECONDS);

        assertThat(eventBusPostCaptor.getAllValues()).hasSize(0);

        assertThat(resultProcessorCaptor.getValue().getStatus()).isEqualTo
                (TaskResult.Status.Exception);
        assertThat(resultProcessorCaptor.getValue().getMessage().get())
                .isEqualTo("Expected");
    }

    @Test
    public void testGetStatusRequest_specificTask_responsePost()
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(2);
        latchReference.set(latch);
        TestTaskConfig config = new TestTaskConfig(
                TaskResult.Status.Success,
                1, -1L);

        TestTaskConfig config2 = new TestTaskConfig(
                TaskResult.Status.Success,
                1, -1L);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config2)
                .build());

        latch.await(5, TimeUnit.SECONDS);

        taskManager.onGetStatusRequest(GetStatusRequest.create(config
                .getTaskName()));

        TaskResult result = resultProcessorCaptor.getAllValues().get(0);
        GetStatusResponse expectedResponse = new GetStatusResponse.Builder()
                .addTasksInfo(buildTaskInfo(TaskStatus.Finished, result))
                .build();

        assertThat(eventBusPostCaptor.getAllValues()).hasSize(1);
        assertThat(eventBusPostCaptor.getValue()).isEqualTo(expectedResponse);
    }

    @Test
    public void testGetStatusRequest_allTasks_responsePosted()
            throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(2);
        latchReference.set(latch);
        TestTaskConfig config = new TestTaskConfig(
                TaskResult.Status.Success,
                1, -1L);

        TestTaskConfig config2 = new TestTaskConfig(
                TaskResult.Status.Success,
                1, -1L);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config2)
                .build());

        latch.await(15, TimeUnit.SECONDS);

        taskManager.onGetStatusRequest(GetStatusRequest.create());

        TaskResult firstResult = resultProcessorCaptor.getAllValues().get(0);
        TaskResult secondResult = resultProcessorCaptor.getAllValues().get(1);
        GetStatusResponse expectedResponse = new GetStatusResponse.Builder()
                .addTasksInfo(buildTaskInfo(TaskStatus.Finished, firstResult))
                .addTasksInfo(buildTaskInfo(TaskStatus.Finished, secondResult))
                .build();

        assertThat(eventBusPostCaptor.getAllValues()).hasSize(1);
        assertThat(eventBusPostCaptor.getValue()).isEqualTo(expectedResponse);
    }

    @Test
    public void
    testOnTaskHoldEvent_oneTimeTask_running_exceptionThrown_stateChangedToHold()
            throws InterruptedException {

        CountDownLatch resultProcessedLatch = new CountDownLatch(1);
        latchReference.set(resultProcessedLatch);

        TestTaskConfig config = new TestTaskConfig(
                TaskResult.Status.Success,
                10, -1L, 1000);
        config.setBlockRun(true);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        config.waitRunStartEqual(1);

        taskManager.onTaskHoldEvent(TaskHoldRequest.create(config.getTaskName()));
        config.setBlockRun(false);

        resultProcessedLatch.await(10, TimeUnit.SECONDS);

        GetStatusResponse response = (GetStatusResponse) eventBusPostCaptor
                .getValue();
        TaskInfo taskInfo = response.getTasksInfo().get(0);

        assertThat(eventBusPostCaptor.getAllValues()).hasSize(1);
        assertThat(response.getTasksInfo()).hasSize(1);

        assertThat(taskInfo.getTaskName()).isEqualTo(config.getTaskName());
        assertThat(taskInfo.getStatus()).isEqualTo(TaskStatus.Hold);
        assertThat(taskInfo.getResultStatus()).isEqualTo(TaskResult.Status
                .NoStatusYet);
    }

    @Test
    public void testOnTaskHoldEvent_oneTimeTask_finished_noChanges()
            throws InterruptedException {

        CountDownLatch resultProcessedLatch = new CountDownLatch(1);
        latchReference.set(resultProcessedLatch);

        TestTaskConfig config = new TestTaskConfig(
                TaskResult.Status.Success,
                1, -1L, Integer.MAX_VALUE);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        resultProcessedLatch.await(10, TimeUnit.SECONDS);

        taskManager.onTaskHoldEvent(TaskHoldRequest.create(config.getTaskName()));
        GetStatusResponse response = (GetStatusResponse) eventBusPostCaptor.getValue();

        assertThat(eventBusPostCaptor.getAllValues()).hasSize(1);
        assertThat(response.getTasksInfo()).hasSize(1);
        assertThat(response.getTasksInfo().get(0).getTaskName()).isEqualTo(config.getTaskName());
        assertThat(response.getTasksInfo().get(0).getStatus()).isEqualTo(TaskStatus.Finished);
        assertThat(response.getTasksInfo().get(0).getResultStatus()).isEqualTo(TaskResult.Status.Success);
    }

    @Test
    public void testOnTaskScheduleEvent_oneTimeTask_finished_executedAgain()
            throws InterruptedException {

        CountDownLatch resultProcessedLatch = new CountDownLatch(1);
        latchReference.set(resultProcessedLatch);

        TestTaskConfig config = new TestTaskConfig(
                TaskResult.Status.Success,
                1, -1L);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        resultProcessedLatch.await(10, TimeUnit.SECONDS);

        resultProcessedLatch = new CountDownLatch(1);
        latchReference.set(resultProcessedLatch);

        taskManager.onTaskScheduleEvent(TaskScheduleRequest.create(config.getTaskName()));
        resultProcessedLatch.await(10, TimeUnit.SECONDS);


        GetStatusResponse response = (GetStatusResponse) eventBusPostCaptor.getAllValues().get(0);

        assertThat(eventBusPostCaptor.getAllValues()).hasSize(1);
        assertThat(response.getTasksInfo()).hasSize(1);
        assertThat(response.getTasksInfo().get(0).getTaskName()).isEqualTo(config.getTaskName());
        assertThat(response.getTasksInfo().get(0).getStatus()).isEqualTo(TaskStatus.Finished);
        assertThat(response.getTasksInfo().get(0).getResultStatus()).isEqualTo(TaskResult.Status.Success);
    }

    private static class TestTaskConfig extends TaskConfig implements Runnable {

        private final TaskResult.Status expectedStatus;

        private final long executionTime;

        private final AtomicInteger runCompleteCounter = new AtomicInteger();
        private final AtomicInteger runStartCounter = new AtomicInteger();
        private final AtomicBoolean blockRun = new AtomicBoolean(false);

        TestTaskConfig(TaskResult.Status expectedStatus, long
                executionTime, long runInterval) {
            this(expectedStatus, executionTime, runInterval, 4);
        }

        TestTaskConfig(TaskResult.Status expectedStatus, long
                executionTime, long runInterval, long deadline) {
            this.expectedStatus = expectedStatus;
            this.executionTime = executionTime;
            this.executorId = TestExecutor.TASK_EXECUTOR_ID;
            this.taskName = "TestTask_" + taskIdSequence++;
            this.runInterval = runInterval;
            this.deadline = deadline;
        }

        public void run() {
            try {
                onRunStart();
                waitRunBlocked();
                TimeUnit.SECONDS.sleep(this.executionTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                onRunComplete();
            }
        }

        private synchronized void onRunStart() {
            System.out.println(String.format("Task [%s] executing",
                    getTaskName()));
            runStartCounter.incrementAndGet();
            notifyAll();
        }

        private synchronized void onRunComplete() {
            runCompleteCounter.incrementAndGet();
            System.out.println(String.format("Task [%s] executed",
                    getTaskName()));
            notifyAll();
        }

        private synchronized void waitRunBlocked() throws InterruptedException {
            while (blockRun.get()) {
                wait(TimeUnit.SECONDS.toMillis(1));
                System.out.println(String.format("Task [%s] blocked",
                        getTaskName()));
            }
        }

        private synchronized void setBlockRun(boolean blockRun) {
            this.blockRun.set(blockRun);
            notifyAll();
        }

        private synchronized void waitRunCompleteEqual(int count) throws
                InterruptedException {
            while (runCompleteCounter.get() != count) {
                wait(TimeUnit.SECONDS.toMillis(1));
            }
        }

        private synchronized void waitRunStartEqual(int count) throws
                InterruptedException {
            while (runStartCounter.get() != count) {
                wait(TimeUnit.SECONDS.toMillis(1));
            }
        }
    }

    private static class ExceptionTestTaskConfig extends TestTaskConfig implements Runnable {

        ExceptionTestTaskConfig() {
            super(TaskResult.Status.Success, 1, 1, 4L);
        }

        @Override
        public void run() {
            throw new RuntimeException("Expected");
        }
    }

    private static class TestExecutor implements TaskExecutor {

        private static final String TASK_EXECUTOR_ID = "ExecutorID";

        @Override
        public TaskResult executeTask(TaskConfig config) {
            TestTaskConfig testConfig = (TestTaskConfig) config;
            testConfig.run();
            return createTaskResult(testConfig);
        }

        @Override
        public String getId() {
            return TASK_EXECUTOR_ID;
        }
    }


}
