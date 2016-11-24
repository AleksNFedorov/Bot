package com.bot.worker.taskmanager;

import com.bot.common.ITaskGroup;
import com.bot.common.TaskResult;
import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * Created by Aleks on 11/20/16.
 */
class TaskGroup implements ITaskGroup {

    private final String groupName;
    private Map<String, TaskContext> tasks = new HashMap<>();

    TaskGroup(String groupName) {
        this.groupName = groupName;
    }

    void addTask(TaskContext task) {
        tasks.put(task.getTaskName(), task);
    }

    void addTaskResult(String taskName, TaskResult result) {
        tasks.get(taskName).setLastTaskResult(result);
    }

    @Override
    public ImmutableList<TaskResult> getResults() {
        return tasks.values().stream()
                .map(TaskContext::getLastTaskResult)
                .collect(collectingAndThen(toList(), ImmutableList::copyOf));
    }

    @Override
    public String getName() {
        return groupName;
    }
}
