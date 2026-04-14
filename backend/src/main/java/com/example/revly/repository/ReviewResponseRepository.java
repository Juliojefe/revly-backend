package com.example.revly.repository;

import com.example.revly.model.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewResponseRepository extends JpaRepository<ReviewResponse, Integer> {
    Page<ReviewResponse> findByReview_ReviewId(Integer reviewId, Pageable pageable);
}
