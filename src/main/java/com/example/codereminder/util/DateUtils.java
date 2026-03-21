package com.example.codereminder.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {

    public static Long toTimestamp(LocalDate localDate) {
        return localDate.atStartOfDay(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli();
    }

    public static LocalDate toLocalDate(Long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of("Asia/Seoul"))
                .toLocalDate();
    }
}
