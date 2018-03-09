package com.scr.journal.util;

public final class ValidationUtils {

    public static <T> void ifPresent(T objToCheck, Runnable runnable) {
        if (objToCheck != null) {
            runnable.run();
        }
    }

    public static boolean isNullOrEmpty(Object... objects) {
        for (Object obj : objects) {
            if (isNullOrEmpty(obj)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNullOrEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }

        return false;
    }

    public static Throwable getRootCause(Throwable exception) {
        while (exception.getCause() != null) {
            exception = exception.getCause();
        }
        return exception;
    }

    private ValidationUtils() {
        throw new UnsupportedOperationException();
    }

}
