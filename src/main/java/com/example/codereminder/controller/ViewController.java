package com.example.codereminder.controller;

import com.example.codereminder.domain.ReviewItem;
import com.example.codereminder.repository.ReviewItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {
    private final ReviewItemRepository repository;

    @GetMapping("/reviews/{userName}")
    public String showReviews(@PathVariable String userName, Model model) {
        List<ReviewItem> reviewItems = repository.findByUserNameAndNextReviewDate(userName, LocalDate.now());
        model.addAttribute("reviewItems", reviewItems);
        model.addAttribute("userName", userName);

        LocalDate today = LocalDate.now();
        model.addAttribute("today", today);

        return "reviewItems";
    }
}
