package com.example.codereminder.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void typeMapping() {
        //given
        LocalDate today = LocalDate.now();

        //when
        long timestamp = DateUtils.toTimestamp(today);
        LocalDate result = DateUtils.toLocalDate(timestamp);

        assertThat(result).isEqualTo(today);
    }
}