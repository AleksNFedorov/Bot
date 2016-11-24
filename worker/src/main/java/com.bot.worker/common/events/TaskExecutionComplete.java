package com.bot.worker.common.events;

import com.bot.common.TaskResult;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/19/16.
 */
@FreeBuilder
public interface TaskExecutionComplete {

    String getGroupName();

    String getTaskName();

    TaskResult getTaskResult();

    class Builder extends TaskExecutionComplete_Builder {
    }

}
