package com.example.revly.dto.response;

public class RefreshResponse {

    private String accessToken;
    private Boolean success;

    public RefreshResponse(String accessToken) {
        this.accessToken = accessToken;
        success = true;
    }

    public RefreshResponse(Boolean success) {
        this.success = success;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}