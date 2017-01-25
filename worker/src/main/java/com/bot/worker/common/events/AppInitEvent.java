package com.bot.worker.common.events;

import org.inferred.freebuilder.FreeBuilder;

/**
 * Event which requests component initialization
 * @author Aleks
 */
@FreeBuilder
public interface AppInitEvent {

  static AppInitEvent create() {
    return new AppInitEvent.Builder().build();
  }

  class Builder extends AppInitEvent_Builder {

  }
}
