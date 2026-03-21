package com.example.codereminder.domain;

import com.example.codereminder.util.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class SubmissionTest {

    @DisplayName("복습 날짜가 1, 3, 7, 21에 해당한다면 성공한다.")
    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7, 21})
    void isReviewDay_Success(int reviewDay) {
        //given
        LocalDate now = LocalDate.now();
        long todayTimestamp = DateUtils.toTimestamp(now);

        Submission submission = Submission.of("1", "park", 10L, "틀렸습니다", todayTimestamp, todayTimestamp);
        LocalDate dateToReview = now.plusDays(reviewDay);

        //when
        boolean result = submission.isReviewDay(DateUtils.toTimestamp(dateToReview));

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("복습 날짜가 1, 3, 7, 21에 해당하지 않다면 실패한다.")
    @ParameterizedTest
    @ValueSource(ints = {2, 5, 10, 22})
    void isReviewDay_Failure(int reviewDay) {
        //given
        LocalDate now = LocalDate.now();
        long todayTimestamp = DateUtils.toTimestamp(now);

        Submission submission = Submission.of("1", "park", 10L, "틀렸습니다", todayTimestamp, todayTimestamp);
        LocalDate dateToReview = now.plusDays(reviewDay);

        //when
        boolean result = submission.isReviewDay(DateUtils.toTimestamp(dateToReview));

        //then
        assertThat(result).isFalse();
    }
}
