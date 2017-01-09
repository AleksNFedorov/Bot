package com.bot.worker.common.events;

import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/8/16.
 */
//TODO consider default create();
@FreeBuilder
public interface TaskDropRequest extends TaskUpdateRequest {

    static TaskDropRequest create(String taskName) {
        return new TaskDropRequest
                .Builder()
                .setNullableTaskName(taskName)
                .build();
    }

    class Builder extends TaskDropRequest_Builder {
    }
}
