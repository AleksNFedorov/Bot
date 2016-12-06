package com.bot.worker.common.events;

import com.google.common.base.Optional;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/5/16.
 */
@FreeBuilder
public interface TaskHoldEvent {

    Optional<String> getTaskName();

    class Builder extends TaskHoldEvent_Builder {

    }


}
