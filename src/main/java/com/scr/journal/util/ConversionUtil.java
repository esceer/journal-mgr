package com.scr.journal.util;

import com.scr.journal.model.PaymentDirection;
import com.scr.journal.model.PaymentType;

import java.time.LocalDate;

public class ConversionUtil {

    public static <T> String convert(T obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof Integer) {
            return Integer.toString((Integer) obj);
        } else if (obj instanceof LocalDate) {
            return DateUtil.toString((LocalDate) obj);
        } else {
            return obj.toString();
        }
    }

    public static <T> T convert(String str, Class<T> targetType) {
        if (str == null) {
            return null;
        }
        if (targetType.isAssignableFrom(Long.class)) {
            return (T) Long.valueOf(str);
        } if (targetType.isAssignableFrom(Double.class)) {
            return (T) Double.valueOf(str);
        } else if (targetType.isAssignableFrom(LocalDate.class)) {
            return (T) DateUtil.parse(str);
        } else if (targetType.isAssignableFrom(PaymentType.class)) {
            return (T) PaymentType.tryParse(str);
        } else if (targetType.isAssignableFrom(PaymentDirection.class)) {
            return (T) PaymentDirection.tryParse(str);
        }
        throw new IllegalArgumentException("Cannot convert '" + str + "' to '" + targetType);
    }

}
