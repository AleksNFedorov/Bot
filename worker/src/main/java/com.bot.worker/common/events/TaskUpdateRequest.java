package com.bot.worker.common.events;

import com.google.common.base.Optional;

/**
 * Created by Aleks on 12/28/2016.
 */
interface TaskUpdateRequest {

    Optional<String> getTaskName();

}
