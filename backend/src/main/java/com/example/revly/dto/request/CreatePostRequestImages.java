package com.example.revly.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CreatePostRequestImages {

    private String description;
    private Instant createdAt;
    private List<MultipartFile> images;
    private List<String> tags;

    public CreatePostRequestImages(String description, Instant createdAt, List<MultipartFile> images, List<String> tags) {
        this.description = description;
        this.createdAt = createdAt;
        this.images = images;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images;
    }
}