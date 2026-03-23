package com.example.codereminder.domain;

import com.example.codereminder.dto.ReviewItemDto;
import com.example.codereminder.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewItemTest {

    private ReviewItem reviewItem;

    @BeforeEach
    void setup() {
        ReviewItemDto dto = new ReviewItemDto(
                "park",
                1000L,
                "틀렸습니다",
                System.currentTimeMillis()
        );

        reviewItem = ReviewItem.from(dto);
    }

    @DisplayName("다음 복습 날짜와 같다면 성공한다.")
    @Test
    void isReviewDay_Success() {
        //given
        LocalDate dateToReview = LocalDate.now().plusDays(1);

        //when
        boolean result = reviewItem.isReviewDay(dateToReview);

        //then
        assertThat(result).isTrue();
    }

    @DisplayName("처음 복습 날짜가 아니라면 false를 반환한다")
    @Test
    void isReviewDay_Failure() {
        //given
        LocalDate dateToReview = LocalDate.now().plusDays(2);

        //when
        boolean result = reviewItem.isReviewDay(dateToReview);

        //then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("복습 날짜보다 이후의 날짜라면 갱신한다")
    void updateIfOverReviewDate_Success(){
        //given
        LocalDate beforeNextReviewDate = reviewItem.getNextReviewDate();

        //when
        LocalDate afterDate = LocalDate.now().plusDays(10);
        reviewItem.updateIfOverReviewDate(afterDate);
        LocalDate updatedNextReviewDate = reviewItem.getNextReviewDate();

        //then
        assertThat(updatedNextReviewDate).isAfter(beforeNextReviewDate);
    }

    @Test
    @DisplayName("복습 날짜보다 이전의 날짜라면 갱신하지 않는다")
    void updateIfOverReviewDate_Failure(){
        //given
        LocalDate beforeNextReviewDate = reviewItem.getNextReviewDate();

        //when
        LocalDate afterDate = LocalDate.now().minusDays(10);
        reviewItem.updateIfOverReviewDate(afterDate);
        LocalDate updatedNextReviewDate = reviewItem.getNextReviewDate();

        //then
        assertThat(updatedNextReviewDate).isEqualTo(beforeNextReviewDate);
    }

    @Test
    @DisplayName("리뷰 갱신 횟수가 4회 넘어 가면 false를 반환한다")
    void ifOverReviewLevelReturnFalse() {
        //given
        assertThat(reviewItem.updateNextReviewDate()).isTrue();

        //when
        for(int i=0; i<3; i++){
            reviewItem.updateNextReviewDate();
        }

        //then
        assertThat(reviewItem.updateNextReviewDate()).isFalse();
    }

    @Test
    @DisplayName("마지막 시도 타임스탬프를 변경한다")
    void updateLastAttemptTimestamp() {
        //given
        Long beforeTimestamp = reviewItem.getLastAttemptTimestamp();

        //when
        reviewItem.updateLastAttemptTimestamp(beforeTimestamp + 1000L);

        //then
        Long afterTimestamp = reviewItem.getLastAttemptTimestamp();
        assertThat(afterTimestamp).isGreaterThan(beforeTimestamp);
    }

    @Test
    @DisplayName("마지막 시도 날짜가 특정 날짜와 같다면 true를 반환한다")
    void isCompletedReview() {
        //given
        LocalDate today = LocalDate.now();
        Long nowTimestamp = DateUtils.toTimestamp(today);
        reviewItem.updateLastAttemptTimestamp(nowTimestamp);

        System.out.println("nowTimestamp = " + nowTimestamp);
        Long lastAttemptTimestamp = reviewItem.getLastAttemptTimestamp();

        System.out.println("lastAttemptDate = " + DateUtils.toLocalDate(lastAttemptTimestamp));

        System.out.println("today = " + today);
        //when
        boolean result = reviewItem.isCompletedReview(today);

        //then
        assertThat(result).isTrue();
    }
}
