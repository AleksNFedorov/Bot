package com.bot.worker;

import com.google.common.eventbus.EventBus;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.guard.Guarded;

import javax.inject.Inject;

/**
 * Created by Aleks on 11/14/16.
 */
@Guarded
public abstract class EventBusComponent {

    private volatile EventBus eventBus;

    protected void post(@NotNull Object event) {
        eventBus.post(event);
    }

    @Inject
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }
}
