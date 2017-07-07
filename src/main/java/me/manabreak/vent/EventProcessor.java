package me.manabreak.vent;

/**
 * Common interface for subscribers.
 * <p>
 * Each subscriber should implement this interface, defining
 * the type of the event it is interested of.
 * <p>
 * When an event is posted, the onEvent() method will be invoked
 * with the said event as an argument. The subscriber should do
 * all its stuff in this method and then return either true or false,
 * depending on whether or not the subscriber consumed the event.
 * <p>
 * If a subscriber consumes an event, subsequent subscribers will not
 * be notified about the event. However, the subscribers up to that point will
 * handle the event. This can be used to stop the processing of an event
 * under certain criteria (e.g. "BulletHitEvent" may be consumed by the
 * last thing the bullet could do damage).
 *
 * @param <T> Type of the event this subscriber is interested of.
 */
public interface EventProcessor<T> {
    /**
     * Invoked when an event is processed-
     *
     * @param event that was posted
     * @return True of this processor consumed the event, false otherwise.
     */
    boolean onEvent(T event);
}
