package com.example.revly.repository;

import com.example.revly.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Integer> {

    // Admin list view - all reports for a specific entity type, oldest first
    Page<Report> findByEntityTypeOrderByCreatedAtAsc(String entityType, Pageable pageable);

    // User's "My Reports"
    List<Report> findByReporterUserIdOrderByCreatedAtDesc(Integer reporterUserId);

    Page<Report> findByReporterUserIdOrderByCreatedAtDesc(Integer reporterUserId, Pageable pageable);

    // Filter by status (used by both user and admin)
    List<Report> findByReporterUserIdAndStatusOrderByCreatedAtDesc(Integer reporterUserId, String status);
    Page<Report> findByReporterUserIdAndStatusOrderByCreatedAtDesc(Integer reporterUserId, String status, Pageable pageable);

    // Admin dashboard - all reports by status
    Page<Report> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    // Admin - reports for a specific entity (e.g. when viewing a post's reports)
    List<Report> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Integer entityId);

    // Prevent duplicate reports (used in service before save)
    Optional<Report> findByReporterUserIdAndEntityTypeAndEntityId(Integer reporterUserId, String entityType, Integer entityId);

    // Admin history - reports they have reviewed
    Page<Report> findByReviewedByUserIdOrderByReviewedAtDesc(Integer reviewedByUserId, Pageable pageable);

    // Count reports per entity (optional but useful for admin UI)
    @Query("SELECT COUNT(r) FROM Report r WHERE r.entityType = :entityType AND r.entityId = :entityId")
    long countByEntityTypeAndEntityId(@Param("entityType") String entityType, @Param("entityId") Integer entityId);
}
