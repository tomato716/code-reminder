package com.example.codereminder.scheduler;

import com.example.codereminder.service.ReviewItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReviewDateScheduler {
    private final ReviewItemService service;

    @Scheduled(cron = "0 0 0 * * *")
    public void autoUpdateOverReviewDate() {
        service.updateOverReviewDate();
    }
}
