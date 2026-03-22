package com.example.revly.dto.response;

public class UserSearchResult {

    private Integer userId;
    private String name;
    private String profilePic;
    private boolean isMechanic;

    public UserSearchResult(Integer userId, String name, String profilePic, boolean isMechanic) {
        this.userId = userId;
        this.name = name;
        this.profilePic = profilePic;
        this.isMechanic = isMechanic;
    }

    public Integer getUserId() { return userId; }
    public String getName() { return name; }
    public String getProfilePic() { return profilePic; }
    public boolean isMechanic() { return isMechanic; }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public void setMechanic(boolean mechanic) {
        isMechanic = mechanic;
    }
}