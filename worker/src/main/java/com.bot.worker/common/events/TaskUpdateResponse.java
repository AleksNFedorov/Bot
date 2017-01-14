package com.bot.worker.common.events;

import com.bot.worker.cli.Command;
import org.inferred.freebuilder.FreeBuilder;

/**
 * Created by Aleks on 11/25/16.
 */
@FreeBuilder
public interface TaskUpdateResponse {

  Command getCommand();

  String getTaskName();

  String getResultMessage();

  class Builder extends TaskUpdateResponse_Builder {

  }
}
