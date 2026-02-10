package com.example.revly.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Integer postId;

    @Column(name = "description", nullable = false, length = 3000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;

    @ManyToMany(mappedBy = "savedPosts")
    @JsonIgnore
    private Set<User> savers = new HashSet<>();

    @ManyToMany(mappedBy = "likedPosts")
    @JsonIgnore
    private Set<User> likers = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    @JsonIgnore
    private Set<Comment> comments = new HashSet<>();

    // Getters and setters
    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public List<PostImage> getPostImages() {
        return images;
    }

    public void setPostImages(List<PostImage> images) {
        this.images = images;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Set<User> getSavers() {
        return savers;
    }

    public void setSavers(Set<User> savers) {
        this.savers = savers;
    }

    public Set<User> getLikers() {
        return likers;
    }

    public void setLikers(Set<User> likers) {
        this.likers = likers;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public Set<Integer> getCommentIds() {
        try {
            Set<Integer> ids = new HashSet<>();
            for (Comment c : this.getComments()) {
                ids.add(c.getCommentId());
            }
            return ids;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }
}