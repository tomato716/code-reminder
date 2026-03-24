package com.example.codereminder.controller;

import com.example.codereminder.dto.ReviewItemDto;
import com.example.codereminder.service.ReviewItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/review-item")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class ReviewItemController {

    private final ReviewItemService reviewItemService;

    @PostMapping
    public String saveSubmission(@Validated @RequestBody ReviewItemDto dto){
        log.debug("도착 데이터={}", dto);
        reviewItemService.save(dto);

        return "ok";
    }

    @GetMapping("/{userName}/today-count")
    public ResponseEntity<Map<String, Integer>> getTodayReviewCount(@PathVariable String userName) {
        int count = reviewItemService.getReviewItems(userName, LocalDate.now()).size();
        log.debug("{}님의 오늘 복습할 문제 개수: {}", userName, count);

        return ResponseEntity.ok(Map.of("today-count", count));
    }
}
