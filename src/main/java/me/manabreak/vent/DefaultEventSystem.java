package me.manabreak.vent;

/**
 * Default singleton event system for generic events.
 */
public final class DefaultEventSystem {

    /**
     * Singleton instance
     */
    private static final EventSystem instance = new EventSystem<>();

    /**
     * Retrieves the singleton instance of the default event system
     *
     * @return singleton instance
     */
    public static EventSystem getInstance() {
        return instance;
    }

    private DefaultEventSystem() {

    }
}
