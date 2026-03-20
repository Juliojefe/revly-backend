package com.example.revly.repository;

import com.example.revly.model.PostEmbeddingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PostEmbeddingJobRepository extends JpaRepository<PostEmbeddingJob, Long> {
    @Modifying
    @Query("UPDATE PostEmbeddingJob j " +
            "SET j.status = 'processing', j.lockedAt = CURRENT_TIMESTAMP " +
            "WHERE j.jobId = :jobId " +
            "AND j.status = 'pending' " +
            "AND j.lockedAt IS NULL")
    int claimJob(@Param("jobId") Long jobId);

    @Query("SELECT j FROM PostEmbeddingJob j " +
            "WHERE j.status = 'pending' " +
            "AND j.nextAttemptAt <= :now " +
            "AND j.lockedAt IS NULL " +
            "ORDER BY j.nextAttemptAt ASC, j.jobId ASC")
    Optional<PostEmbeddingJob> findNextPendingJob(@Param("now") Instant now);

}
