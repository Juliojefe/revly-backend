package com.example.revly.controller;

import com.example.revly.dto.request.CreateReportRequest;
import com.example.revly.dto.response.MyReportDto;
import com.example.revly.dto.response.ReportReasonDto;
import com.example.revly.dto.response.report.*;
import com.example.revly.exception.BadRequestException;
import com.example.revly.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<MyReportDto> createReport(
            @RequestBody CreateReportRequest request,
            Principal principal) {
        return ResponseEntity.ok(reportService.createReport(request, principal));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<MyReportDto> getMyReportForEntity(
            @PathVariable String entityType,
            @PathVariable Integer entityId,
            Principal principal) {
        return reportService.getMyReportForEntity(entityType, entityId, principal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{reportId}")
    public ResponseEntity<MyReportDto> updateMyReport(
            @PathVariable Integer reportId,
            @RequestBody UpdateReportRequest request,
            Principal principal) {
        return ResponseEntity.ok(
                reportService.updateMyReport(reportId, request.getReasonCodes(), request.getExplanation(), principal)
        );
    }

    @PatchMapping("/{reportId}/review")
    public ResponseEntity<MyReportDto> reviewReport(
            @PathVariable Integer reportId,
            @RequestBody ReviewReportRequest request,
            Principal principal) {
        return ResponseEntity.ok(
                reportService.reviewReport(reportId, request.getStatus(), request.getAdminExplanation(), principal)
        );
    }

    @GetMapping("/reasons")
    public ResponseEntity<List<ReportReasonDto>> getAllReasons() {
        return ResponseEntity.ok(reportService.getAllReportReasons());
    }

    @GetMapping
    public ResponseEntity<Page<ReportSummary>> getReportsByEntityType(
            @RequestParam String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<ReportSummary> result = reportService.getReportsByEntityType(entityType, pageable, principal);
        return ResponseEntity.ok(result);
    }

    // Single report endpoints (MESSAGE_IMAGE removed)
    @GetMapping("/USER/{reportId}")
    public ResponseEntity<UserReportSummary> getUserReport(@PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof UserReportSummary)) throw new BadRequestException("Wrong type");
        return ResponseEntity.ok((UserReportSummary) summary);
    }

    @GetMapping("/POST/{reportId}")
    public ResponseEntity<PostReportSummary> getPostReport(@PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof PostReportSummary)) throw new BadRequestException("Wrong type");
        return ResponseEntity.ok((PostReportSummary) summary);
    }

    @GetMapping("/COMMENT/{reportId}")
    public ResponseEntity<CommentReportSummary> getCommentReport(@PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof CommentReportSummary)) throw new BadRequestException("Wrong type");
        return ResponseEntity.ok((CommentReportSummary) summary);
    }

    @GetMapping("/REVIEW/{reportId}")
    public ResponseEntity<ReviewReportSummary> getReviewReport(@PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof ReviewReportSummary)) throw new BadRequestException("Wrong type");
        return ResponseEntity.ok((ReviewReportSummary) summary);
    }

    @GetMapping("/REVIEW_RESPONSE/{reportId}")
    public ResponseEntity<ReviewResponseReportSummary> getReviewResponseReport(@PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof ReviewResponseReportSummary)) throw new BadRequestException("Wrong type");
        return ResponseEntity.ok((ReviewResponseReportSummary) summary);
    }

    @GetMapping("/MESSAGE/{reportId}")
    public ResponseEntity<MessageReportSummary> getMessageReport(@PathVariable Integer reportId, Principal principal) {
        ReportSummary summary = reportService.getSingleReport(reportId, principal);
        if (!(summary instanceof MessageReportSummary)) throw new BadRequestException("Wrong type");
        return ResponseEntity.ok((MessageReportSummary) summary);
    }

    // Small request DTOs
    public static class UpdateReportRequest {
        private Set<String> reasonCodes;
        private String explanation;
        public Set<String> getReasonCodes() { return reasonCodes; }
        public void setReasonCodes(Set<String> reasonCodes) { this.reasonCodes = reasonCodes; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }
    }

    public static class ReviewReportRequest {
        private String status;
        private String adminExplanation;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getAdminExplanation() { return adminExplanation; }
        public void setAdminExplanation(String adminExplanation) { this.adminExplanation = adminExplanation; }
    }
}