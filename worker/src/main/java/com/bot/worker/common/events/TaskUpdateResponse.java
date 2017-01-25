package com.bot.worker.common.events;

import com.bot.worker.cli.Command;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Task update request processing result.
 */
@FreeBuilder
public interface TaskUpdateResponse {

  Command getCommand();

  String getTaskName();

  String getResultMessage();

  class Builder extends TaskUpdateResponse_Builder {

  }
}
