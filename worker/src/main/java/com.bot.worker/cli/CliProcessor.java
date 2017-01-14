package com.bot.worker.cli;

import com.bot.worker.EventBusComponent;
import com.bot.worker.common.events.AppInitEvent;
import com.bot.worker.common.events.GetStatusRequest;
import com.bot.worker.common.events.GetStatusResponse;
import com.bot.worker.common.events.TaskConfigReloadRequest;
import com.bot.worker.common.events.TaskDropRequest;
import com.bot.worker.common.events.TaskHoldRequest;
import com.bot.worker.common.events.TaskScheduleRequest;
import com.bot.worker.common.events.TaskUpdateResponse;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    private final Executor executor;

    @Inject
    CliProcessor(Executor executor) {
        this.executor = executor;
    }

    private static void displayHelpMessage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(
                " ",
                "Available commands",
                new RunOptions(),
                "",
                false);
    }

    @Subscribe
    void onInit(AppInitEvent init) {
        executor.execute(
                () -> {
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
                    logger.warn("CLI Stopped");
                }
        );
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

        if (unknownCommand) {
            throw new IllegalArgumentException(
                    String.format("Unable to process command: [%s]", commandLineString));
        }
    }

    private void processOption(Command command, CommandLine commandLine) {
        logger.info("Processing CLI command [{}]", command.name());
        System.out.println(command.name());
        String taskId = Strings.nullToEmpty(commandLine
                .getOptionValue(command.name()));

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

}
