package com.dawns.tingstable.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThemeModeTest {
    @Test
    public void defaultsToLightForUnknownValues() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromId("unknown"));
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromId(null));
    }

    @Test
    public void modesHaveStableIdsAndDarkState() {
        assertEquals("light", ThemeMode.LIGHT.id());
        assertEquals("night", ThemeMode.NIGHT.id());
        assertFalse(ThemeMode.LIGHT.isDark());
        assertTrue(ThemeMode.NIGHT.isDark());
    }

    @Test
    public void toggleReturnsTheOtherSkin() {
        assertEquals(ThemeMode.NIGHT, ThemeMode.LIGHT.toggle());
        assertEquals(ThemeMode.LIGHT, ThemeMode.NIGHT.toggle());
    }
}
