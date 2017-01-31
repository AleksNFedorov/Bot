package com.bot.worker.cli;

/**
 * Enum with all CLI commands
 *
 * @see CliProcessor
 * @author Aleks
 */
public enum Command {
  // use upper case for enumerated constants 
  help("Show this help message", "help"),
  hold("Put task on hold, put 'all' to put on hold all tasks", "hd"),
  schedule("Schedule task for execution", "s"),
  status("Show task status, put 'all' to display status for all tasks", "st"),
  drop("Drop task and remove all results, put 'all' drop all tasks", "dp"),
  reload("Reload task from config, put 'all' to reload all tasks", "rd");

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
