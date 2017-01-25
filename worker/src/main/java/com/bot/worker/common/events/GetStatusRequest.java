package com.bot.worker.common.events;

import com.bot.worker.common.Constants;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Requests status info for given task or all tasks
 */
@FreeBuilder
public interface GetStatusRequest extends TaskUpdateRequest {

  static GetStatusRequest create() {
    return GetStatusRequest.create(Constants.ALL);
  }

  static GetStatusRequest create(String taskName) {
    return new GetStatusRequest
        .Builder()
        .setTaskName(taskName)
        .build();
  }

  class Builder extends GetStatusRequest_Builder {}
}
