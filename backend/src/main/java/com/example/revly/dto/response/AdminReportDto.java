// what admin sees – includes reporter info + admin fields
package com.example.revly.dto.response;

import java.time.Instant;
import java.util.Set;

public class AdminReportDto {

    private Integer reportId;
    private Integer reporterId;
    private String reporterName;
    private String reporterProfilePic;
    private String entityType;
    private Integer entityId;
    private String explanation;
    private String status;
    private Instant createdAt;
    private Instant reviewedAt;
    private String adminExplanation;
    private Integer reviewedById;
    private String reviewedByName;
    private Set<ReportReasonDto> reasons;

    // Getters and setters
    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public Integer getReporterId() {
        return reporterId;
    }

    public void setReporterId(Integer reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterProfilePic() {
        return reporterProfilePic;
    }

    public void setReporterProfilePic(String reporterProfilePic) {
        this.reporterProfilePic = reporterProfilePic;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getAdminExplanation() {
        return adminExplanation;
    }

    public void setAdminExplanation(String adminExplanation) {
        this.adminExplanation = adminExplanation;
    }

    public Integer getReviewedById() {
        return reviewedById;
    }

    public void setReviewedById(Integer reviewedById) {
        this.reviewedById = reviewedById;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }

    public Set<ReportReasonDto> getReasons() {
        return reasons;
    }

    public void setReasons(Set<ReportReasonDto> reasons) {
        this.reasons = reasons;
    }
}
