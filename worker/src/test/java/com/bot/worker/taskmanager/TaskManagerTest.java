package com.bot.worker.taskmanager;

import com.bot.common.ITaskExecutor;
import com.bot.common.ITaskResultProcessor;
import com.bot.common.TaskConfig;
import com.bot.common.TaskResult;
import com.bot.worker.common.TaskStatus;
import com.bot.worker.common.events.*;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;

/**
 * Unit test for {@link TaskManager}
 */
public class TaskManagerTest {

    @Rule
    public final MockitoRule mockitoInit = MockitoJUnit.rule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private EventBus eventBus;

    @Mock
    private ITaskResultProcessor resultProcessor;

    @Captor
    private ArgumentCaptor eventBusPostCaptor;

    @Captor
    private ArgumentCaptor<TaskResult> resultProcessorCaptor;

    @Captor
    private ArgumentCaptor<List<TaskResult>> groupResultProcessorCaptor;

    private ITaskExecutor<TestTaskConfig> executor;

    private TaskManager taskManager;

    private AtomicReference<CountDownLatch> latchReference = new
            AtomicReference<>();

    private static GetStatusResponse.TaskInfo buildTaskInfo(TaskStatus
                                                                    status,
                                                            TaskResult result) {
        return new GetStatusResponse.TaskInfo.Builder()
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
        executor = new TestExecutor();
        doAnswer((x) -> {
            latchReference.get().countDown(); return null;
        }).when(resultProcessor).processResult
                (resultProcessorCaptor.capture(), groupResultProcessorCaptor
                        .capture());
        doNothing().when(eventBus).post(eventBusPostCaptor.capture());
        taskManager = new TaskManager(2, ImmutableList.of
                (executor), resultProcessor);
        taskManager.setEventBus(eventBus);
    }

    @After
    public void tearDown() throws Exception {
        taskManager.onAppStop(StopAppRequest.create());
    }

    @Test
    public void testCreateTaskManager_invalidThreadsCount_throwsException() {

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Threads count must be positive");

        taskManager = new TaskManager(0, ImmutableList.of
                (executor), resultProcessor);
    }

    @Test
    public void testExecuteTask_onNewTaskConfig_scheduledAndExecuted() throws
            InterruptedException {

        CountDownLatch latch = new CountDownLatch(2);
        latchReference.set(latch);
        TestTaskConfig config = new TestTaskConfig(TaskResult.Status.Success,
                1, 2L);
        TaskResult expectedResult = createTaskResult(config);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        //TODO add as test case - timunit is a must
        latch.await(10, TimeUnit.SECONDS);

        assertThat(resultProcessorCaptor.getValue()).isEqualTo(expectedResult);
        assertThat(groupResultProcessorCaptor.getValue()).isEmpty();

        assertThat(resultProcessorCaptor.getAllValues()).hasSize(2);
        assertThat(groupResultProcessorCaptor.getAllValues()).hasSize(2);
    }

    @Test
    public void testExecuteTask_onNewTaskConfig_onTimeTask_Executed() throws
            InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        latchReference.set(latch);
        TestTaskConfig config = new TestTaskConfig(TaskResult.Status.Success,
                1, -1L);
        TaskResult expectedResult = createTaskResult(config);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        //TODO add as test case - timunit is a must
        latch.await(10, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(config.getRunInterval() * 3);

        assertThat(resultProcessorCaptor.getValue()).isEqualTo(expectedResult);
        assertThat(groupResultProcessorCaptor.getValue()).isEmpty();

        assertThat(resultProcessorCaptor.getAllValues()).hasSize(1);
        assertThat(groupResultProcessorCaptor.getAllValues()).hasSize(1);
    }

    @Test
    public void testExecuteTask_onNewTaskConfig_groupResult_Executed() throws
            InterruptedException {

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

        config2.waitRunsEqual(1);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        //TODO add as test case - timunit is a must
        latch.await(50, TimeUnit.SECONDS);

        assertThat(resultProcessorCaptor.getValue()).isEqualTo(expectedResult);
        assertThat(groupResultProcessorCaptor.getValue()).containsExactly
                (expectedGroupResult);

        assertThat(resultProcessorCaptor.getAllValues()).hasSize(2);
        assertThat(groupResultProcessorCaptor.getAllValues()).hasSize(2);
    }

