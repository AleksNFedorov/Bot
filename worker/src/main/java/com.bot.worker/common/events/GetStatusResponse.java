package com.bot.worker.common.events;

import com.bot.common.TaskResult;
import com.bot.worker.common.TaskStatus;
import com.google.common.collect.ImmutableList;
import java.time.LocalDateTime;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Response on {@link GetStatusRequest}
 * @author Aleks
 */
@FreeBuilder
public interface GetStatusResponse {

  ImmutableList<TaskInfo> getTasksInfo();

  @FreeBuilder
  interface TaskInfo {

    String getTaskName();

    TaskStatus getStatus();

    TaskResult.Status getResultStatus();

    LocalDateTime getResultTimestamp();

    class Builder extends GetStatusResponse_TaskInfo_Builder {}
  }

  class Builder extends GetStatusResponse_Builder {}
}
