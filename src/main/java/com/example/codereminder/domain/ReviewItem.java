package com.example.codereminder.domain;

import com.example.codereminder.dto.SubmissionDto;
import com.example.codereminder.util.DateUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@ToString
@Builder(access = AccessLevel.PRIVATE)
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewItem {
    private static final List<Long> REVIEW_CYCLE = List.of(0L, 1L, 2L, 4L, 14L);

    @Id
    private String id;
    @Column(nullable = false)
    private String userName;
    @Column(nullable = false)
    private Long problemId;
    private String resultText;
    private Long timestamp;
    private Long lastAttemptTimestamp;
    private int reviewLevel;
    private LocalDate nextReviewDate;

    public static ReviewItem from(SubmissionDto dto) {
        return ReviewItem.builder()
                .id(UUID.randomUUID().toString())
                .userName(dto.getUserName())
                .problemId(dto.getProblemId())
                .resultText(dto.getResultText())
                .timestamp(dto.getTimestamp())
                .lastAttemptTimestamp(dto.getTimestamp())
                .reviewLevel(1)
                .nextReviewDate(DateUtils.toLocalDate(dto.getTimestamp()).plusDays(1))
                .build();
    }

    public boolean isReviewDay() {
        return nextReviewDate.isEqual(LocalDate.now());
//        LocalDate submittedDate = DateUtils.toLocalDate(timestamp);
//        LocalDate today = DateUtils.toLocalDate(dtoTimestamp);
//
//        return REVIEW_CYCLE.contains(ChronoUnit.DAYS.between(submittedDate, today));
    }

    public void updateIfOverReviewDate(LocalDate today) {
        if (today.isAfter(nextReviewDate)) {
            nextReviewDate = LocalDate.now();
        }
    }

    public void updateNextReviewDate() {
        if(reviewLevel < REVIEW_CYCLE.size()) {
            nextReviewDate = nextReviewDate.plusDays(REVIEW_CYCLE.get(reviewLevel));
        }
    }

    public void updateLastAttemptTimestamp(Long timestampOfSubmissionDto) {
        lastAttemptTimestamp = timestampOfSubmissionDto;
    }

    public void updateReviewLevel() {
        reviewLevel++;
    }

    public boolean isLastReviewLevel() {
        return reviewLevel >= REVIEW_CYCLE.size();
    }

    public boolean isCompletedReview(LocalDate today) {
        return DateUtils.toLocalDate(lastAttemptTimestamp) == today;
    }
}
