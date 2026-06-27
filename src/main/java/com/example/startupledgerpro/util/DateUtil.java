package com.example.startupledgerpro.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD

    public static String format(LocalDate date) {
        return date != null ? date.format(FORMATTER) : "";
    }

    public static LocalDate parse(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return LocalDate.parse(dateStr, FORMATTER);
    }

    public static long daysUntil(LocalDate date) {
        if (date == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    public static boolean isOverdue(String dateStr) {
        LocalDate date = parse(dateStr);
        if (date == null) return false;
        return date.isBefore(LocalDate.now());
    }
}