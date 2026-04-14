package com.example.revly.dto.request;

import java.util.Set;

public class CreateReportRequest {

    private String entityType;
    private Integer entityId;
    private Set<String> reasonCodes;   // e.g. ["SPAM", "HARASSMENT"]
    private String explanation;

    // Getters and setters
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
