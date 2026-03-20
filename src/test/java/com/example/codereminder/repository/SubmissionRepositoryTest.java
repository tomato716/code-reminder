package com.example.codereminder.repository;

import com.example.codereminder.domain.Submission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@JdbcTest
@Import(SubmissionRepository.class)
class SubmissionRepositoryTest {
    private static final String SUBMISSION_ID = "1";

    @Autowired
    private SubmissionRepository submissionRepository;
    private Submission submission;

    @BeforeEach
    void setup() {
        long dateOfFirstFailure = LocalDate.now().atStartOfDay(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli();

        submission = Submission.of(SUBMISSION_ID, "park", 10L, "틀렸습니다", dateOfFirstFailure, dateOfFirstFailure);

        submissionRepository.save(submission);
    }

    @AfterEach
    void afterEach() {
        submissionRepository.remove(SUBMISSION_ID);
    }

    @Test
    @DisplayName("유저 아이디와 문제 번호로 엔티티를 찾는다.")
    void findByUserIdAndProblemId_Success() {
        //when
        Optional<Submission> optionalSubmission = submissionRepository.findByUserIdAndProblemId(submission.getUserId(), submission.getProblemId());

        //then
        assertThat(optionalSubmission).isPresent();
    }

    @Test
    @DisplayName("유저 아이디와 문제 번호로 찾은 엔티티가 없으면 빈 Optional을 반환한다.")
    void findByUserIdAndProblemId_Failure() {
        //when
        Optional<Submission> optionalSubmission = submissionRepository.findByUserIdAndProblemId("임의의_유저_아이디", 11111L);

        //then
        assertThat(optionalSubmission).isEmpty();
    }

    @Test
    @DisplayName("id를 통해 엔티티를 제거한다.")
    void remove() {
        //when
        submissionRepository.remove(SUBMISSION_ID);
        Optional<Submission> optionalSubmission = submissionRepository.findByUserIdAndProblemId(submission.getUserId(), submission.getProblemId());

        //then
        assertThat(optionalSubmission).isEmpty();
    }

    @Test
    @DisplayName("id를 통해 마지막 제출 날짜를 업데이트한다.")
    void updateLastAttemptDate() {
        //given
        Long beforeLastAttemptDate = submission.getLastAttemptDate();

        //when
        submissionRepository.updateLastAttemptDate(SUBMISSION_ID);
        Optional<Submission> optionalSubmission = submissionRepository.findByUserIdAndProblemId(submission.getUserId(), submission.getProblemId());

        assertThat(optionalSubmission).isPresent();

        Submission updatedSubmission = optionalSubmission.get();
        Long afterLastAttemptDate = updatedSubmission.getLastAttemptDate();

        //then
        assertThat(afterLastAttemptDate).isGreaterThan(beforeLastAttemptDate);
    }
}
