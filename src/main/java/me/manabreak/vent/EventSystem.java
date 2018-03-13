package me.manabreak.vent;

import java.util.ArrayList;
import java.util.List;

/**
 * Event system is the base class for handling all the events.
 * Currently, two kinds of event processing is supported: "normal"
 * and "immediate".
 * <p>
 * "Normal" events are all handled one by one
 * in the order they were received by the event system, and only
 * when process() is called. Events are discarded only after all
 * subscribers have processed the event.
 * <p>
 * "Immediate" events are handled immediately; the subscribers
 * are notified right away and the event is disposed after all
 * subscribers have been notified.
 *
 * @param <BaseEvent> The base type for the events. This may
 *                    be used to enforce all the event types to
 *                    inherit a common base type. This can be omitted
 *                    if no base event class is required.
 */
@SuppressWarnings("WeakerAccess")
public class EventSystem<BaseEvent> {

    private final List<Subscription> subscriptions = new ArrayList<>();
    private final List<EventQueue> queues = new ArrayList<>();
    private final List<Subscription> beforeHandlers = new ArrayList<>();
    private final List<Subscription> afterHandlers = new ArrayList<>();
    private final List<EventProcessor> beforeAnyProcessors = new ArrayList<>();
    private final List<EventProcessor> afterAnyProcessors = new ArrayList<>();

    private final List<BaseEvent> queuedEvents = new ArrayList<>();

    /**
     * Total number of queued events.
     */
    private int queueSize = 0;

    private boolean processing = false;

    private static <T> Subscription<T> getSubscription(Class<T> clazz, List<Subscription> subscriptions) {
        for (Subscription s : subscriptions) {
            if (s.clazz == clazz) //noinspection unchecked
                return s;
        }

        Subscription<T> s = new Subscription<>(clazz);
        subscriptions.add(s);
        return s;
    }

    private <T extends BaseEvent> EventQueue<T> getQueue(Class<T> clazz) {
        for (EventQueue queue : queues) {
            if (queue.clazz == clazz) {
                //noinspection unchecked
                return queue;
            }
        }

        EventQueue<T> q = new EventQueue<>(clazz);
        queues.add(q);
        return q;
    }

    /**
     * Adds a processor to be invoked before any other "normal" processors are invoked.
     *
     * @param eventType Type of the event
     * @param processor Processor to invoke
     * @param <T>       Event type
     * @param <P>       Processor type
     */
    public <T extends BaseEvent, P extends EventProcessor<T>> void before(Class<T> eventType, P processor) {
        getSubscription(eventType, beforeHandlers).eventProcessors.add(processor);
    }

    /**
     * Adds a processor to be invoked after all "normal" processors are invoked.
     *
     * @param eventType Type of the event
     * @param processor Processor to invoke
     * @param <T>       Event type
     * @param <P>       Processor type
     */
    public <T extends BaseEvent, P extends EventProcessor<T>> void after(Class<T> eventType, P processor) {
        getSubscription(eventType, afterHandlers).eventProcessors.add(processor);
    }

    /**
     * Adds a processor to be invoked before any other processing takes place.
     *
     * @param processor to invoke
     */
    public void beforeAny(EventProcessor processor) {
        beforeAnyProcessors.add(processor);
    }

    /**
     * Adds a processor to be invoked after all other processing has been done.
     *
     * @param processor to invoke
     */
    public void afterAny(EventProcessor processor) {
        afterAnyProcessors.add(processor);
    }

    /**
     * Subscribe to a certain event type.
     * <p>
     * After this method is called, the subscriber will be notified
     * of events of type T. To stop the subscriber from receiving
     * events, call unsubscribe() with the subscriber.
     *
     * @param eventType Type of the event, eg. LevelCompleteEvent.class
     * @param processor the subscriber interested of the said events
     * @param <T>       Type of the event
     * @param <P>       Type of the processor
     */
    public <T extends BaseEvent, P extends EventProcessor<T>> void subscribe(Class<T> eventType, P processor) {
        getSubscription(eventType, subscriptions).eventProcessors.add(processor);
    }

