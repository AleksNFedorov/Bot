package com.bot.worker;

import com.bot.worker.common.events.ExceptionEvent;
import com.google.common.eventbus.EventBus;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Aleks on 11/14/16.
 */
public abstract class EventBusComponent {

    private EventBus eventBus;

    public abstract String getComponentName();

    protected void post(Object event) {
        checkNotNull(event, "Event must not be null");
        eventBus.post(event);
    }

    protected void postException(Throwable exception) {
        checkNotNull(exception, "Exception must be specified");

        eventBus.post(new ExceptionEvent.Builder()
                .setComponentName(getComponentName())
                .setCause(exception)
                .build()
        );
    }

    @Inject
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
    }
}
