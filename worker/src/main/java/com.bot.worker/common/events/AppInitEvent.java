package com.bot.worker.common.events;

import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/17/16.
 */
//TODO post as sticky
@FreeBuilder
public interface AppInitEvent {

    static AppInitEvent create() {
        return new AppInitEvent.Builder().build();
    }

    class Builder extends AppInitEvent_Builder {
    }
}
