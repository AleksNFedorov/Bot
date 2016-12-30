package com.bot.worker.common.events;

import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/7/16.
 */
@FreeBuilder
public interface TaskConfigReloadRequest extends TaskUpdateRequest {

    static TaskConfigReloadRequest create(String taskName) {
        return new TaskConfigReloadRequest
                .Builder()
                .setNullableTaskName(taskName)
                .build();
    }

    class Builder extends TaskConfigReloadRequest_Builder {
    }
}
