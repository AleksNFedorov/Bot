package com.bot.common;

import com.google.common.collect.ImmutableList;

/**
 * Created by Aleks on 11/23/16.
 */
public interface ITaskGroup {

    String getName();

    ImmutableList<TaskResult> getResults();
}
