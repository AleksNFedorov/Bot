package com.bot.worker.common.events;

import com.bot.common.TaskConfig;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/17/16.
 */
@FreeBuilder
public interface TaskConfigLoaded {

    TaskConfig getTaskConfig();

    String getGroupName();

    String getTaskName();

    String getExecutorId();

    class Builder extends TaskConfigLoaded_Builder {
    }

}
