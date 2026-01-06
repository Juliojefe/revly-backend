package com.example.revly.dto.request;

import java.time.Instant;
import java.util.List;

public class CreatePostRequestUrl {

    private String description;
    private Instant createdAt;
    private List<String> images;

    public CreatePostRequestUrl(String description, Instant createdAt, List<String> images) {
        this.description = description;
        this.createdAt = createdAt;
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}