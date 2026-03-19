package com.example.codereminder.domain;

import com.example.codereminder.dto.SubmissionDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@ToString
@Builder(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Submission {
    private final String id;
    private final String userId;
    private final Long problemId;
    private final String resultText;
    private final LocalDateTime timestamp;

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
