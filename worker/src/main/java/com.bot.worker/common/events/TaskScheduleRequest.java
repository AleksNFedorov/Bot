package com.bot.worker.common.events;

import com.bot.worker.common.Constants;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/6/16.
 */
@FreeBuilder
public interface TaskScheduleRequest extends TaskUpdateRequest {

  static TaskScheduleRequest create() {
    return TaskScheduleRequest.create(Constants.ALL);
  }

  static TaskScheduleRequest create(String taskName) {
    return new TaskScheduleRequest
        .Builder()
        .setTaskName(taskName)
        .build();
  }

  class Builder extends TaskScheduleRequest_Builder {

  }
}
