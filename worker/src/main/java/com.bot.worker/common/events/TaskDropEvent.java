package com.bot.worker.common.events;

import com.google.common.base.Optional;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/8/16.
 */
@FreeBuilder
public interface TaskDropEvent {

    Optional<String> getTaskName();

    class Builder extends TaskDropEvent_Builder {

    }


}
