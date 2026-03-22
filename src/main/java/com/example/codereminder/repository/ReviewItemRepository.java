package com.example.codereminder.repository;

import com.example.codereminder.domain.ReviewItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReviewItemRepository extends JpaRepository<ReviewItem, String> {

    Optional<ReviewItem> findByUserNameAndProblemId(String userName, Long problemId);
    List<ReviewItem> findByUserNameAndNextReviewDate(String userName, LocalDate nextReviewDate);
    List<ReviewItem> findAllByNextReviewDateBefore(LocalDate today);
}
