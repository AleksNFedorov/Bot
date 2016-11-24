package com.bot.worker.common.events;

import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/17/16.
 */
@FreeBuilder
public interface ExceptionEvent {

    String getComponentName();

    Throwable getCause();

    class Builder extends ExceptionEvent_Builder {
    }
}
