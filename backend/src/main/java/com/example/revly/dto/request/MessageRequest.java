package com.example.revly.dto.request;

import java.util.List;

// Request DTO
public class MessageRequest {
    private String content;
    private List<String> imageUrls;

    // Getters/setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
