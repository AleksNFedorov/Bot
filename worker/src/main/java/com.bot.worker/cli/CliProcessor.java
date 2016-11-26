package com.bot.worker.cli;

import com.bot.worker.EventBusComponent;
import com.bot.worker.common.TaskCommand;
import com.bot.worker.common.events.TaskUpdateEvent;
import com.bot.worker.common.events.TaskUpdateResultEvent;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Scanner;
import java.util.concurrent.Executor;

/**
 * Created by Aleks on 11/25/16.
 */
public class CliProcessor extends EventBusComponent {

    private static final Logger logger = LoggerFactory.getLogger(CliProcessor.class);

    @Inject
    public CliProcessor(Executor executor) {
        executor.execute(() -> {

            final Scanner scanner = new Scanner(System.in);
            while (!Thread.currentThread().isInterrupted()) {
                String command = scanner.nextLine();
                try {
                    processCommand(command);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void processCommand(String commandLineString) throws ParseException {
        CommandLine commandLine = new DefaultParser().parse(new RunOptions(), commandLineString.split("\\s+"));
        boolean knownCommand = false;
        for (TaskCommand command : TaskCommand.values()) {
            if (commandLine.hasOption(command.name())) {
                processOption(command, commandLine);
                knownCommand = true;
            }
        }

        if (!knownCommand) {
            System.out.println("Unknown command");
        }
    }

    private void processOption(TaskCommand command, CommandLine commandLine) {
        String optionValue = commandLine.getOptionValue(command.name());
        logger.info("Processing new cli command [%s][%s]", command.name(), optionValue);
        TaskUpdateEvent event = new TaskUpdateEvent.Builder()
//                .setTaskName("")
                .setTaskName(optionValue)
                .setCommand(command)
                .build();
        post(event);
    }

    @Subscribe
    public void onTaskUpdateStatus(TaskUpdateResultEvent event) {
        System.out.println(event.getResultMessage());
    }

    @Override
    public String getComponentName() {
        return "TaskCommand line processor";
    }
}
