package com.example.revly.dto.response;

import java.sql.Timestamp;
import java.util.List;

public class CommentResponseDTO {
    private Integer commentId;
    private String content;
    private Integer userId;
    private Timestamp createdAt;
    private List<String> imageUrls;

    public CommentResponseDTO(Integer commentId, String content, Integer userId, Timestamp createdAt, List<String> imageUrls) {
        this.commentId = commentId;
        this.content = content;
        this.userId = userId;
        this.createdAt = createdAt;
        this.imageUrls = imageUrls;
    }

    public CommentResponseDTO() {}

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}