package com.example.revly.dto.response;

import java.time.Instant;
import java.util.List;

public class ReviewResponseItemDTO {
    private Integer responseId;
    private Integer userId;
    private String userName;
    private String userProfilePicUrl;
    private String content;
    private Instant createdAt;
    private List<String> imageUrls;

    // getters and setters (same style as CommentResponseDTO)
    public Integer getResponseId() { return responseId; }
    public void setResponseId(Integer responseId) { this.responseId = responseId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserProfilePicUrl() { return userProfilePicUrl; }
    public void setUserProfilePicUrl(String userProfilePicUrl) { this.userProfilePicUrl = userProfilePicUrl; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
