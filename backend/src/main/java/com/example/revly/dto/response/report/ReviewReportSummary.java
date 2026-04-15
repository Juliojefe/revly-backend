package com.example.revly.dto.response.report;

import java.util.List;

public class ReviewReportSummary extends ReportSummary {

    private Integer reviewId;
    private Double rating;
    private String content;
    private List<String> imageUrls;

    private Integer reviewerId;
    private String reviewerName;
    private String reviewerEmail;
    private String reviewerProfilePic;

    // Getters and setters
    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public Integer getReviewerId() { return reviewerId; }
    public void setReviewerId(Integer reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getReviewerEmail() { return reviewerEmail; }
    public void setReviewerEmail(String reviewerEmail) { this.reviewerEmail = reviewerEmail; }

    public String getReviewerProfilePic() { return reviewerProfilePic; }
    public void setReviewerProfilePic(String reviewerProfilePic) { this.reviewerProfilePic = reviewerProfilePic; }
}
