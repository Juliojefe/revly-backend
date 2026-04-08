package com.example.revly.dto.response;

import com.example.revly.model.Post;
import com.example.revly.model.User;
import java.util.HashSet;
import java.util.Set;

public class GetUserProfilePublicResponse {

    private String name;
    private boolean isMechanic;
    private boolean isAdmin;
    private Set<Integer> followingIds;
    private Set<Integer> followerIds;
    private Set<Integer> likedPostIds;
    private Set<Integer> ownedPostIds;
    private int followerCount;
    private int followingCount;
    private String profilePicUrl;
    private String biography;
    private String businessAddress;
    private Double businessLat;
    private Double businessLon;
    private boolean viewerCanViewFullProfile;
    private boolean viewerFollowsUser;

    public GetUserProfilePublicResponse() {
        this.name = "";
        this.isMechanic = false;
        this.isAdmin = false;
        this.followingIds = new HashSet<>();
        this.followerIds = new HashSet<>();
        this.likedPostIds = new HashSet<>();
        this.ownedPostIds = new HashSet<>();
        this.followerCount = 0;
        this.followingCount = 0;
        this.profilePicUrl = "";
        this.biography = "";
        this.businessAddress = null;
        this.businessLat = null;
        this.businessLon = null;
        this.viewerCanViewFullProfile = false;
        this.viewerFollowsUser = false;
    }

    public GetUserProfilePublicResponse(User u) {
        this(u, true, false);
    }

    public GetUserProfilePublicResponse(User u, boolean viewerCanViewFullProfile, boolean viewerFollowsUser) {
        this.name = u.getName();
        this.isMechanic = u.getUserRoles().getIsMechanic();
        this.isAdmin = u.getUserRoles().getIsAdmin();
        this.followingIds = viewerCanViewFullProfile ? getUserIds(u.getFollowing()) : new HashSet<>();
        this.followerIds = viewerCanViewFullProfile ? getUserIds(u.getFollowers()) : new HashSet<>();
        this.likedPostIds = viewerCanViewFullProfile ? getPostIds(u.getLikedPosts()) : new HashSet<>();
        this.ownedPostIds = viewerCanViewFullProfile ? getPostIds(u.getOwnedPosts()) : new HashSet<>();
        this.followerCount = followerIds.size();
        this.followingCount = followingIds.size();
        if (!viewerCanViewFullProfile) {
            this.followerCount = u.getFollowers().size();
            this.followingCount = u.getFollowing().size();
        }
        this.profilePicUrl = u.getProfilePic();
        this.biography = u.getBiography();
        if (viewerCanViewFullProfile) {
            var biz = u.getBusinesses().stream().findFirst();
            this.businessAddress = biz.map(com.example.revly.model.Business::getAddress).orElse(null);
            this.businessLat = biz.map(com.example.revly.model.Business::getLat).orElse(null);
            this.businessLon = biz.map(com.example.revly.model.Business::getLon).orElse(null);
        } else {
            this.businessAddress = null;
            this.businessLat = null;
            this.businessLon = null;
        }

        this.viewerCanViewFullProfile = viewerCanViewFullProfile;
        this.viewerFollowsUser = viewerFollowsUser;
    }

    private Set<Integer> getUserIds(Set<User> users) {
        try {
            Set<Integer> ids = new HashSet<>();
            for (User u : users) {
                ids.add(u.getUserId());
            }
            return ids;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Integer> getPostIds(Set<Post> posts) {
        try {
            Set<Integer> ids = new HashSet<>();
            for (Post p : posts) {
                ids.add(p.getPostId());
            }
            return ids;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public boolean isMechanic() {
        return isMechanic;
    }

    public void setMechanic(boolean mechanic) {
        isMechanic = mechanic;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Set<Integer> getFollowingIds() {
        return followingIds;
    }

    public void setFollowingIds(Set<Integer> followingIds) {
        this.followingIds = followingIds;
    }

    public Set<Integer> getFollowerIds() {
        return followerIds;
    }

    public void setFollowerIds(Set<Integer> followerIds) {
        this.followerIds = followerIds;
    }

    public Set<Integer> getLikedPostIds() {
        return likedPostIds;
    }

    public void setLikedPostIds(Set<Integer> likedPostIds) {
        this.likedPostIds = likedPostIds;
    }

    public Set<Integer> getOwnedPostIds() {
        return ownedPostIds;
    }

    public void setOwnedPostIds(Set<Integer> ownedPostIds) {
        this.ownedPostIds = ownedPostIds;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public Double getBusinessLat() {
        return businessLat;
    }

    public void setBusinessLat(Double businessLat) {
        this.businessLat = businessLat;
    }

    public Double getBusinessLon() {
        return businessLon;
    }

    public void setBusinessLon(Double businessLon) {
        this.businessLon = businessLon;
    }

    public boolean isViewerCanViewFullProfile() {
        return viewerCanViewFullProfile;
    }

    public void setViewerCanViewFullProfile(boolean viewerCanViewFullProfile) {
        this.viewerCanViewFullProfile = viewerCanViewFullProfile;
    }

    public boolean isViewerFollowsUser() {
        return viewerFollowsUser;
    }

    public void setViewerFollowsUser(boolean viewerFollowsUser) {
        this.viewerFollowsUser = viewerFollowsUser;
    }
}