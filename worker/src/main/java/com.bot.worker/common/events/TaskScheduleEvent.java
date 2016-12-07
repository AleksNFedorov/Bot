package com.bot.worker.common.events;

import com.google.common.base.Optional;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/6/16.
 */
@FreeBuilder
public interface TaskScheduleEvent {

    Optional<String> getTaskName();

    class Builder extends TaskScheduleEvent_Builder {

    }

}
