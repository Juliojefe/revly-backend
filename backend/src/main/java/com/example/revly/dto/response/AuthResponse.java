package com.example.revly.dto.response;

public class AuthResponse {
    private String message;
    private String name;
    private String email;
    private String profilePic;
    private boolean isGoogle;
    private String accessToken;
    private String refreshToken;
    private Boolean isMechanic;
    private Boolean isAdmin;

    // Constructor for success (message is omitted/null)
    public AuthResponse(String name, String email, String profilePic, boolean isGoogle, String accessToken, String refreshToken, Boolean isAdmin, Boolean isMechanic) {
        this.name = name;
        this.email = email;
        this.profilePic = profilePic;
        this.isGoogle = isGoogle;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.isAdmin = isAdmin;
        this.isMechanic = isMechanic;
    }

    // Constructor for error (other fields are omitted/null)
    public AuthResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public boolean isGoogle() {
        return isGoogle;
    }

    public void setGoogle(boolean google) {
        isGoogle = google;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Boolean getIsMechanic() {
        return isMechanic;
    }

    public void setIsMechanic(Boolean mechanic) {
        isMechanic = mechanic;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean admin) {
        isAdmin = admin;
    }
}