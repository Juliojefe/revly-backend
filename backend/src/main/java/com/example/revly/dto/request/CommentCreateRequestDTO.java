package com.example.revly.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

public class CommentCreateRequestDTO {
    private Integer postId;
    private Integer userId;
    private Instant createdAt;
    private String content;

    public CommentCreateRequestDTO(Integer postId, Integer userId, Instant createdAt, String content) {
        this.postId = postId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.content = content;
    }

    public CommentCreateRequestDTO() {}

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}