package com.bot.worker.common.events;

import com.bot.worker.common.Constants;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/8/16.
 */
@FreeBuilder
public interface TaskDropRequest extends TaskUpdateRequest {

    static TaskDropRequest create() {
        return TaskDropRequest.create(Constants.ALL);
    }

    static TaskDropRequest create(String taskName) {
        return new TaskDropRequest
                .Builder()
                .setTaskName(taskName)
                .build();
    }

    class Builder extends TaskDropRequest_Builder {
    }
}
