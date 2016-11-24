package com.bot.common;

import com.google.common.collect.ImmutableList;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.guard.Guarded;

/**
 * Created by Aleks on 11/23/16.
 */
@Guarded
public interface ITaskGroup {

    @NotNull
    String getName();

    ImmutableList<TaskResult> getResults();
}
