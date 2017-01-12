package com.bot.worker.common.events;

import com.bot.worker.common.Constants;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/7/16.
 */
@FreeBuilder
public interface TaskConfigReloadRequest extends TaskUpdateRequest {

    static TaskConfigReloadRequest create() {
        return TaskConfigReloadRequest.create(Constants.ALL);
    }

    static TaskConfigReloadRequest create(String taskName) {
        return new TaskConfigReloadRequest
                .Builder()
                .setTaskName(taskName)
                .build();
    }

    class Builder extends TaskConfigReloadRequest_Builder {
    }
}
