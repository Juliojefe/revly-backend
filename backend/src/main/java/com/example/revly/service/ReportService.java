package com.example.revly.service;

import com.example.revly.dto.request.CreateReportRequest;
import com.example.revly.dto.response.MyReportDto;
import com.example.revly.dto.response.ReportReasonDto;
import com.example.revly.exception.BadRequestException;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.Report;
import com.example.revly.model.ReportReason;
import com.example.revly.model.User;
import com.example.revly.repository.ReportReasonRepository;
import com.example.revly.repository.ReportRepository;
import com.example.revly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportReasonRepository reportReasonRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Creates a new report.
     * Called only when the user does NOT already have a report for this entity.
     */
    @Transactional
    public MyReportDto createReport(CreateReportRequest request, Principal principal) {
        User reporter = getUserFromPrincipalOrThrow(principal);

        // Enforce one-report-per-user-per-entity
        if (reportRepository.findByReporterUserIdAndEntityTypeAndEntityId(
                reporter.getUserId(), request.getEntityType(), request.getEntityId()).isPresent()) {
            throw new BadRequestException("You have already reported this " + request.getEntityType().toLowerCase());
        }

        // Validate reasons
        Set<ReportReason> reasons = new HashSet<>();
        for (String code : request.getReasonCodes()) {
            ReportReason reason = reportReasonRepository.findByCode(code)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid reason code: " + code));
            reasons.add(reason);
        }

        if (reasons.isEmpty()) {
            throw new BadRequestException("At least one reason is required");
        }

        if (request.getExplanation() == null || request.getExplanation().trim().isEmpty()) {
            throw new BadRequestException("Explanation is required");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setEntityType(request.getEntityType());
        report.setEntityId(request.getEntityId());
        report.setExplanation(request.getExplanation().trim());
        report.setStatus("PENDING");
        report.setCreatedAt(Instant.now());
        report.setReasons(reasons);

        Report saved = reportRepository.save(report);
        return toMyReportDto(saved);
    }

    /**
     * Returns the user's report for a specific entity.
     * Used when user clicks "View / Modify Report" on an entity they already reported.
     */
    @Transactional(readOnly = true)
    public Optional<MyReportDto> getMyReportForEntity(String entityType, Integer entityId, Principal principal) {
        User user = getUserFromPrincipalOrThrow(principal);

        Optional<Report> reportOpt = reportRepository.findByReporterUserIdAndEntityTypeAndEntityId(
                user.getUserId(), entityType, entityId);

        return reportOpt.map(this::toMyReportDto);
    }

    /**
     * Updates the user's own report (explanation + reasons).
     * only allowed while status is still PENDING.
     * Once admin changes status (IN_REVIEW, RESOLVED, etc.), user can no longer modify it.
     */
    @Transactional
    public MyReportDto updateMyReport(Integer reportId, Set<String> newReasonCodes, String newExplanation, Principal principal) {
        User user = getUserFromPrincipalOrThrow(principal);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        // Security: only the owner can update
        if (!report.getReporter().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException("You can only modify your own reports");
        }

        // Cannot modify once admin has started reviewing
        if (!"PENDING".equals(report.getStatus())) {
            throw new BadRequestException("This report can no longer be modified because an admin has already reviewed it");
        }

        // Update reasons
        Set<ReportReason> newReasons = new HashSet<>();
        for (String code : newReasonCodes) {
            ReportReason reason = reportReasonRepository.findByCode(code)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid reason code: " + code));
            newReasons.add(reason);
        }

        if (newReasons.isEmpty()) {
            throw new BadRequestException("At least one reason is required");
        }

        if (newExplanation == null || newExplanation.trim().isEmpty()) {
            throw new BadRequestException("Explanation is required");
        }

        report.setExplanation(newExplanation.trim());
        report.setReasons(newReasons);
        // createdAt and status remain unchanged

        Report updated = reportRepository.save(report);
        return toMyReportDto(updated);
    }

    /**
     * ADMIN ONLY: Review / update status of any report.
     */
    @Transactional
    public MyReportDto reviewReport(Integer reportId, String newStatus, String adminExplanation, Principal principal) {
        User admin = getUserFromPrincipalOrThrow(principal);
        if (admin.getUserRoles() == null || !Boolean.TRUE.equals(admin.getUserRoles().getIsAdmin())) {
            throw new UnauthorizedException("Only admins can review reports");
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        if (!Set.of("PENDING", "IN_REVIEW", "RESOLVED", "CLOSED", "DISMISSED").contains(newStatus)) {
            throw new BadRequestException("Invalid status: " + newStatus);
        }

        report.setStatus(newStatus);
        report.setAdminExplanation(adminExplanation != null ? adminExplanation.trim() : null);
        report.setReviewedBy(admin);
        report.setReviewedAt(Instant.now());

        Report updated = reportRepository.save(report);
        return toMyReportDto(updated);
    }

    /**
     * Returns ALL predefined report reasons (used by the frontend modal).
     * No paging needed — there are only ~13 reasons.
     */
    @Transactional(readOnly = true)
    public List<ReportReasonDto> getAllReportReasons() {
        return reportReasonRepository.findAll().stream()
                .map(r -> {
                    ReportReasonDto dto = new ReportReasonDto();
                    dto.setCode(r.getCode());
                    dto.setDescription(r.getDescription());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private MyReportDto toMyReportDto(Report report) {
        MyReportDto dto = new MyReportDto();
        dto.setReportId(report.getReportId());
        dto.setEntityType(report.getEntityType());
        dto.setEntityId(report.getEntityId());
        dto.setExplanation(report.getExplanation());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());

        // Convert reasons (fixed generic type + stream)
        List<ReportReasonDto> reasonDtos = report.getReasons().stream()
                .map(r -> {
                    ReportReasonDto rd = new ReportReasonDto();
                    rd.setCode(r.getCode());
                    rd.setDescription(r.getDescription());
                    return rd;
                })
                .collect(Collectors.toList());

        dto.setReasons(reasonDtos);

        return dto;
    }

    private User getUserFromPrincipalOrThrow(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
