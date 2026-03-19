package com.example.codereminder.controller;

import com.example.codereminder.dto.SubmissionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
@Slf4j
public class SubmissionController {

    @PostMapping
    public String saveSubmission(@RequestBody SubmissionDto dto){
        log.info("도착 데이터={}", dto);

        return "ok";
    }
}
