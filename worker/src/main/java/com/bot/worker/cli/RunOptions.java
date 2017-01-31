package com.bot.worker.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Command line interface options, available to use during runtime
 *
 * @see Command
 * @see CliProcessor
 * @author Aleks
 */
class RunOptions extends Options {

  RunOptions() {
    Command command = Command.HELP;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .build());

    command = Command.HOLD;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .hasArg()
        .argName("TASK ID")
        .build());

    command = Command.SCHEDULE;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .hasArg()
        .argName("TASK ID")
        .build());

    command = Command.STATUS;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .hasArg()
        .argName("TASK ID")
        .build());

    command = Command.RELOAD;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .hasArg()
        .argName("TASK ID")
        .build());

    command = Command.DROP;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .hasArg()
        .argName("TASK ID")
        .build());
  }
}
