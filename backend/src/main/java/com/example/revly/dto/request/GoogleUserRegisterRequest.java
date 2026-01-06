package com.example.revly.dto.request;

public class GoogleUserRegisterRequest {
    private String googleId;
    private String email;
    private String name;
    private String profilePic;

    public GoogleUserRegisterRequest(String googleId, String email, String name, String profilePic) {
        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.profilePic = profilePic;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
