package com.example.codereminder.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {
    private static final String TIME_ZONE = "Asia/Seoul";

    public static Long toTimestamp(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.of(TIME_ZONE))
                .toInstant()
                .toEpochMilli();
    }

    public static LocalDate toLocalDate(Long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of(TIME_ZONE))
                .toLocalDate();
    }
}
