package com.bot.worker.cli;

import com.bot.worker.common.Command;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Created by Aleks on 11/25/16.
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

        command = Command.cancel;
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
    }
}
