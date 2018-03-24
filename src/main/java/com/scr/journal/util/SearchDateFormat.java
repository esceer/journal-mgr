package com.scr.journal.util;

public enum SearchDateFormat {
    FULL,
    SHORT;

    public static SearchDateFormat parse(String value) {
        for (SearchDateFormat format : SearchDateFormat.values()) {
            if (format.name().equalsIgnoreCase(value)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Cannot parse SearchDateFormat: '" + value + "'");
    }
}
