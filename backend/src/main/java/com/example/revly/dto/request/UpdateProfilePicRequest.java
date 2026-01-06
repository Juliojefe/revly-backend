package com.example.revly.dto.request;

public class UpdateProfilePicRequest {
    private int userId;
    private String pictureUrl;

    public UpdateProfilePicRequest(int userId, String pictureUrl) {
        this.userId = userId;
        this.pictureUrl = pictureUrl;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
