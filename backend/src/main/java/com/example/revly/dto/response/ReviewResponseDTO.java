package com.example.revly.dto.response;

import java.time.Instant;
import java.util.List;

public class ReviewResponseDTO {
    private Integer reviewId;
    private Integer reviewerId;
    private String reviewerName;
    private String reviewerProfilePicUrl;
    private Integer mechanicId;
    private Double rating;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> imageUrls;
    private boolean isCurrentUsersReview;

    // getters and setters
    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }
    public Integer getReviewerId() { return reviewerId; }
    public void setReviewerId(Integer reviewerId) { this.reviewerId = reviewerId; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public String getReviewerProfilePicUrl() { return reviewerProfilePicUrl; }
    public void setReviewerProfilePicUrl(String reviewerProfilePicUrl) { this.reviewerProfilePicUrl = reviewerProfilePicUrl; }
    public Integer getMechanicId() { return mechanicId; }
    public void setMechanicId(Integer mechanicId) { this.mechanicId = mechanicId; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public boolean isCurrentUsersReview() { return isCurrentUsersReview; }
    public void setCurrentUsersReview(boolean currentUsersReview) { isCurrentUsersReview = currentUsersReview; }
}
