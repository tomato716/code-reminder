package com.example.codereminder.domain;

import com.example.codereminder.dto.ReviewItemDto;
import com.example.codereminder.util.DateUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    public static ReviewItem from(ReviewItemDto dto) {
        return ReviewItem.builder()
                .id(UUID.randomUUID().toString())
                .userName(dto.getUserName())
                .problemId(dto.getProblemId())
                .resultText(dto.getResultText())
                .timestamp(dto.getTimestamp())
                .lastAttemptTimestamp(dto.getTimestamp())
                .reviewLevel(1)
                .nextReviewDate(LocalDate.now().plusDays(1))
                .build();
    }

    public boolean isReviewDay(LocalDate date) {
        return nextReviewDate.isEqual(date);
    }

    public void updateIfOverReviewDate(LocalDate date) {
        if (date.isAfter(nextReviewDate)) {
            nextReviewDate = date;
        }
    }

    public boolean updateNextReviewDate() {
        reviewLevel++;

        if(reviewLevel < REVIEW_CYCLE.size()) {
            nextReviewDate = nextReviewDate.plusDays(REVIEW_CYCLE.get(reviewLevel));
            return true;
        }

        return false;
    }

    public void updateLastAttemptTimestamp(Long timestampOfReviewItemDto) {
        lastAttemptTimestamp = timestampOfReviewItemDto;
    }

    public boolean isCompletedReview(LocalDate date) {
        return DateUtils.toLocalDate(lastAttemptTimestamp).isEqual(date);
    }
}
