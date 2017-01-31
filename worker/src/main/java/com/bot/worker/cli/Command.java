package com.bot.worker.cli;

/**
 * Enum with all CLI commands
 *
 * @see CliProcessor
 * @author Aleks
 */
public enum Command {
  HELP("Show this HELP message", "help", "help"),
  HOLD("Put task on HOLD, put 'all' to put on HOLD all tasks", "hd", "hold"),
  SCHEDULE("Schedule task for execution", "s", "schedule"),
  STATUS("Show task STATUS, put 'all' to display STATUS for all tasks", "st","status"),
  DROP("Drop task and remove all results, put 'all' DROP all tasks", "dp", "drop"),
  RELOAD("Reload task from config, put 'all' to RELOAD all tasks", "rd", "reload");

  private String description;
  private String shortOpt;
  private String longOpt;

  Command(String description, String shortOpt, String longOpt) {
    this.description = description;
    this.shortOpt = shortOpt;
    this.longOpt = longOpt;
  }

  public String getDescription() {
    return description;
  }

  public String getShortOpt() {
    return shortOpt;
  }

  public String getLongOpt() {
    return longOpt;
  }
}
