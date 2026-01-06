package com.example.revly.dto.request;

import java.util.Set;

public class ChatCreateRequest {
    private String name;
    private Set<Integer> userIds;  // Users to add (including creator?)

    // Getters/setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(Set<Integer> userIds) {
        this.userIds = userIds;
    }
}
