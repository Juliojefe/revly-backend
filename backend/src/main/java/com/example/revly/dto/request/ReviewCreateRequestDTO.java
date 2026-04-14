package com.example.revly.dto.request;

public class ReviewCreateRequestDTO {
    private Integer mechanicId;
    private Integer businessId;   // optional but stored if provided
    private Double rating;
    private String content;

    // getters and setters
    public Integer getMechanicId() { return mechanicId; }
    public void setMechanicId(Integer mechanicId) { this.mechanicId = mechanicId; }
    public Integer getBusinessId() { return businessId; }
    public void setBusinessId(Integer businessId) { this.businessId = businessId; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}