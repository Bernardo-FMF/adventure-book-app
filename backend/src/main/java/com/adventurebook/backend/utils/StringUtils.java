package com.adventurebook.backend.utils;

import java.util.Locale;
import java.util.Objects;

public final class StringUtils {
    private StringUtils() {
    }

    public static String blankToNull(String value) {
        return Objects.isNull(value) || value.isBlank() ? null : value.trim();
    }

    public static String upperCaseOrNull(String value) {
        String trimmed = blankToNull(value);
        return Objects.isNull(trimmed) ? null : trimmed.toUpperCase(Locale.ROOT);
    }
}
