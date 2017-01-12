package com.bot.common;

import java.util.List;

/**
 * Created by Aleks on 11/20/16.
 */
public interface TaskResultProcessor {

    void processResult(TaskResult result, List<TaskResult> groupResults);

}
