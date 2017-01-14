package com.bot.worker.common.events;

import com.bot.worker.common.Constants;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/5/16.
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
