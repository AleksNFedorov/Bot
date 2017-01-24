package com.bot.worker.taskmanager;

import com.bot.common.TaskConfig;
import com.bot.common.TaskResult;
import com.bot.worker.common.TaskStatus;
import java.util.concurrent.Future;

/**
 * Container for task configuration and execution info
 *
 * <p>
 * Contains next data
 * <ul>
 *     <li>{@link TaskConfig}</li>
 *     <li>Group name</li>
 *     <li>Task execution {@link Future}</li>
 *     <li>Last {@link TaskResult}</li>
 *     <li>{@link TaskStatus}</li>
 * </ul>
 *
 * @author Aleks
 */
class TaskContext {

  private final TaskConfig config;

  private final String groupName;

  private Future<?> future;

  private TaskResult lastTaskResult;

  private TaskStatus status = TaskStatus.Scheduled;

  TaskContext(TaskConfig config, String groupName) {
    this.config = config;
    this.groupName = groupName;
    setLastTaskResult(new TaskResult(config.getTaskName(), TaskResult
        .Status.NoStatusYet));
  }

  String getGroupName() {
    return groupName;
  }

  void setFuture(Future<?> future) {
    this.future = future;
  }

  String getTaskName() {
    return config.getTaskName();
  }

  TaskResult getLastTaskResult() {
    return lastTaskResult;
  }

  void setLastTaskResult(TaskResult lastTaskResult) {
    this.lastTaskResult = lastTaskResult;
  }

  TaskStatus getStatus() {
    return status;
  }

  void setStatus(TaskStatus status) {
    this.status = status;
  }

  /**
   * Cancels tasks and sets status to 'Hold'
   * @see TaskStatus
   * @see com.bot.worker.cli.Command.hold
   */
  void putOnHold() {
    future.cancel(true);
    setStatus(TaskStatus.Hold);
  }

  public TaskConfig getConfig() {
    return config;
  }
}
