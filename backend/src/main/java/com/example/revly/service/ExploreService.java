package com.example.revly.service;

import com.example.revly.dto.response.PostSummary;
import com.example.revly.model.Post;
import com.example.revly.model.PostImage;
import com.example.revly.model.User;
import com.example.revly.repository.PostRepository;
import com.example.revly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExploreService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    /*
    Posts of those not followed by user, newest to oldest
     */
    public Page<PostSummary> getExplorePosts(Pageable pageable, User u) {
        // Step 1: Get a page of post IDs that are recommended for this user (explore feed).
        // This uses a database query that takes the user's ID to personalize.
        Page<Integer> postIdsPage = postRepository.findExplorePostIds(u.getUserId(), pageable);

        // Get the list of post IDs from this page.
        List<Integer> postIds = postIdsPage.getContent();

        // If there are no post IDs, return an empty page right away.
        if (postIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // Step 2: Fetch the full post details for these IDs from the database.
        List<Post> posts = postRepository.findByPostIdInWithDetails(postIds);

        // Create a map where the key is post ID and value is the Post object.
        // This makes it easy to look up posts by ID later.
        Map<Integer, Post> postMap = new HashMap<>();
        for (Post post : posts) {
            postMap.put(post.getPostId(), post);
        }

        // Step 3: Create an ordered list of posts, matching the order of the original post IDs.
        // Skip any that weren't found (using null check).
        List<Post> orderedPosts = new ArrayList<>();
        for (Integer postId : postIds) {
            Post post = postMap.get(postId);
            if (post != null) {
                orderedPosts.add(post);
            }
        }

        // Step 4: Fetch like counts for all these posts in one batch query.
        // The result is a list of arrays, where each array is [postId, likeCount].
        List<Object[]> likeCountsRaw = postRepository.findLikeCountsByPostIds(postIds);

        // Convert that list into a map for easy lookup: postId -> likeCount.
        Map<Integer, Long> likeCounts = new HashMap<>();
        for (Object[] raw : likeCountsRaw) {
            Integer postId = (Integer) raw[0];
            Long count = (Long) raw[1];
            likeCounts.put(postId, count);
        }

        // Step 5: Fetch which of these posts the user has liked.
        List<Integer> likedIds = postRepository.findLikedPostIdsForUser(postIds, u.getUserId());

        // Put them into a set for fast checks (O(1) lookup).
        Set<Integer> likedSet = new HashSet<>();
        for (Integer id : likedIds) {
            likedSet.add(id);
        }

        // Step 6: Fetch which of these posts the user has saved.
        List<Integer> savedIds = postRepository.findSavedPostIdsForUser(postIds, u.getUserId());

        Set<Integer> savedSet = new HashSet<>();
        for (Integer id : savedIds) {
            savedSet.add(id);
        }

        // Step 7: Collect all unique author IDs from the posts (skip null authors).
        List<Integer> authorIds = new ArrayList<>();
        for (Post post : orderedPosts) {
            User author = post.getUser();
            if (author != null) {
                Integer authorId = author.getUserId();
                if (!authorIds.contains(authorId)) {  // Ensure unique
                    authorIds.add(authorId);
                }
            }
        }

        // Step 8: Check which of these authors the user is following.
        List<Integer> followedAuthorIds = userRepository.findFollowedAuthorIds(u.getUserId(), authorIds);

        Set<Integer> followedAuthors = new HashSet<>();
        for (Integer id : followedAuthorIds) {
            followedAuthors.add(id);
        }

        // Step 9: Build the list of PostSummary objects.
        List<PostSummary> summaries = new ArrayList<>();
        for (Post post : orderedPosts) {
            User author = post.getUser();

            // Check if following the author (false if author is null).
            boolean followingAuthor = false;
            if (author != null) {
                followingAuthor = followedAuthors.contains(author.getUserId());
            }

            // Check if user liked this post.
            boolean hasLiked = likedSet.contains(post.getPostId());

            // Check if user saved this post.
            boolean hasSaved = savedSet.contains(post.getPostId());

            // Get like count, default to 0 if not found.
            long likeCount = likeCounts.getOrDefault(post.getPostId(), 0L);

            // Convert to PostSummary DTO with all this info.
            PostSummary summary = toPostSummaryDto(post, hasLiked, hasSaved, followingAuthor, likeCount);
            summaries.add(summary);
        }

        // Step 10: Return the paginated result using the summaries list.
        // Total elements come from the original post IDs page.
        return new PageImpl<>(summaries, pageable, postIdsPage.getTotalElements());
    }

    public Page<PostSummary> getExplorePostsGuest(Pageable pageable) {
        // Step 1: Get a page of all post IDs (no personalization for guests).
        Page<Integer> postIdsPage = postRepository.findAllPostIds(pageable);

        // Get the list of post IDs from this page.
        List<Integer> postIds = postIdsPage.getContent();

        // If no post IDs, return empty page.
        if (postIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // Step 2: Fetch full post details for these IDs.
        List<Post> posts = postRepository.findByPostIdInWithDetails(postIds);

        // Create a map for quick lookup: postId -> Post.
        Map<Integer, Post> postMap = new HashMap<>();
        for (Post post : posts) {
            postMap.put(post.getPostId(), post);
        }

        // Step 3: Create ordered list of posts based on original ID order.
        List<Post> orderedPosts = new ArrayList<>();
        for (Integer postId : postIds) {
            Post post = postMap.get(postId);
            if (post != null) {
                orderedPosts.add(post);
            }
        }

        // Step 4: Fetch like counts in batch.
        List<Object[]> likeCountsRaw = postRepository.findLikeCountsByPostIds(postIds);

        // Convert to map: postId -> likeCount.
        Map<Integer, Long> likeCounts = new HashMap<>();
        for (Object[] raw : likeCountsRaw) {
            Integer postId = (Integer) raw[0];
            Long count = (Long) raw[1];
            likeCounts.put(postId, count);
        }

        // Step 5: Build summaries (no user-specific checks, all set to false).
        List<PostSummary> summaries = new ArrayList<>();
        for (Post post : orderedPosts) {
            // Get like count, default 0.
            long likeCount = likeCounts.getOrDefault(post.getPostId(), 0L);

            // Create summary with defaults.
            PostSummary summary = toPostSummaryDto(post, false, false, false, likeCount);
            summaries.add(summary);
        }

        // Step 6: Return paginated result.
        return new PageImpl<>(summaries, pageable, postIdsPage.getTotalElements());
    }

    private PostSummary toPostSummaryDto(Post p, Boolean hasLiked, Boolean hasSaved, Boolean followingAuthor, Long likeCount) {
        PostSummary summary = new PostSummary();
        User user = p.getUser();
        if (user != null) { //  user exists case
            summary.setAuthorId(user.getUserId());
            summary.setCreatedBy(user.getName());
            summary.setCreatedByProfilePicUrl(user.getProfilePic());
            summary.setAuthorIsMechanic(user.getUserRoles().getIsMechanic());
        } else {    //  user is null case such as when user has deleted their account
            summary.setAuthorId(null);
            summary.setCreatedBy(null);
            summary.setCreatedByProfilePicUrl(null);
            summary.setAuthorIsMechanic(false);
        }

        summary.setPostId(p.getPostId());
        summary.setDescription(p.getDescription());
        summary.setCreatedAt(p.getCreatedAt());
        summary.setLikeCount(Math.toIntExact(likeCount));   //  casting to correct type for dto

        List<String> imageUrls = new ArrayList<>();
        for (PostImage img : p.getPostImages()) {
            imageUrls.add(img.getImageUrl());   //  putting imageUrls in a List
        }
        summary.setImageUrls(imageUrls);

        summary.setHasLiked(hasLiked);
        summary.setHasSaved(hasSaved);
        summary.setFollowingAuthor(followingAuthor);
        return summary;
    }
}