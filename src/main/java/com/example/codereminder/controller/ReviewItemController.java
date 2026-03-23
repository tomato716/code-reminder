package com.example.codereminder.controller;

import com.example.codereminder.dto.ReviewItemDto;
import com.example.codereminder.service.ReviewItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review-item")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class ReviewItemController {

    private final ReviewItemService reviewItemService;

    @PostMapping
    public String saveSubmission(@Validated @RequestBody ReviewItemDto dto){
        log.info("도착 데이터={}", dto);
        reviewItemService.save(dto);

        return "ok";
    }
}
