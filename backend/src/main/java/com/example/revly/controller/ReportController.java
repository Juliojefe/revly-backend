package com.example.revly.controller;

import com.example.revly.dto.request.CreateReportRequest;
import com.example.revly.dto.response.MyReportDto;
import com.example.revly.dto.response.ReportReasonDto;
import com.example.revly.dto.response.report.*;
import com.example.revly.exception.BadRequestException;
import com.example.revly.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * CREATE a new report for an entity (first time only).
     * Frontend should call this only if getMyReportForEntity returns no report.
     */
    @PostMapping
    public ResponseEntity<MyReportDto> createReport(
            @RequestBody CreateReportRequest request,
            Principal principal) {

        return ResponseEntity.ok(reportService.createReport(request, principal));
    }

    /**
     * GET the user's existing report for a specific entity.
     * Used when user clicks "View / Modify Report" button on any entity.
     * Returns 404 if the user has not reported this entity yet.
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<MyReportDto> getMyReportForEntity(
            @PathVariable String entityType,
            @PathVariable Integer entityId,
            Principal principal) {

        return reportService.getMyReportForEntity(entityType, entityId, principal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * UPDATE the user's own report (explanation + reasons).
     * Only allowed while the report is still in PENDING status.
     */
    @PutMapping("/{reportId}")
    public ResponseEntity<MyReportDto> updateMyReport(
            @PathVariable Integer reportId,
            @RequestBody UpdateReportRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                reportService.updateMyReport(reportId, request.getReasonCodes(), request.getExplanation(), principal)
        );
    }

    /**
     * ADMIN ONLY: Review / update the status and add admin explanation.
     */
    @PatchMapping("/{reportId}/review")
    public ResponseEntity<MyReportDto> reviewReport(
            @PathVariable Integer reportId,
            @RequestBody ReviewReportRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                reportService.reviewReport(reportId, request.getStatus(), request.getAdminExplanation(), principal)
        );
    }

    /**
     * GET all predefined report reasons for the modal.
     * Called when the ReportModal opens.
     */
    @GetMapping("/reasons")
    public ResponseEntity<List<ReportReasonDto>> getAllReasons() {
        return ResponseEntity.ok(reportService.getAllReportReasons());
    }

    @GetMapping("/USER/{reportId}")
    public ResponseEntity<UserReportSummary> getUserReport(
            @PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof UserReportSummary)) {
            throw new BadRequestException("This report is not of type USER");
        }
        return ResponseEntity.ok((UserReportSummary) summary);
    }

    @GetMapping("/POST/{reportId}")
    public ResponseEntity<PostReportSummary> getPostReport(
            @PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof PostReportSummary)) {
            throw new BadRequestException("This report is not of type POST");
        }
        return ResponseEntity.ok((PostReportSummary) summary);
    }

    @GetMapping("/COMMENT/{reportId}")
    public ResponseEntity<CommentReportSummary> getCommentReport(
            @PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof CommentReportSummary)) {
            throw new BadRequestException("This report is not of type COMMENT");
        }
        return ResponseEntity.ok((CommentReportSummary) summary);
    }

    @GetMapping("/REVIEW/{reportId}")
    public ResponseEntity<ReviewReportSummary> getReviewReport(
            @PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof ReviewReportSummary)) {
            throw new BadRequestException("This report is not of type REVIEW");
        }
        return ResponseEntity.ok((ReviewReportSummary) summary);
    }

    @GetMapping("/REVIEW_RESPONSE/{reportId}")
    public ResponseEntity<ReviewResponseReportSummary> getReviewResponseReport(
            @PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof ReviewResponseReportSummary)) {
            throw new BadRequestException("This report is not of type REVIEW_RESPONSE");
        }
        return ResponseEntity.ok((ReviewResponseReportSummary) summary);
    }

    @GetMapping("/MESSAGE/{reportId}")
    public ResponseEntity<MessageReportSummary> getMessageReport(
            @PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof MessageReportSummary)) {
            throw new BadRequestException("This report is not of type MESSAGE");
        }
        return ResponseEntity.ok((MessageReportSummary) summary);
    }

    @GetMapping("/MESSAGE_IMAGE/{reportId}")
    public ResponseEntity<MessageImageReportSummary> getMessageImageReport(
            @PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof MessageImageReportSummary)) {
            throw new BadRequestException("This report is not of type MESSAGE_IMAGE");
        }
        return ResponseEntity.ok((MessageImageReportSummary) summary);
    }

    // ===================================================================
    // Small request DTOs used only by this controller
    // ===================================================================

    /**
     * Simple DTO for updating a report (user side).
     */
    public static class UpdateReportRequest {
        private Set<String> reasonCodes;
        private String explanation;

        public Set<String> getReasonCodes() {
            return reasonCodes;
        }

        public void setReasonCodes(Set<String> reasonCodes) {
            this.reasonCodes = reasonCodes;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }
    }

    /**
     * Simple DTO for admin review action.
     */
    public static class ReviewReportRequest {
        private String status;
        private String adminExplanation;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getAdminExplanation() {
            return adminExplanation;
        }

        public void setAdminExplanation(String adminExplanation) {
            this.adminExplanation = adminExplanation;
        }
    }
}
