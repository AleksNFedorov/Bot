package com.bot.worker.common.events;

import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/6/16.
 */
//TODO consider default create();
@FreeBuilder
public interface TaskScheduleRequest extends TaskUpdateRequest {

    static TaskScheduleRequest create(String taskName) {
        return new TaskScheduleRequest
                .Builder()
                .setNullableTaskName(taskName)
                .build();
    }

    class Builder extends TaskScheduleRequest_Builder {
    }
}
