package com.example.codereminder.service;

import com.example.codereminder.domain.Submission;
import com.example.codereminder.dto.SubmissionDto;
import com.example.codereminder.repository.SubmissionRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {
    private final SubmissionRepository submissionRepository;

    public void save(SubmissionDto dto) {
        Optional<Submission> findSubmission = submissionRepository.findByUserNameAndProblemId(dto.getUserName(), dto.getProblemId());
        //복습날이니?
        if (findSubmission.isEmpty()) {
            saveNewSubmission(dto);
            return;
        }

        findSubmission.ifPresent(submission -> handleReviewResult(dto, submission));
    }

    private void handleReviewResult(SubmissionDto dto, Submission submission) {
        if(submission.isReviewDay(dto.getTimestamp())){
            if(isSuccess(dto.getResultText())){
                submissionRepository.remove(submission.getId());
                log.info("복습할 문제를 바로 맞춰서 db에서 제거: {}", dto.getProblemId());
                return;
            }

            submissionRepository.updateLastAttemptTimestamp(submission.getId(), dto.getTimestamp());
            log.info("복습할 문제 다시 틀려서 db 갱신");
        }
    }

    private void saveNewSubmission(SubmissionDto dto) {
        if(!isSuccess(dto.getResultText())){
            Submission newSubmission = Submission.from(dto);
            submissionRepository.save(newSubmission);
            log.info("DB에 틀린 문제를 저장: {}", dto);
        }
    }

    private boolean isSuccess(@NotBlank String resultText) {
        return resultText.contains("맞았습니다");
    }
}
