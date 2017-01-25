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
    Command command = Command.help;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .build());

    command = Command.hold;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .hasArg()
        .argName("TASK ID")
        .build());

    command = Command.schedule;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .hasArg()
        .argName("TASK ID")
        .build());

    command = Command.status;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .hasArg()
        .argName("TASK ID")
        .build());

    command = Command.reload;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .hasArg()
        .argName("TASK ID")
        .build());

    command = Command.drop;
    addOption(Option
        .builder(command.getShortOpt())
        .longOpt(command.name())
        .desc(command.getDescription())
        .hasArg()
        .argName("TASK ID")
        .build());
  }
}
