package com.example.revly.dto.response;

import java.time.Instant;import java.util.List;

public class CommentResponseDTO {
    private Integer commentId;
    private Integer authorId;
    private String createdByName;
    private String createdByProfilePicUrl;
    private String content;
    private List<String> imageUrls;
    private Instant createdAt;

    public CommentResponseDTO() {}

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getCreatedByProfilePicUrl() {
        return createdByProfilePicUrl;
    }

    public void setCreatedByProfilePicUrl(String createdByProfilePicUrl) {
        this.createdByProfilePicUrl = createdByProfilePicUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}