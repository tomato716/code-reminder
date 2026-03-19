package com.example.codereminder.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@RequiredArgsConstructor
public class SubmissionDto {
    private final String userId;
    private final Long problemId;
    private final String resultText;
    private final Long timestamp;
}
