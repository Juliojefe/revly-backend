package com.example.revly.dto.request;

import java.util.List;

public class CommentUpdateRequestDTO {
    private Integer userId;
    private Integer commentId;
    private String content; // Optional, null if no change
    private List<String> existingImageUrlsToKeep; // URLs to retain

    public CommentUpdateRequestDTO(Integer userId, Integer commentId, String content, List<String> existingImageUrlsToKeep) {
        this.userId = userId;
        this.commentId = commentId;
        this.content = content;
        this.existingImageUrlsToKeep = existingImageUrlsToKeep;
    }

    public CommentUpdateRequestDTO() {}

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

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

    public List<String> getExistingImageUrlsToKeep() {
        return existingImageUrlsToKeep;
    }

    public void setExistingImageUrlsToKeep(List<String> existingImageUrlsToKeep) {
        this.existingImageUrlsToKeep = existingImageUrlsToKeep;
    }
}