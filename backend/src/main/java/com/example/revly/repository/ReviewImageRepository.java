package com.example.revly.repository;

import com.example.revly.model.Review;
import com.example.revly.model.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Integer> {
    List<ReviewImage> findByReview(Review review);
}
