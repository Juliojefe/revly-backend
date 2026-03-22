package com.example.revly.dto.request;


import java.util.ArrayList;
import java.util.List;

public class UpdatePostRequest {

    private String description;
    private List<String> tags;

    public UpdatePostRequest() {}

    public UpdatePostRequest(String description, List<String> tags) {
        this.description = description;
        this.tags = tags != null ? new ArrayList<>(tags) : null;
    }

    public String getDescription() { return description; }
    public List<String> getTags() { return tags; }
}