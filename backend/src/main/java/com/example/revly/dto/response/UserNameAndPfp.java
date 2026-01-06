//	this is a comment
package com.example.revly.dto.response;

import com.example.revly.model.User;

public class UserNameAndPfp {
    private String name;
    private String profilePic;

    public UserNameAndPfp(String name, String profilePic) {
        this.name = name;
        this.profilePic = profilePic;
    }

    public UserNameAndPfp(User u) {
        this.name = u.getName();
        this.profilePic = u.getProfilePic();
    }

    public UserNameAndPfp() {
        this.name = "";
        this.profilePic = "";
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
