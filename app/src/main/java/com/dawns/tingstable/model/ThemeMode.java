package com.dawns.tingstable.model;

/** Persistent visual skins supported by the local app. */
public enum ThemeMode {
    LIGHT("light"),
    NIGHT("night");

    private final String id;

    ThemeMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public boolean isDark() {
        return this == NIGHT;
    }

    public ThemeMode toggle() {
        return this == LIGHT ? NIGHT : LIGHT;
    }

    public static ThemeMode fromId(String value) {
        for (ThemeMode mode : values()) {
            if (mode.id.equals(value)) return mode;
        }
        return LIGHT;
    }
}
