package com.bot.worker.common.events;

/**
 * Ancestor for all events which request task(s) update
 * @author Aleks
 */
interface TaskUpdateRequest {

  String getTaskName();

}
