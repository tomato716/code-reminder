package com.example.codereminder.service;

import com.example.codereminder.domain.Submission;
import com.example.codereminder.dto.SubmissionDto;
import com.example.codereminder.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;

    public void save(SubmissionDto dto) {
        //해당 문제가 db에 있는지 조회
        //있다면 복습날짜인지 체크
        //처음 풀었을 때 바로 맞추면 db에서 제거 후 return

        //db에 없는데 처음 풀었을 때 바로 맞추면 return

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