    @Test
    public void
    testExecuteTask_onNewTaskConfig_deadlineExceed_resultWithException() throws
            InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        latchReference.set(latch);
        TestTaskConfig config = new TestTaskConfig(TaskResult.Status.Success,
                100, -1L);

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        //TODO add as test case - timunit is a must
        latch.await(50, TimeUnit.SECONDS);

        assertThat(eventBusPostCaptor.getAllValues()).isEmpty();
        assertThat(resultProcessorCaptor.getValue().getStatus()).isEqualTo
                (TaskResult.Status.DeadlineExceed);
    }

    @Test
    public void
    testExecuteTask_onNewTaskConfig_executionException_exceptionPostToBus()
            throws
            InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        latchReference.set(latch);
        TestTaskConfig config = new TestTaskConfig(TaskResult.Status.Success,
                1, -1L);
        config.withRunException();

        taskManager.onNewTaskConfig(new TaskConfigLoadedResponse.Builder()
                .setGroupName("TestGroup")
                .setTaskConfig(config)
                .build());

        latch.await(5, TimeUnit.SECONDS);

        assertThat(eventBusPostCaptor.getAllValues()).hasSize(1);
        assertThat(eventBusPostCaptor.getValue()).isInstanceOf
                (ExecutionExceptionEvent.class);

        assertThat(resultProcessorCaptor.getValue().getStatus()).isEqualTo
                (TaskResult.Status.Exception);
        assertThat(resultProcessorCaptor.getValue().getMessage().get())
                .isEqualTo("Expected");
    }

    @Test
    public void testGetStatusRequest_specificTask_responsePost()
            throws
            InterruptedException {

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

        TaskResult result = resultProcessorCaptor.getValue();
        GetStatusResponse expectedResponse = new GetStatusResponse.Builder()
                .addTasksInfo(buildTaskInfo(TaskStatus.Finished, result))
                .build();

        assertThat(eventBusPostCaptor.getAllValues()).hasSize(1);
        assertThat(eventBusPostCaptor.getValue()).isEqualTo(expectedResponse);
    }

    @Test
    public void testGetStatusRequest_allTasks_responsePost()
            throws
            InterruptedException {

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

    private static class TestTaskConfig extends TaskConfig implements Runnable {

        private static int taskIdSequence;

        private final TaskResult.Status expectedStatus;

        private final long executionTime;

        private final AtomicInteger runCounter = new AtomicInteger();

        private volatile boolean throwExceptionOnRun;

        TestTaskConfig(TaskResult.Status expectedStatus, long
                executionTime, long runInterval) {
            this.expectedStatus = expectedStatus;
            this.executionTime = executionTime;
            this.executorId = TestExecutor.TASK_EXECUTOR_ID;
            this.taskName = "TestTask_" + taskIdSequence++;
            this.runInterval = runInterval;
            this.deadline = 4;
        }

        void withRunException() {
            throwExceptionOnRun = true;
        }

        public void run() {
            runCounter.incrementAndGet();
            if (throwExceptionOnRun) {
                throw new RuntimeException("Expected");
            }
        }

        void waitRunsEqual(int count) {
            while (runCounter.get() != count) {
                LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
            }
        }
    }

    private static class TestExecutor implements ITaskExecutor<TestTaskConfig> {

        private static final String TASK_EXECUTOR_ID = "ExecutorID";

        @Override
        public TaskResult executeTask(TestTaskConfig config) {

            try {
                TimeUnit.SECONDS.sleep(config.executionTime);
                config.run();
                System.out.println(String.format("Task [%s] executed", config
                        .getTaskName()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return createTaskResult(config);
        }

        @Override
        public String getId() {
            return TASK_EXECUTOR_ID;
        }
    }

}
