package com.example.revly.dto.response.report;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.example.revly.dto.response.ReportReasonDto;

import java.time.Instant;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "entityType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = UserReportSummary.class, name = "USER"),
    @JsonSubTypes.Type(value = PostReportSummary.class, name = "POST"),
    @JsonSubTypes.Type(value = CommentReportSummary.class, name = "COMMENT"),
    @JsonSubTypes.Type(value = ReviewReportSummary.class, name = "REVIEW"),
    @JsonSubTypes.Type(value = ReviewResponseReportSummary.class, name = "REVIEW_RESPONSE"),
    @JsonSubTypes.Type(value = MessageReportSummary.class, name = "MESSAGE"),
    @JsonSubTypes.Type(value = MessageImageReportSummary.class, name = "MESSAGE_IMAGE")
})

//  parent class
public abstract class ReportSummary {

    private Integer reportId;
    private String entityType;
    private Integer entityId;
    private String explanation;
    private String status;
    private Instant createdAt;

    // Reporter info (common to all)
    private Integer reporterId;
    private String reporterName;
    private String reporterEmail;
    private String reporterProfilePic;

    // Admin review fields
    private Integer reviewedBy;
    private String adminExplanation;
    private Instant reviewedAt;

    private List<ReportReasonDto> reasons;

    // Getters and setters
    public Integer getReportId() { return reportId; }
    public void setReportId(Integer reportId) { this.reportId = reportId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Integer getReporterId() { return reporterId; }
    public void setReporterId(Integer reporterId) { this.reporterId = reporterId; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterEmail() { return reporterEmail; }
    public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }

    public String getReporterProfilePic() { return reporterProfilePic; }
    public void setReporterProfilePic(String reporterProfilePic) { this.reporterProfilePic = reporterProfilePic; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public String getAdminExplanation() { return adminExplanation; }
    public void setAdminExplanation(String adminExplanation) { this.adminExplanation = adminExplanation; }

    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }

    public List<ReportReasonDto> getReasons() { return reasons; }
    public void setReasons(List<ReportReasonDto> reasons) { this.reasons = reasons; }
}
