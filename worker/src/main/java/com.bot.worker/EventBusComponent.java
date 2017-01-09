package com.bot.worker;

import com.bot.worker.common.events.ExecutionExceptionEvent;
import com.google.common.eventbus.EventBus;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.guard.Guarded;

import javax.inject.Inject;

/**
 * Created by Aleks on 11/14/16.
 */
@Guarded
public abstract class EventBusComponent {

    private EventBus eventBus;

    public abstract String getComponentName();

    protected void post(@NotNull Object event) {
        eventBus.post(event);
    }

    //TODO replace SubscriberExceptionHndler
    //http://google.github.io/guava/releases/snapshot/api/docs/com/google/common/eventbus/SubscriberExceptionHandler.html
    protected void postException(@NotNull Throwable exception) {

        eventBus.post(new ExecutionExceptionEvent.Builder()
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
