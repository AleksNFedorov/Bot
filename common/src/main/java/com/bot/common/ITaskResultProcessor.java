package com.bot.common;

import net.sf.oval.constraint.NotNull;
import net.sf.oval.guard.Guarded;

/**
 * Created by Aleks on 11/20/16.
 */
@Guarded
public interface ITaskResultProcessor {

    void processResult(@NotNull TaskResult result, @NotNull ITaskGroup group);

}
