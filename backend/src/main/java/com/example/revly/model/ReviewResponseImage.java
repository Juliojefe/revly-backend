package com.example.revly.model;

import jakarta.persistence.*;

@Entity
@Table(name = "review_response_image")
public class ReviewResponseImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "response_id")
    private ReviewResponse response;

    // Getters and setters (exact same as CommentImage.java)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public ReviewResponse getResponse() { return response; }
    public void setResponse(ReviewResponse response) { this.response = response; }
}
