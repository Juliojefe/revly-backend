package com.example.revly.repository;

import com.example.revly.model.ReportReason;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportReasonRepository extends JpaRepository<ReportReason, Integer> {

    Optional<ReportReason> findByCode(String code);
}
