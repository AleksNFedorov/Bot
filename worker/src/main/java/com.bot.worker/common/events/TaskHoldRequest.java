package com.bot.worker.common.events;

import com.bot.worker.common.Constants;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Request to cancel task execution
 *
 * @author Aleks
 */
@FreeBuilder
public interface TaskHoldRequest extends TaskUpdateRequest {

  static TaskHoldRequest create() {
    return TaskHoldRequest.create(Constants.ALL);
  }

  static TaskHoldRequest create(String taskName) {
    return new TaskHoldRequest
        .Builder()
        .setTaskName(taskName)
        .build();
  }

  class Builder extends TaskHoldRequest_Builder {

  }
}
