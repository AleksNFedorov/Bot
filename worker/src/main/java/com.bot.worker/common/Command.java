package com.bot.worker.common;

/**
 * Created by Aleks on 11/25/16.
 */
public enum Command {
    help("Show this help message", "help"),
    cancel("Try to stop task execution and put on hold", "c"),
    schedule("Schedule task for execution", "s"),
    stop("Stop application", "stop"),
    status("Show task status, put 'all' to display status for all tasks", "st");

    private String description;
    private String shortOpt;

    Command(String description, String shortOpt) {
        this.description = description;
        this.shortOpt = shortOpt;
    }

    public String getDescription() {
        return description;
    }

    public String getShortOpt() {
        return shortOpt;
    }
}
