package me.manabreak.vent;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DefaultEventSystemTest {

    @Test
    public void testGetInstance() {
        assertNotNull(DefaultEventSystem.getInstance());
    }

}