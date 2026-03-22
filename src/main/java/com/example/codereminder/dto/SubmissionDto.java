package com.example.codereminder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class SubmissionDto {
    @NotBlank
    private final String userName;
    @NotNull
    private final Long problemId;
    @NotBlank
    private final String resultText;
    @NotNull
    private final Long timestamp;
}
