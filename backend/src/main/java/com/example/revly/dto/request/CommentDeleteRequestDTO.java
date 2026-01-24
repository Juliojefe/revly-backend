package com.example.revly.dto.request;

public class CommentDeleteRequestDTO {
    private Integer userId;
    private Integer commentId;

    public CommentDeleteRequestDTO(Integer userId, Integer commentId) {
        this.userId = userId;
        this.commentId = commentId;
    }

    public CommentDeleteRequestDTO() {}

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
}