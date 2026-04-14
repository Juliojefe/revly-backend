package com.example.revly.model;

import jakarta.persistence.*;

@Entity
@Table(name = "review_image")
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "review_id")
    private Review review;

    // Getters and setters (exact same as CommentImage.java)
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Review getReview() { return review; }
    public void setReview(Review review) { this.review = review; }
}
