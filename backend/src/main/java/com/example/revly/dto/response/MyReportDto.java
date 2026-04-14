package com.example.revly.dto.response;

import java.time.Instant;
import java.util.List;

public class MyReportDto {

    private Integer reportId;
    private String entityType;
    private Integer entityId;
    private String explanation;
    private String status;
    private Instant createdAt;
    private List<ReportReasonDto> reasons;

    // Getters and setters
    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
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

    public List<ReportReasonDto> getReasons() {
        return reasons;
    }

    public void setReasons(List<ReportReasonDto> reasons) {
        this.reasons = reasons;
    }
}
