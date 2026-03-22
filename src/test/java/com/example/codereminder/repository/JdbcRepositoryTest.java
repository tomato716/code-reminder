package com.example.codereminder.repository;

import com.example.codereminder.domain.ReviewItem;
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


@JdbcTest
@Import(JdbcRepository.class)
class JdbcRepositoryTest {
    private static final String SUBMISSION_ID = "1";

    @Autowired
    private JdbcRepository jdbcRepository;
    private ReviewItem reviewItem;

    @BeforeEach
    void setup() {
        Long dateOfFirstFailure = LocalDate.now().atStartOfDay(ZoneId.of("Asia/Seoul"))
                .toInstant()
                .toEpochMilli();

        reviewItem = ReviewItem.of(SUBMISSION_ID, "park", 10L, "틀렸습니다", dateOfFirstFailure, dateOfFirstFailure);

        jdbcRepository.save(reviewItem);
    }

    @AfterEach
    void afterEach() {
        jdbcRepository.remove(SUBMISSION_ID);
    }

    @Test
    @DisplayName("유저 아이디와 문제 번호로 엔티티를 찾는다.")
    void findByUserNameAndProblemId_Success() {
        //when
        Optional<ReviewItem> optionalSubmission = jdbcRepository.findByUserNameAndProblemId(reviewItem.getUserName(), reviewItem.getProblemId());

        //then
        assertThat(optionalSubmission).isPresent();
    }

    @Test
    @DisplayName("유저 아이디와 문제 번호로 찾은 엔티티가 없으면 빈 Optional을 반환한다.")
    void findByUserNameAndProblemId_Failure() {
        //when
        Optional<ReviewItem> optionalSubmission = jdbcRepository.findByUserNameAndProblemId("임의의_유저_아이디", 11111L);

        //then
        assertThat(optionalSubmission).isEmpty();
    }

    @Test
    @DisplayName("id를 통해 엔티티를 제거한다.")
    void remove() {
        //when
        jdbcRepository.remove(SUBMISSION_ID);
        Optional<ReviewItem> optionalSubmission = jdbcRepository.findByUserNameAndProblemId(reviewItem.getUserName(), reviewItem.getProblemId());

        //then
        assertThat(optionalSubmission).isEmpty();
    }

    @Test
    @DisplayName("id를 통해 마지막 제출 날짜를 업데이트한다.")
    void updateLastAttemptTimestamp() {
        //given
        Long beforeLastAttemptTimestamp = reviewItem.getLastAttemptTimestamp();

        //when
        jdbcRepository.updateLastAttemptTimestamp(SUBMISSION_ID, System.currentTimeMillis());
        Optional<ReviewItem> optionalSubmission = jdbcRepository.findByUserNameAndProblemId(reviewItem.getUserName(), reviewItem.getProblemId());

        assertThat(optionalSubmission).isPresent();

        ReviewItem updatedReviewItem = optionalSubmission.get();
        Long afterLastAttemptTimestamp = updatedReviewItem.getLastAttemptTimestamp();

        //then
        assertThat(afterLastAttemptTimestamp).isGreaterThan(beforeLastAttemptTimestamp);
    }
}
