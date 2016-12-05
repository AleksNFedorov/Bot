package com.bot.worker.common.events;

import com.google.common.base.Optional;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 12/4/16.
 */
@FreeBuilder
public interface GetStatusRequest {

    Optional<String> getTaskName();

    class Builder extends GetStatusRequest_Builder {

    }

}
