package com.example.codereminder.service;

import com.example.codereminder.domain.Submission;
import com.example.codereminder.dto.SubmissionDto;
import com.example.codereminder.repository.SubmissionRepository;
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
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private SubmissionService submissionService;

    @Test
    @DisplayName("틀린 경우 DB에서 찾아보고 없으면 새로운 엔티티를 DB에 저장한다.")
    void saveUnresolvedNewSubmission() {
        //given
        Long timestamp = DateUtils.toTimestamp(LocalDate.now());

        SubmissionDto unresolvedDto = new SubmissionDto("park", 2000L, "틀렸습니다", timestamp);


        given(submissionRepository.findByUserIdAndProblemId(unresolvedDto.getUserId(), unresolvedDto.getProblemId()))
                .willReturn(Optional.empty());

        //when
        submissionService.save(unresolvedDto);

        //then
        then(submissionRepository).should().save(any(Submission.class));
    }

    @Test
    @DisplayName("맞춘 경우 DB에서 찾아보고 없으면 저장하지 않는다.")
    void doNotSaveTheSolvedSubmission() {
        //given
        Long timestamp = DateUtils.toTimestamp(LocalDate.now());

        SubmissionDto solvedDto = new SubmissionDto("park", 2000L, "맞았습니다!!", timestamp);

        given(submissionRepository.findByUserIdAndProblemId(solvedDto.getUserId(), solvedDto.getProblemId()))
                .willReturn(Optional.empty());

        //when
        submissionService.save(solvedDto);

        //then
        then(submissionRepository).should(never()).save(any(Submission.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7, 21})
    @DisplayName("틀린 경우 DB에서 엔티티를 찾고 복습일이라면 마지막 시도 날짜를 갱신한다.")
    void unresolvedAtReviewDate(int date) {
        //given
        SubmissionDto unresolvedToday = new SubmissionDto("park", 2000L, "틀렸습니다", DateUtils.toTimestamp(LocalDate.now()));

        Long reviewDay = DateUtils.toTimestamp(LocalDate.now().minusDays(date));
        Submission submission = Submission.of("1","park",2000L,"틀렸습니다", reviewDay, reviewDay);
        given(submissionRepository.findByUserIdAndProblemId(unresolvedToday.getUserId(), unresolvedToday.getProblemId()))
                .willReturn(Optional.of(submission));

        //when
        submissionService.save(unresolvedToday);

        //then
        then(submissionRepository).should().updateLastAttemptDate(submission.getId(), unresolvedToday.getTimestamp());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 7, 21})
    @DisplayName("맞춘 경우 DB에서 엔티티를 찾고 복습일이라면 마지막 시도 날짜를 갱신한다.")
    void solvedAtReviewDate(int date) {
        //given
        SubmissionDto solvedToday = new SubmissionDto("park", 2000L, "맞았습니다!!", DateUtils.toTimestamp(LocalDate.now()));

        Long reviewDay = DateUtils.toTimestamp(LocalDate.now().minusDays(date));
        Submission submission = Submission.of("1", "park", 2000L, "틀렸습니다", reviewDay, reviewDay);
        given(submissionRepository.findByUserIdAndProblemId(solvedToday.getUserId(), solvedToday.getProblemId()))
                .willReturn(Optional.of(submission));

        //when
        submissionService.save(solvedToday);

        //then
        then(submissionRepository).should().remove(submission.getId());
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
        SubmissionDto todaySubmission = new SubmissionDto("park", 2000L, resultText, DateUtils.toTimestamp(LocalDate.now()));

        Long reviewDay = DateUtils.toTimestamp(LocalDate.now().minusDays(date));
        Submission submission = Submission.of("1", "park", 2000L, "틀렸습니다", reviewDay, reviewDay);
        given(submissionRepository.findByUserIdAndProblemId(todaySubmission.getUserId(), todaySubmission.getProblemId()))
                .willReturn(Optional.of(submission));

        //when
        submissionService.save(todaySubmission);

        //then
        then(submissionRepository).should(never()).updateLastAttemptDate(submission.getId(), todaySubmission.getTimestamp());
        then(submissionRepository).should(never()).remove(submission.getId());
    }
}