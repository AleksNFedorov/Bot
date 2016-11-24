package com.bot.worker;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by Aleks on 11/6/16.
 */
public class EventBusTest {

    public static void main(String... args) {

/*
        EventBusTest test = new EventBusTest();

        EventBus bus = new EventBus();

        bus.register(test);

        Event event = test.new ChildEvent();

        bus.post(event   );
*/

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                EventBus bus = getProvider(EventBus.class).get();

                bus.register(new EventBusTest().new Subscriber());

            }
        });

        EventBus bus = injector.getInstance(EventBus.class);

        bus.post(new EventBusTest.ChildEvent());


    }

    public static class Event {
    }

    public static class ChildEvent extends Event {
    }

    class Subscriber {
        @Subscribe
        public void processChildEvent(ChildEvent event) {
            System.out.println("Child event");
        }

        @Subscribe
        public void processEvent(Event event) {
            System.out.println("Event");
        }
    }


}
