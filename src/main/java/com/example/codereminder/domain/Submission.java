package com.example.codereminder.domain;

import com.example.codereminder.dto.SubmissionDto;
import com.example.codereminder.util.DateUtils;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Getter
@ToString
@Builder(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Submission {
    private static final List<Long> REVIEW_CYCLE = List.of(1L, 3L, 7L, 21L);

    private final String id;
    private final String userId;
    private final Long problemId;
    private final String resultText;
    private final Long timestamp;
    private final Long lastAttemptDate;

    public static Submission from(SubmissionDto dto) {
        return Submission.builder()
                .id(UUID.randomUUID().toString())
                .userId(dto.getUserId())
                .problemId(dto.getProblemId())
                .resultText(dto.getResultText())
                .timestamp(dto.getTimestamp())
                .lastAttemptDate(dto.getTimestamp())
                .build();
    }

    public static Submission of(String id, String userId, long problemId, String resultText, long timestamp, long lastAttemptDate) {
        return new Submission(id, userId, problemId, resultText, timestamp, lastAttemptDate);
    }

    public boolean isReviewDay(long dtoTimestamp) {
        LocalDate submittedDate = DateUtils.toLocalDate(timestamp);
        LocalDate today = DateUtils.toLocalDate(dtoTimestamp);

        return REVIEW_CYCLE.contains(ChronoUnit.DAYS.between(submittedDate, today));
    }
}
