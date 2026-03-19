package com.example.codereminder.dto;

import lombok.*;

@Getter
@ToString
@RequiredArgsConstructor
public class SubmissionDto {
    private final String userId;
    private final String problemId;
    private final String resultText;
    private final String timestamp;
}
