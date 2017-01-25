package com.bot.worker.cli;

import com.bot.worker.common.Annotations.TaskConfigFile;
import com.bot.worker.common.Annotations.ThreadsCount;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.guard.Guarded;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

/**
 * CLI Google guice module, initializes required bindings for CLI
 *
 * @see <a href="https://commons.apache.org/proper/commons-cli/">Commons CLI</a>
 * @see <a href="https://github.com/google/guice">Google guice</a>
 * @author Aleks
 */
@Guarded
public class CliModule extends AbstractModule {

  private static final int THREADS_COUNT_DEFAULT = 20;

  private final CommandLine commandLine;

  /**
   * @param commandLineArguments boot loading parameters, used to be arguments from main method
   * @throws ParseException in case of unable to parse arguments
   */
  public CliModule(@NotNull final String[] commandLineArguments)
      throws ParseException {

    commandLine = new DefaultParser().parse(new
        BootOptions(), commandLineArguments);
  }

  protected void configure() {
    bind(CliProcessor.class).asEagerSingleton();
    bind(Executor.class).toInstance(Executors.newSingleThreadExecutor());
  }

  @TaskConfigFile
  @Provides
  String providesPathToConfigFile() {
    return commandLine.getOptionValue(BootOptions.TASK_CONFIG_FILE_PATH);
  }

  @ThreadsCount
  @Provides
  Integer providesThreadsCount() {
    if (commandLine.hasOption(BootOptions.THREADS_COUNT)) {
      return Integer.parseInt(commandLine.getOptionValue(BootOptions.THREADS_COUNT));
    }
    return THREADS_COUNT_DEFAULT;
  }
}
