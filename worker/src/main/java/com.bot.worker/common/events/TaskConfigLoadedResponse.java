package com.bot.worker.common.events;

import com.bot.common.TaskConfig;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/17/16.
 */
@FreeBuilder
public interface TaskConfigLoadedResponse {

    TaskConfig getTaskConfig();

    String getGroupName();

    String getTaskName();

    String getExecutorId();

    class Builder extends TaskConfigLoadedResponse_Builder {
    }
}
