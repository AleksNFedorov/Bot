package com.bot.worker.common.events;

import com.bot.common.TaskConfig;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/17/16.
 */
@FreeBuilder
public interface TaskConfigLoadedResponse {

  static TaskConfigLoadedResponse create(String groupName, TaskConfig config) {
    return new TaskConfigLoadedResponse.Builder()
        .setGroupName(groupName)
        .setTaskConfig(config)
        .build();
  }

  TaskConfig getTaskConfig();

  String getGroupName();

  class Builder extends TaskConfigLoadedResponse_Builder {

  }
}
