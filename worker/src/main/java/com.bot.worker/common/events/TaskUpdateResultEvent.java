package com.bot.worker.common.events;

import com.bot.worker.common.TaskCommand;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/25/16.
 */
@FreeBuilder
public interface TaskUpdateResultEvent {

    TaskCommand getCommand();

    String getTaskName();

    String getResultMessage();

    class Builder extends TaskUpdateResultEvent_Builder {
    }

}
