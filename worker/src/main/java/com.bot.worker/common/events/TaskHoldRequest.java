package com.bot.worker.common.events;

import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/5/16.
 */
//TODO consider default create();
@FreeBuilder
public interface TaskHoldRequest extends TaskUpdateRequest {

    static TaskHoldRequest create(String taskName) {
        return new TaskHoldRequest
                .Builder()
                .setNullableTaskName(taskName)
                .build();
    }

    class Builder extends TaskHoldRequest_Builder {
    }
}
