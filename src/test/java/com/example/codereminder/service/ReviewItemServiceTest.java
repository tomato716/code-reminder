package com.example.codereminder.service;

import com.example.codereminder.domain.ReviewItem;
import com.example.codereminder.dto.ReviewItemDto;
import com.example.codereminder.repository.JdbcRepository;
import com.example.codereminder.util.DateUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReviewItemServiceTest {

    @Mock
    private JdbcRepository jdbcRepository;

    @InjectMocks
    private SubmissionService submissionService;

    @Test
    @DisplayName("틀린 경우 DB에서 찾아보고 없으면 새로운 엔티티를 DB에 저장한다.")
    void saveUnresolvedNewSubmission() {
        //given
        Long timestamp = DateUtils.toTimestamp(LocalDate.now());

        ReviewItemDto unresolvedDto = new ReviewItemDto("park", 2000L, "틀렸습니다", timestamp);


        given(jdbcRepository.findByUserNameAndProblemId(unresolvedDto.getUserName(), unresolvedDto.getProblemId()))
                .willReturn(Optional.empty());

        //when
        submissionService.save(unresolvedDto);

        //then
        then(jdbcRepository).should().save(any(ReviewItem.class));
    }

    @Test
    @DisplayName("맞춘 경우 DB에서 찾아보고 없으면 저장하지 않는다.")
    void doNotSaveTheSolvedSubmission() {
        //given
        Long timestamp = DateUtils.toTimestamp(LocalDate.now());

        ReviewItemDto solvedDto = new ReviewItemDto("park", 2000L, "맞았습니다!!", timestamp);

        given(jdbcRepository.findByUserNameAndProblemId(solvedDto.getUserName(), solvedDto.getProblemId()))
                .willReturn(Optional.empty());

        //when
        submissionService.save(solvedDto);

        //then
        then(jdbcRepository).should(never()).save(any(ReviewItem.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7, 21})
    @DisplayName("틀린 경우 DB에서 엔티티를 찾고 복습일이라면 마지막 시도 날짜를 갱신한다.")
    void unresolvedAtReviewDate(int date) {
        //given
        ReviewItemDto unresolvedToday = new ReviewItemDto("park", 2000L, "틀렸습니다", DateUtils.toTimestamp(LocalDate.now()));

        Long reviewDay = DateUtils.toTimestamp(LocalDate.now().minusDays(date));
        ReviewItem reviewItem = ReviewItem.of("1","park",2000L,"틀렸습니다", reviewDay, reviewDay);
        given(jdbcRepository.findByUserNameAndProblemId(unresolvedToday.getUserName(), unresolvedToday.getProblemId()))
                .willReturn(Optional.of(reviewItem));

        //when
        submissionService.save(unresolvedToday);

        //then
        then(jdbcRepository).should().updateLastAttemptTimestamp(reviewItem.getId(), unresolvedToday.getTimestamp());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7, 21})
    @DisplayName("맞춘 경우 DB에서 엔티티를 찾고 복습일이라면 마지막 시도 날짜를 갱신한다.")
    void solvedAtReviewDate(int date) {
        //given
        ReviewItemDto solvedToday = new ReviewItemDto("park", 2000L, "맞았습니다!!", DateUtils.toTimestamp(LocalDate.now()));

        Long reviewDay = DateUtils.toTimestamp(LocalDate.now().minusDays(date));
        ReviewItem reviewItem = ReviewItem.of("1", "park", 2000L, "틀렸습니다", reviewDay, reviewDay);
        given(jdbcRepository.findByUserNameAndProblemId(solvedToday.getUserName(), solvedToday.getProblemId()))
                .willReturn(Optional.of(reviewItem));

        //when
        submissionService.save(solvedToday);

        //then
        then(jdbcRepository).should().remove(reviewItem.getId());
    }

    @ParameterizedTest
    @CsvSource({
            "5, 맞았습니다!!",
            "5, 틀렸습니다",
            "10, 맞았습니다!!",
            "10, 틀렸습니다",
    })
    @DisplayName("DB에서 엔티티를 찾고 복습일이 아니라면 아무 로직도 호출되지 않는다.")
    void isNotReviewDate(int date, String resultText) {
        //given
        ReviewItemDto todaySubmission = new ReviewItemDto("park", 2000L, resultText, DateUtils.toTimestamp(LocalDate.now()));

        Long reviewDay = DateUtils.toTimestamp(LocalDate.now().minusDays(date));
        ReviewItem reviewItem = ReviewItem.of("1", "park", 2000L, "틀렸습니다", reviewDay, reviewDay);
        given(jdbcRepository.findByUserNameAndProblemId(todaySubmission.getUserName(), todaySubmission.getProblemId()))
                .willReturn(Optional.of(reviewItem));

        //when
        submissionService.save(todaySubmission);

        //then
        then(jdbcRepository).should(never()).updateLastAttemptTimestamp(reviewItem.getId(), todaySubmission.getTimestamp());
        then(jdbcRepository).should(never()).remove(reviewItem.getId());
    }
}