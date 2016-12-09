package com.bot.worker.common.events;

import com.google.common.base.Optional;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/7/16.
 */
@FreeBuilder
public interface TaskConfigReloadEvent {

    Optional<String> getTaskName();

    class Builder extends TaskConfigReloadEvent_Builder {

    }


}
