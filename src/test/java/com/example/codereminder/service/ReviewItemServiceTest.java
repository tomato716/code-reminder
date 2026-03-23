package com.example.codereminder.service;

import com.example.codereminder.domain.ReviewItem;
import com.example.codereminder.dto.ReviewItemDto;
import com.example.codereminder.repository.ReviewItemRepository;
import com.example.codereminder.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReviewItemServiceTest {

    @Mock
    private ReviewItemRepository repository;

    @InjectMocks
    private SubmissionService submissionService;
    private ReviewItem reviewItem;

    @BeforeEach
    void setup() {
        ReviewItemDto dto = new ReviewItemDto(
                "park",
                2000L,
                "틀렸습니다",
                System.currentTimeMillis()
        );

        reviewItem = ReviewItem.from(dto);
    }

    @Test
    @DisplayName("틀린 경우 DB에서 찾아보고 없으면 새로운 엔티티를 DB에 저장한다.")
    void saveUnresolvedNewSubmission() {
        //given
        ReviewItemDto unresolvedDto = new ReviewItemDto(
                "kim",
                5555L,
                "틀렸습니다",
                System.currentTimeMillis()
        );
        given(repository.findByUserNameAndProblemId(unresolvedDto.getUserName(), unresolvedDto.getProblemId()))
                .willReturn(Optional.empty());

        //when
        submissionService.save(unresolvedDto);

        //then
        then(repository).should().save(any(ReviewItem.class));
    }

    @Test
    @DisplayName("맞춘 경우 DB에서 찾아보고 없으면 저장하지 않는다.")
    void doNotSaveTheSolvedSubmission() {
        //given
        Long timestamp = DateUtils.toTimestamp(LocalDate.now());

        ReviewItemDto solvedDto = new ReviewItemDto("park", 2000L, "맞았습니다!!", timestamp);

        given(repository.findByUserNameAndProblemId(solvedDto.getUserName(), solvedDto.getProblemId()))
                .willReturn(Optional.empty());

        //when
        submissionService.save(solvedDto);

        //then
        then(repository).should(never()).save(any(ReviewItem.class));
    }

    @Test
    @DisplayName("틀린 경우 DB에서 엔티티를 찾고 복습일이라면 마지막 시도날짜를 갱신하고 복습일을 갱신한다.")
    void unresolvedAtReviewDate() {
        //given
        LocalDate reviewDate = LocalDate.now().plusDays(1);

        ReviewItemDto unresolvedToday = new ReviewItemDto(
                "park",
                2000L,
                "틀렸습니다",
                DateUtils.toTimestamp(reviewDate)
        );

        assertThat(reviewItem.isReviewDay(reviewDate)).isTrue();

        given(repository.findByUserNameAndProblemId(unresolvedToday.getUserName(), unresolvedToday.getProblemId()))
                .willReturn(Optional.of(reviewItem));

        //when
        submissionService.save(unresolvedToday);

        //then
        assertAll(
                () -> assertThat(reviewItem.getLastAttemptTimestamp()).isEqualTo(unresolvedToday.getTimestamp()),
                () -> assertThat(reviewItem.getNextReviewDate()).isAfter(reviewDate)
        );
    }

    @Test
    @DisplayName("맞춘 경우 DB에서 엔티티를 찾고 복습일이라면 DB에서 제거한다.")
    void solvedAtReviewDate() {
        //given
        LocalDate reviewDate = LocalDate.now().plusDays(1);

        ReviewItemDto solvedToday = new ReviewItemDto(
                reviewItem.getUserName(),
                reviewItem.getProblemId(),
                "맞았습니다!!",
                DateUtils.toTimestamp(reviewDate)
        );

        assertThat(reviewItem.isReviewDay(reviewDate)).isTrue();
        given(repository.findByUserNameAndProblemId(solvedToday.getUserName(), solvedToday.getProblemId()))
                .willReturn(Optional.of(reviewItem));

        //when
        submissionService.save(solvedToday);

        //then
        then(repository).should().delete(reviewItem);
    }

    @ParameterizedTest
    @ValueSource(strings = {"맞았습니다!!", "틀렸습니다"})
    @DisplayName("DB에서 엔티티를 찾고 복습일이 아니라면 엔티티의 마지막 시도 날짜, 다음 복습일을 업데이트 하지 않고 DB에서 삭제도 되지 않는다")
    void isNotReviewDate(String resultText) {
        //given
        LocalDate reviewDate = LocalDate.now().plusDays(5);

        ReviewItemDto todaySubmission = new ReviewItemDto(
                "park",
                2000L,
                resultText,
                DateUtils.toTimestamp(reviewDate)
        );

        long beforeLastAttemptTimestamp = reviewItem.getLastAttemptTimestamp();
        LocalDate beforeNextReviewDate = reviewItem.getNextReviewDate();

        assertThat(reviewItem.isReviewDay(reviewDate)).isFalse();

        given(repository.findByUserNameAndProblemId(todaySubmission.getUserName(), todaySubmission.getProblemId()))
                .willReturn(Optional.of(reviewItem));

        //when
        submissionService.save(todaySubmission);

        //then
        assertAll(
                () -> assertThat(reviewItem.getNextReviewDate()).isEqualTo(beforeNextReviewDate),
                () -> assertThat(reviewItem.getLastAttemptTimestamp()).isEqualTo(beforeLastAttemptTimestamp),
                () -> then(repository).should(never()).delete(reviewItem)
        );
    }

    @Test
    @DisplayName("제출 틀린 경우 최종 복습날이라면 DB에서 삭제한다")
    void unresolveAtLastReviewDate() {
        //given
        for(int i=0; i<3;i++){
            reviewItem.updateNextReviewDate();
        }

        LocalDate LastReviewDate = reviewItem.getNextReviewDate();

        ReviewItemDto todaySubmission = new ReviewItemDto(
                "park",
                2000L,
                "틀렸습니다",
                DateUtils.toTimestamp(LastReviewDate)
        );

        given(repository.findByUserNameAndProblemId(todaySubmission.getUserName(), todaySubmission.getProblemId()))
                .willReturn(Optional.of(reviewItem));


        //when
        submissionService.save(todaySubmission);

        //then
        then(repository).should().delete(reviewItem);
    }
}
