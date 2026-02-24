package com.example.revly.dto.response;

import com.example.revly.model.PostImage;
import com.example.revly.model.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostSummary {

    private int postId;
    private Integer authorId;  // Integer to allow null
    private String description;
    private String createdBy;
    private String createdByProfilePicUrl;
    private Instant createdAt;
    private int likeCount;
    private List<String> imageUrls;
    private Boolean hasLiked;
    private Boolean hasSaved;
    private Boolean followingAuthor;
    private Boolean authorIsMechanic;

    public PostSummary() {
        this.description = "";
        this.createdBy = "";
        this.createdAt = null;
        imageUrls = new ArrayList<>();
    }

    public Boolean getAuthorIsMechanic() {
        return authorIsMechanic;
    }

    public void setAuthorIsMechanic(Boolean authorIsMechanic) {
        this.authorIsMechanic = authorIsMechanic;
    }

    public int getPostId() {
        return postId;
    }

    public Boolean getFollowingAuthor() {
        return followingAuthor;
    }

    public void setFollowingAuthor(Boolean followingAuthor) {
        this.followingAuthor = followingAuthor;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    private Set<Integer> getUserIds(Set<User> users) {
        try {
            Set<Integer> ids = new HashSet<>();
            if (users != null) {  // prevent NullPointerException
                for (User u : users) {
                    ids.add(u.getUserId());
                }
            }
            return ids;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    List<String> getImageUrls(Set<PostImage> postImages) {
        try {
            List<String> images = new ArrayList<>();
            if (postImages != null) {  // prevent NullPointerException
                for (PostImage pi : postImages) {
                    images.add(pi.getImageUrl());
                }
            }
            return images;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<UserNameAndPfp> summarizeUsers(Set<User> users) {
        try {
            Set<UserNameAndPfp> summary = new HashSet<>();
            if (users != null) {    // prevent NullPointerException
                for (User u : users) {
                    summary.add(new UserNameAndPfp(u));
                }
            }
            return summary;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getCreatedByProfilePicUrl() {
        return createdByProfilePicUrl;
    }

    public void setCreatedByProfilePicUrl(String createdByProfilePicUrl) {
        this.createdByProfilePicUrl = createdByProfilePicUrl;
    }

    public Boolean getHasLiked() {
        return hasLiked;
    }

    public void setHasLiked(Boolean hasLiked) {
        this.hasLiked = hasLiked;
    }

    public Boolean getHasSaved() {
        return hasSaved;
    }

    public void setHasSaved(Boolean hasSaved) {
        this.hasSaved = hasSaved;
    }
}