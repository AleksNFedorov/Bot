package com.bot.worker.cli;

import com.bot.worker.EventBusComponent;
import com.bot.worker.common.Command;
import com.bot.worker.common.Constants;
import com.bot.worker.common.events.*;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.Executor;

/**
 * Created by Aleks on 11/25/16.
 */
@Singleton
public class CliProcessor extends EventBusComponent {

    private static final DateTimeFormatter DEFAULT_TIME_FORMATTER =
            DateTimeFormatter
                    .ISO_LOCAL_TIME;

    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER =
            DateTimeFormatter
                    .ISO_LOCAL_DATE;

    private static final Logger logger = LoggerFactory.getLogger(CliProcessor
            .class);

    //TODO avoid using executor
    private final Executor executor;

    @Inject
    public CliProcessor(Executor executor) {
        this.executor = executor;
    }

    @Subscribe
    void onInit(AppInitEvent init) {
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
                    System.out.println(String.format("%s, use '-help' for " +
                            "more info", e.getLocalizedMessage()));
                    displayHelpMessage();
                }

            }
        });
    }

    private void processCommand(String commandLineString)
            throws ParseException {

        CommandLine commandLine = new DefaultParser().parse(new RunOptions(),
                commandLineString.split("\\s+"));
        boolean unknownCommand = true;
        for (Command command : Command.values()) {
            if (commandLine.hasOption(command.name())) {
                processOption(command, commandLine);
                unknownCommand = false;
            }
        }

        //TODO replace with throwing exception
        if (unknownCommand) {
            System.out.println("Unknown command");
            displayHelpMessage();
        }
    }

    private void processOption(Command command, CommandLine commandLine) {
        logger.info("Processing new cli command [%s]", command.name());
        System.out.println(command.name());
        String taskId = taskOptionValueToParameter(commandLine
                .getOptionValue(command.name()));

        //TODO replace switch with map
        switch (command) {
            case help:
                displayHelpMessage();
                return;
            case status:
                post(GetStatusRequest.create(taskId));
                return;
            case hold:
                post(TaskHoldRequest.create(taskId));
                return;
            case schedule:
                post(TaskScheduleRequest.create(taskId));
                return;
            case reload:
                post(TaskConfigReloadRequest.create(taskId));
                return;
            case drop:
                post(TaskDropRequest.create(taskId));
                return;
            default:
                System.out.println(String.format("Unknown command: [%s]",
                        command.name()));
        }
    }

    //TODO refactor name
    private String taskOptionValueToParameter(@Nullable String taskId) {
        if (Constants.ALL.equalsIgnoreCase(taskId)) {
            return null;
        }
        return Strings.nullToEmpty(taskId).trim();
    }

    private void displayHelpMessage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(" ", "Available commands", new RunOptions(), "",
                false);
    }


    @Subscribe
    void onStatusResponse(GetStatusResponse response) {
        System.out.println(String.format("Tasks found : %d", response
                .getTasksInfo().size()));
        response.getTasksInfo().forEach((info) ->
                System.out.println(MoreObjects.toStringHelper("")
                        .addValue(info.getTaskName())
                        .addValue(info.getStatus())
                        .addValue(info.getResultStatus())
                        .addValue(DEFAULT_TIME_FORMATTER.format(info
                                .getResultTimestamp()))
                        .addValue(DEFAULT_DATE_FORMATTER.format(info
                                .getResultTimestamp()))
                        .toString()));
    }

    @Subscribe
    void onTaskUpdateStatus(TaskUpdateResponse event) {
        System.out.println(event.getResultMessage());
    }

    @Override
    public String getComponentName() {
        return "Command line processor";
    }
}
