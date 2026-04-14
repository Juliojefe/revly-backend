package com.example.revly.repository;

import com.example.revly.model.ReviewResponse;
import com.example.revly.model.ReviewResponseImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewResponseImageRepository extends JpaRepository<ReviewResponseImage, Integer> {
    List<ReviewResponseImage> findByResponse(ReviewResponse response);
}
