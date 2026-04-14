package com.example.revly.dto.request;

public class CreateReviewResponseRequestDTO {
    private Integer reviewId;
    private String content;

    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
