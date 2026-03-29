package com.example.codereminder.controller;

import com.example.codereminder.domain.ReviewItem;
import com.example.codereminder.service.ReviewItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ViewController {
    private final ReviewItemService service;

    @GetMapping("/reviews/{userName}")
    public String showReviews(@PathVariable String userName, Model model) {
        List<ReviewItem> reviewItems = service.getReviewItems(userName);
        model.addAttribute("reviewItems", reviewItems);
        model.addAttribute("userName", userName);

        model.addAttribute("today", LocalDate.now());

        return "reviewItems";
    }

    @GetMapping("/")
    public String showHome(Model model) {
        model.addAttribute("today", LocalDate.now());
        return "home";
    }

    @PostMapping("/")
    public String search(@RequestParam String userName) {
        return "redirect:/reviews/" + userName;
    }
}
