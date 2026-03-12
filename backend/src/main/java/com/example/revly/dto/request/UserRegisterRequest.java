package com.example.revly.dto.request;

import org.springframework.web.multipart.MultipartFile;

public class UserRegisterRequest {
    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private MultipartFile profilePic;
    private String biography;

    public UserRegisterRequest() {}

    public UserRegisterRequest(String name, String email, String password, String confirmPassword, MultipartFile profilePic, String biography) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.profilePic = profilePic;
        this.biography = biography;
    }

    // Getters and setters...
    public MultipartFile getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(MultipartFile profilePic) {
        this.profilePic = profilePic;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
