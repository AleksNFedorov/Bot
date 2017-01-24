package com.bot.worker;

import com.google.common.eventbus.EventBus;
import javax.inject.Inject;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.guard.Guarded;

/**
 * Ancestor class for linking particular component with EventBus and provide helper methods to post events
 *
 * @author Aleks
 */
@Guarded
public abstract class EventBusComponent {

  private volatile EventBus eventBus;

  protected void post(@NotNull Object event) {
    eventBus.post(event);
  }

  @Inject
  public final void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
    eventBus.register(this);
  }
}
