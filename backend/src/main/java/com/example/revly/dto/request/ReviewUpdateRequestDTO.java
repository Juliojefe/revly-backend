package com.example.revly.dto.request;

import java.util.List;

public class ReviewUpdateRequestDTO {
    private Integer reviewId;
    private Double rating;
    private String content;
    private List<String> existingImageUrlsToKeep;

    // getters and setters
    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getExistingImageUrlsToKeep() { return existingImageUrlsToKeep; }
    public void setExistingImageUrlsToKeep(List<String> existingImageUrlsToKeep) { this.existingImageUrlsToKeep = existingImageUrlsToKeep; }
}
