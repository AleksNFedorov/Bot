package com.bot.worker.common.events;

import com.bot.worker.common.Command;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/25/16.
 */
@FreeBuilder
public interface TaskUpdateEvent {

    Command getCommand();

    String getTaskName();

    class Builder extends TaskUpdateEvent_Builder {
    }

}
