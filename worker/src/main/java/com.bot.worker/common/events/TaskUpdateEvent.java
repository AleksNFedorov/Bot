package com.bot.worker.common.events;

import com.bot.worker.common.TaskCommand;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/25/16.
 */
@FreeBuilder
public interface TaskUpdateEvent {

    TaskCommand getCommand();

    String getTaskName();

    class Builder extends TaskUpdateEvent_Builder {
    }

}
