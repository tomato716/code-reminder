package com.example.codereminder.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {

    public static long toTimestamp(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli();
    }

    public static LocalDate toLocalDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate();
    }
}
