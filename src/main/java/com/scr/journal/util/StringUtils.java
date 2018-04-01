package com.scr.journal.util;

public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException();
    }

    public static String trim(String value) {
        return value != null
                ? value.trim()
                : null;
    }

}
