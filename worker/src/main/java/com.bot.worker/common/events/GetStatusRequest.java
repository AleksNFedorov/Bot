package com.bot.worker.common.events;

import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/4/16.
 */
@FreeBuilder
public interface GetStatusRequest extends TaskUpdateRequest {

    static GetStatusRequest create(String taskName) {
        return new GetStatusRequest
                .Builder()
                .setNullableTaskName(taskName)
                .build();
    }

    class Builder extends GetStatusRequest_Builder {
    }
}
