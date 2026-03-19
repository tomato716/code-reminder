package com.example.codereminder.domain;

import com.example.codereminder.dto.SubmissionDto;
import lombok.*;

import java.util.UUID;

@Getter
@ToString
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Submission {
    private final String id;
    private final String userId;
    private final String problemId;
    private final String resultText;
    private final String timestamp;

    public static Submission from(SubmissionDto dto) {
        return Submission.builder()
                .id(UUID.randomUUID().toString())
                .userId(dto.getUserId())
                .problemId(dto.getProblemId())
                .resultText(dto.getResultText())
                .timestamp(dto.getTimestamp())
                .build();
    }
}
