package com.projectanalytics.authentication.domain;

/**
 * Supported UI themes.
 */
public enum Theme {
    LIGHT,
    DARK;

    public static Theme fromConfigValue(String value) {
        if (value == null || value.isBlank()) {
            return LIGHT;
        }
        return Theme.valueOf(value.trim().toUpperCase());
    }

    public String toConfigValue() {
        return name().toLowerCase();
    }
}
