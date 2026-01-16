package com.example.revly.dto.request;

public class UpdateBioRequest {
    private int userId;
    private String newBio;

    public UpdateBioRequest(int userId, String newBio) {
        this.userId = userId;
        this.newBio = newBio;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNewBio() {
        return newBio;
    }

    public void setNewBio(String newBio) {
        this.newBio = newBio;
    }
}
