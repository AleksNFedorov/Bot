package com.bot.worker.common.events;

import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/17/16.
 */
//TODO remove
@FreeBuilder
public interface StopAppRequest {

    static StopAppRequest create() {
        return new StopAppRequest.Builder().build();
    }

    class Builder extends StopAppRequest_Builder {
    }
}
