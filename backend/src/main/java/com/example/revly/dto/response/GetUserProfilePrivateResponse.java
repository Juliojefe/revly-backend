package com.example.revly.dto.response;

import com.example.revly.model.Chat;
import com.example.revly.model.Post;
import com.example.revly.model.User;
import java.util.HashSet;
import java.util.Set;


//  NOTE this entire file need redoing
public class GetUserProfilePrivateResponse {
    private String name;
    private boolean isMechanic;
    private boolean isAdmin;
    private Set<Integer> chatIds;
    private Set<Integer> followingIds;
    private Set<Integer> followerIds;
    private Set<Integer> savedPostIds;
    private Set<Integer> likedPostIds;
    private Set<Integer> ownedPostIds;
    private int followerCount;
    private int followingCount;
    private String profilePicUrl;


    public GetUserProfilePrivateResponse() {
        this.name = "";
        this.isMechanic = false;
        this.isAdmin = false;
        this.chatIds = new HashSet<>();
        this.followingIds = new HashSet<>();
        this.followerIds = new HashSet<>();
        this.savedPostIds = new HashSet<>();
        this.likedPostIds = new HashSet<>();
        this.ownedPostIds = new HashSet<>();
        this.followerCount = 0;
        this.followingCount = 0;
        this.profilePicUrl = "";
    }

    public GetUserProfilePrivateResponse(User u) {
        this.name = u.getName();
        this.isMechanic = u.getUserRoles().getIsMechanic();
        this.isAdmin = u.getUserRoles().getIsAdmin();
        this.chatIds = getChatIds(u.getChats());
        this.followingIds = getUserIds(u.getFollowing());
        this.followerIds = getUserIds(u.getFollowers());
        this.savedPostIds = getPostIds(u.getSavedPosts());
        this.likedPostIds = getPostIds(u.getLikedPosts());
        this.ownedPostIds = getPostIds(u.getOwnedPosts());
        this.followerCount = followerIds.size();
        this.followingCount = followingIds.size();
        this.profilePicUrl = u.getProfilePic();
    }

    private Set<Integer> getChatIds(Set<Chat> chats) {
        try {
            Set<Integer> ids = new HashSet<>();
            for (Chat cht : chats) {
                ids.add(cht.getChatId());
            }
            return ids;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private Set<ChatSummary> summarizeChat(Set<Chat> chats) {
        try {
            Set<ChatSummary> chatSum = new HashSet<>();
            for (Chat cht : chats) {
                chatSum.add(new ChatSummary(cht));
            }
            return chatSum;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<UserNameAndPfp> summarizeUsers(Set<User> users) {
        try {
            Set<UserNameAndPfp> summary = new HashSet<>();
            for (User u : users) {
                summary.add(new UserNameAndPfp(u));
            }
            return summary;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<PostSummary> summarizePosts(Set<Post> posts) {
        try {
            Set<PostSummary> pSum = new HashSet<>();
            for (Post p : posts) {
                pSum.add(new PostSummary());
            }
            return pSum;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
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

    public Set<Integer> getChatIds() {
        return chatIds;
    }

    public void setChatIds(Set<Integer> chatIds) {
        this.chatIds = chatIds;
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

    public Set<Integer> getSavedPostIds() {
        return savedPostIds;
    }

    public void setSavedPostIds(Set<Integer> savedPostIds) {
        this.savedPostIds = savedPostIds;
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
}