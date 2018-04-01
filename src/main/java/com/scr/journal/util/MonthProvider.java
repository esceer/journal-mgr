package com.scr.journal.util;

import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MonthProvider {

    private static final List<Month> CALENDAR_YEAR_MONTH = Collections.unmodifiableList(
            Arrays.asList(
                    Month.JANUARY, Month.FEBRUARY,
                    Month.MARCH, Month.APRIL, Month.MAY,
                    Month.JUNE, Month.JULY, Month.AUGUST,
                    Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER,
                    Month.DECEMBER));

    private final List<Month> months;

    private MonthProvider(List<Month> months) {
        this.months = new ArrayList<>(months);
    }

    public static MonthProvider create() {
        return new MonthProvider(CALENDAR_YEAR_MONTH);
    }

    public void shiftMonths(int distance) {
        Collections.rotate(months, distance);
    }

    public Month first() {
        return months.get(0);
    }

    public Month second() {
        return months.get(1);
    }

    public Month third() {
        return months.get(2);
    }

    public Month fourth() {
        return months.get(3);
    }

    public Month fifth() {
        return months.get(4);
    }

    public Month sixth() {
        return months.get(5);
    }

    public Month seventh() {
        return months.get(6);
    }

    public Month eighth() {
        return months.get(7);
    }

    public Month ninth() {
        return months.get(8);
    }

    public Month tenth() {
        return months.get(9);
    }

    public Month eleventh() {
        return months.get(10);
    }

    public Month twelfth() {
        return months.get(11);
    }

}
