package com.example.codereminder.service;

import com.example.codereminder.domain.ReviewItem;
import com.example.codereminder.dto.ReviewItemDto;
import com.example.codereminder.repository.ReviewItemRepository;
import com.example.codereminder.util.DateUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewItemService {
    private final ReviewItemRepository repository;

    public void save(ReviewItemDto dto) {
        repository.findByUserNameAndProblemId(dto.getUserName(), dto.getProblemId())
                .ifPresentOrElse(
                        submission -> handleReviewResult(dto, submission),
                        () -> saveNewSubmission(dto)
                );
    }

    private void handleReviewResult(ReviewItemDto dto, ReviewItem reviewItem) {
        LocalDate dateOfReviewItemDto = DateUtils.toLocalDate(dto.getTimestamp());

        if (reviewItem.isReviewDay(dateOfReviewItemDto)) {
            if (isSuccess(dto.getResultText()) || !reviewItem.updateNextReviewDate()) {
                repository.delete(reviewItem);
                log.debug("복습할 문제 db에서 제거: {}", dto.getProblemId());
                return;
            }
            reviewItem.updateLastAttemptTimestamp(dto.getTimestamp());
            log.debug("복습할 문제 다시 틀려서 db 갱신");
        }
    }

    private void saveNewSubmission(ReviewItemDto dto) {
        if (!isSuccess(dto.getResultText())) {
            ReviewItem newReviewItem = ReviewItem.from(dto);
            repository.save(newReviewItem);
            log.debug("{}님의 DB에 틀린 문제를 저장: {}", newReviewItem.getUserName(), newReviewItem.getProblemId());
        }
    }

    private boolean isSuccess(@NotBlank String resultText) {
        return resultText.contains("맞았습니다");
    }

    public void updateOverReviewDate() {
        log.debug("리뷰 날짜 초과 데이터 자동 갱신");

        LocalDate today = LocalDate.now();
        List<ReviewItem> submissionsToUpdate = repository.findAllByNextReviewDateBefore(today);
        submissionsToUpdate.forEach(submission -> {
            submission.updateIfOverReviewDate(today);
            log.debug("자동 갱신 대상: 유저={}, 문제={}", submission.getUserName(), submission.getProblemId());
        });

        log.debug("총 {}건의 데이터가 갱신되었습니다.", submissionsToUpdate.size());
    }

    @Transactional(readOnly = true)
    public List<ReviewItem> getReviewItems(String userName, LocalDate date) {
        return repository.findByUserNameAndNextReviewDate(userName, date);
    }
}
