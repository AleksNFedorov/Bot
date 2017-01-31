package com.bot.worker.cli;

/**
 * Enum with all CLI commands
 *
 * @see CliProcessor
 * @author Aleks
 */
public enum Command {
  HELP("Show this HELP message", "help"),
  HOLD("Put task on HOLD, put 'all' to put on HOLD all tasks", "hd"),
  SCHEDULE("Schedule task for execution", "s"),
  STATUS("Show task STATUS, put 'all' to display STATUS for all tasks", "st"),
  DROP("Drop task and remove all results, put 'all' DROP all tasks", "dp"),
  RELOAD("Reload task from config, put 'all' to RELOAD all tasks", "rd");

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