    /**
     * Unsubscribes the given subscriber from receiving any events.
     *
     * @param processor subscriber to unsubscribe
     * @param <T>       Type of the event
     */
    public <T extends BaseEvent> void unsubscribe(EventProcessor<T> processor) {
        for (Subscription s : subscriptions) {
            s.eventProcessors.remove(processor);
        }
    }

    /**
     * Posts an event and immediately notifies the subscribers.
     *
     * @param event event that should be processed
     * @param <T>   Type of the event
     */
    public <T extends BaseEvent> void postImmediate(T event) {
        //noinspection unchecked
        Subscription<T> s = getSubscription((Class<T>) event.getClass(), subscriptions);
        //noinspection unchecked
        Subscription<T> before = getSubscription((Class<T>) event.getClass(), beforeHandlers);
        //noinspection unchecked
        Subscription<T> after = getSubscription((Class<T>) event.getClass(), afterHandlers);

        for (EventProcessor eventProcessor : beforeAnyProcessors) {
            //noinspection unchecked
            eventProcessor.onEvent(event);
        }

        for (EventProcessor<T> eventProcessor : before.eventProcessors) {
            eventProcessor.onEvent(event);
        }

        for (EventProcessor<T> eventProcessor : s.eventProcessors) {
            boolean consume = eventProcessor.onEvent(event);
            if (consume) {
                break;
            }
        }

        for (EventProcessor<T> eventProcessor : after.eventProcessors) {
            eventProcessor.onEvent(event);
        }

        for (EventProcessor eventProcessor : afterAnyProcessors) {
            //noinspection unchecked
            eventProcessor.onEvent(event);
        }
    }

    /**
     * Queues an event to be processed the next time process() is called.
     *
     * @param event to queue
     * @param <T>   Type of the event
     */
    public <T extends BaseEvent> void post(T event) {
        if (processing) {
            queuedEvents.add(event);
        } else {
            //noinspection unchecked
            EventQueue queue = getQueue((Class<T>) event.getClass());
            //noinspection unchecked
            queue.events.add(event);
            queueSize++;
        }
    }

    /**
     * Processes the queued events.
     */
    public void process() {
        if (queueSize == 0) return;

        processing = true;

        for (EventQueue queue : queues) {
            Subscription<?> s = getSubscription(queue.clazz, subscriptions);
            Subscription<?> before = getSubscription(queue.clazz, beforeHandlers);
            Subscription<?> after = getSubscription(queue.clazz, afterHandlers);
            for (int j = 0, d = queue.events.size(); j < d; ++j) {
                Object event = queue.events.get(j);

                for (EventProcessor eventProcessor : beforeAnyProcessors) {
                    //noinspection unchecked
                    eventProcessor.onEvent(event);
                }

                for (EventProcessor eventProcessor : before.eventProcessors) {
                    //noinspection unchecked
                    eventProcessor.onEvent(event);
                }

                for (EventProcessor eventProcessor : s.eventProcessors) {
                    //noinspection unchecked
                    boolean consume = eventProcessor.onEvent(event);
                    if (consume) {
                        break;
                    }
                }

                for (EventProcessor eventProcessor : after.eventProcessors) {
                    //noinspection unchecked
                    eventProcessor.onEvent(event);
                }

                for (EventProcessor eventProcessor : afterAnyProcessors) {
                    //noinspection unchecked
                    eventProcessor.onEvent(event);
                }
            }
            queue.events.clear();
        }

        processing = false;
        queueSize = 0;

        for (BaseEvent event : queuedEvents) {
            post(event);
        }
        queuedEvents.clear();
    }

    /**
     * Internal representation of a subscription.
     *
     * @param <T>
     */
    private static class Subscription<T> {
        private final Class<T> clazz;
        private final List<EventProcessor<T>> eventProcessors = new ArrayList<>();

        Subscription(Class<T> clazz) {
            this.clazz = clazz;
        }
    }

    /**
     * Internal representation of an event queue.
     *
     * @param <T>
     */
    private static class EventQueue<T> {
        private final Class<T> clazz;
        private final List<T> events = new ArrayList<>();

        EventQueue(Class<T> clazz) {
            this.clazz = clazz;
        }
    }
}
