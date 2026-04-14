package com.example.revly.repository;

import com.example.revly.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Optional<Review> findByReviewer_UserIdAndMechanic_UserId(Integer reviewerId, Integer mechanicId);

    Page<Review> findByMechanic_UserId(Integer mechanicId, Pageable pageable);

    @Query("SELECT AVG(r.rating), COUNT(r) FROM Review r WHERE r.mechanic.userId = :mechanicId")
    Object[] getAverageRatingAndCount(@Param("mechanicId") Integer mechanicId);
}
