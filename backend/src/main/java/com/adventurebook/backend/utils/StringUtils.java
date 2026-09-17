package com.adventurebook.backend.utils;

import java.util.Locale;
import java.util.Objects;

public final class StringUtils {
    private StringUtils() {
    }

    // A value made only of whitespace carries no information, so it is treated the same as a missing one.
    public static String blankToNull(String value) {
        return Objects.isNull(value) || value.isBlank() ? null : value.trim();
    }

    // Locale.ROOT keeps the result stable regardless of the machine's language, e.g. "i" never becomes a dotted "İ".
    public static String upperCaseOrNull(String value) {
        String trimmed = blankToNull(value);
        return Objects.isNull(trimmed) ? null : trimmed.toUpperCase(Locale.ROOT);
    }
}
