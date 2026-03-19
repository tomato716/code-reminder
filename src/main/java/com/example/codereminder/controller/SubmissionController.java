package com.example.codereminder.controller;

import com.example.codereminder.dto.SubmissionDto;
import com.example.codereminder.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public String saveSubmission(@Validated @RequestBody SubmissionDto dto){
        log.info("도착 데이터={}", dto);
        submissionService.save(dto);

        return "ok";
    }
}
