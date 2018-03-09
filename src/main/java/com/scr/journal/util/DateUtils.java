package com.scr.journal.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.");

    public static LocalDate parse(String dateStr) {
        return LocalDate.parse(dateStr, dateFormatter);
    }

    public static String toString(LocalDate date) {
        return date != null
                ? date.format(dateFormatter)
                : null;
    }

}
