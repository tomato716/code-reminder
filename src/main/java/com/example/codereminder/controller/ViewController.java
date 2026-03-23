package com.example.codereminder.controller;

import com.example.codereminder.domain.ReviewItem;
import com.example.codereminder.service.ReviewItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {
    private final ReviewItemService service;

    @GetMapping("/reviews/{userName}")
    public String showReviews(@PathVariable String userName, Model model) {
        List<ReviewItem> reviewItems = service.getReviewItems(userName, LocalDate.now());
        model.addAttribute("reviewItems", reviewItems);
        model.addAttribute("userName", userName);

        LocalDate today = LocalDate.now();
        model.addAttribute("today", today);

        return "reviewItems";
    }
}
