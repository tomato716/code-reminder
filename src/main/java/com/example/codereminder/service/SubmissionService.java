package com.example.codereminder.service;

import com.example.codereminder.domain.ReviewItem;
import com.example.codereminder.dto.ReviewItemDto;
import com.example.codereminder.repository.ReviewItemRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubmissionService {
    private final ReviewItemRepository repository;

    public void save(ReviewItemDto dto) {
        repository.findByUserNameAndProblemId(dto.getUserName(), dto.getProblemId())
                .ifPresentOrElse(
                        submission -> handleReviewResult(dto, submission),
                        () -> saveNewSubmission(dto)
                );
    }

    private void handleReviewResult(ReviewItemDto dto, ReviewItem reviewItem) {
        if (reviewItem.isReviewDay(LocalDate.now())) {
            if (isSuccess(dto.getResultText()) || !reviewItem.updateNextReviewDate()) {
                repository.delete(reviewItem);
                log.info("복습할 문제 db에서 제거: {}", dto.getProblemId());
                return;
            }
            reviewItem.updateLastAttemptTimestamp(dto.getTimestamp());
            log.info("복습할 문제 다시 틀려서 db 갱신");
        }
    }

    private void saveNewSubmission(ReviewItemDto dto) {
        if (!isSuccess(dto.getResultText())) {
            ReviewItem newReviewItem = ReviewItem.from(dto);
            ReviewItem savedReviewItem = repository.save(newReviewItem);
            log.info("{}님의 DB에 틀린 문제를 저장: {}", savedReviewItem.getUserName(), savedReviewItem.getProblemId());
        }
    }

    private boolean isSuccess(@NotBlank String resultText) {
        return resultText.contains("맞았습니다");
    }
}
