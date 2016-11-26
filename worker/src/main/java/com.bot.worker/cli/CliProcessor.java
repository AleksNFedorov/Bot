package com.bot.worker.cli;

import com.bot.worker.EventBusComponent;
import com.bot.worker.common.Command;
import com.bot.worker.common.events.TaskUpdateEvent;
import com.bot.worker.common.events.TaskUpdateResultEvent;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Scanner;
import java.util.concurrent.Executor;

/**
 * Created by Aleks on 11/25/16.
 */
@Singleton
public class CliProcessor extends EventBusComponent {

    private static final Logger logger = LoggerFactory.getLogger(CliProcessor.class);

    @Inject
    public CliProcessor(Executor executor) {
        executor.execute(() -> {

            final Scanner scanner = new Scanner(System.in);
            while (!Thread.currentThread().isInterrupted()) {
                String command = scanner.nextLine();
                try {
                    if (!Strings.isNullOrEmpty(command)) {
                        processCommand(command);
                    }
                } catch (Exception e) {
                    logger.error("Exception during processing command ", e);
                    System.out.println(String.format("%s, use '-help' for more info", e.getLocalizedMessage()));
                }

            }
        });
    }

    private void processCommand(String commandLineString) throws ParseException {
        CommandLine commandLine = new DefaultParser().parse(new RunOptions(), commandLineString.split("\\s+"));
        boolean knownCommand = false;
        for (Command command : Command.values()) {
            if (commandLine.hasOption(command.name())) {
                processOption(command, commandLine);
                knownCommand = true;
            }
        }

        if (!knownCommand) {
            System.out.println("Unknown command");
            displayHelpMessage();
        }
    }

    private void processOption(Command command, CommandLine commandLine) {
        logger.info("Processing new cli command [%s]", command.name());

        if (command.equals(Command.help)) {
            displayHelpMessage();
            return;
        }

        String optionValue = commandLine.getOptionValue(command.name());
        TaskUpdateEvent event = new TaskUpdateEvent.Builder()
                .setCommand(command)
                .setTaskName(optionValue)
                .build();
        post(event);
    }

    private void displayHelpMessage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(" ", "Available commands", new RunOptions(), "", false);
    }

    @Subscribe
    public void onTaskUpdateStatus(TaskUpdateResultEvent event) {
        System.out.println(event.getResultMessage());
    }

    @Override
    public String getComponentName() {
        return "Command line processor";
    }
}
