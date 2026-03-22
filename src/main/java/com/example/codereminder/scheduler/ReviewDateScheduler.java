package com.example.codereminder.scheduler;

import com.example.codereminder.domain.ReviewItem;
import com.example.codereminder.repository.ReviewItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReviewDateScheduler {
    private final ReviewItemRepository repository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoUpdateOverReviewDate() {
        log.info("리뷰 날짜 초과 데이터 자동 갱신");

        LocalDate today = LocalDate.now();
        List<ReviewItem> submissionsToUpdate = repository.findAllByNextReviewDateBefore(today);
        submissionsToUpdate.forEach(submission -> {
            submission.updateIfOverReviewDate(today);
            log.info("자동 갱신 대상: 유저={}, 문제={}", submission.getUserName(), submission.getProblemId());
        });

        log.info("총 {}건의 데이터가 갱신되었습니다.", submissionsToUpdate.size());
    }
}
