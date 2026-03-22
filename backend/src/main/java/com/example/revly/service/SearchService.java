package com.example.revly.service;

import com.example.revly.dto.response.PostSummary;
import com.example.revly.dto.response.UserSearchResult;
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
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TextEmbeddingService textEmbeddingService;

    public Page<PostSummary> searchPostsByText(String query, Pageable pageable, User currentUser) {
        if (query == null || query.trim().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<Float> embedding = textEmbeddingService.embed(query.trim());
        Page<Integer> postIdsPage = postRepository.findPostIdsBySemanticSimilarity(embedding, pageable);
        return buildPostSummaryPage(postIdsPage, pageable, currentUser);
    }

    public Page<PostSummary> searchPostsByTag(String tag, Pageable pageable, User currentUser) {
        if (tag == null || tag.trim().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        String normalized = tag.trim().toLowerCase();
        Page<Integer> postIdsPage = postRepository.findPostIdsByTag(normalized, pageable);
        return buildPostSummaryPage(postIdsPage, pageable, currentUser);
    }

    public Page<PostSummary> searchPostsHybrid(String query, String tag, Pageable pageable, User currentUser) {
        if (query == null || query.trim().isEmpty() || tag == null || tag.trim().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        List<Float> embedding = textEmbeddingService.embed(query.trim());
        String normalizedTag = tag.trim().toLowerCase();
        Page<Integer> postIdsPage = postRepository.findPostIdsByHybrid(embedding, normalizedTag, pageable);
        return buildPostSummaryPage(postIdsPage, pageable, currentUser);
    }

    // Common builder (exactly like ExploreService)
    private Page<PostSummary> buildPostSummaryPage(Page<Integer> postIdsPage, Pageable pageable, User currentUser) {
        List<Integer> postIds = postIdsPage.getContent();
        if (postIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, postIdsPage.getTotalElements());
        }

        List<Post> posts = postRepository.findByPostIdInWithDetails(postIds);
        Map<Integer, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getPostId, p -> p));

        List<Post> orderedPosts = postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Integer, Long> likeCounts = loadLikeCounts(postIds);
        Set<Integer> likedSet = currentUser != null ? loadLikedIds(postIds, currentUser.getUserId()) : Set.of();
        Set<Integer> savedSet = currentUser != null ? loadSavedIds(postIds, currentUser.getUserId()) : Set.of();
        Set<Integer> followedAuthors = currentUser != null ? loadFollowedAuthors(orderedPosts, currentUser) : Set.of();

        List<PostSummary> summaries = orderedPosts.stream().map(post -> {
            User author = post.getUser();
            boolean followingAuthor = author != null && followedAuthors.contains(author.getUserId());
            boolean hasLiked = likedSet.contains(post.getPostId());
            boolean hasSaved = savedSet.contains(post.getPostId());
            long likeCount = likeCounts.getOrDefault(post.getPostId(), 0L);

            return toPostSummaryDto(post, hasLiked, hasSaved, followingAuthor, likeCount);
        }).collect(Collectors.toList());

        return new PageImpl<>(summaries, pageable, postIdsPage.getTotalElements());
    }

    // ===================== USER SEARCH =====================

    public Page<UserSearchResult> searchUsers(String query, boolean mechanicOnly, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        Page<User> userPage = userRepository.searchUsersPaged(query.trim(), mechanicOnly, pageable);

        List<UserSearchResult> results = userPage.getContent().stream()
                .map(u -> new UserSearchResult(
                        u.getUserId(),
                        u.getName(),
                        u.getProfilePic(),
                        u.getUserRoles() != null && Boolean.TRUE.equals(u.getUserRoles().getIsMechanic())
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(results, pageable, userPage.getTotalElements());
    }

    // ===================== PRIVATE HELPERS (copied from ExploreService) =====================

    private Map<Integer, Long> loadLikeCounts(List<Integer> postIds) {
        List<Object[]> raw = postRepository.findLikeCountsByPostIds(postIds);
        return raw.stream().collect(Collectors.toMap(o -> (Integer) o[0], o -> (Long) o[1]));
    }

    private Set<Integer> loadLikedIds(List<Integer> postIds, Integer userId) {
        List<Integer> ids = postRepository.findLikedPostIdsForUser(postIds, userId);
        return new HashSet<>(ids);
    }

    private Set<Integer> loadSavedIds(List<Integer> postIds, Integer userId) {
        List<Integer> ids = postRepository.findSavedPostIdsForUser(postIds, userId);
        return new HashSet<>(ids);
    }

    private Set<Integer> loadFollowedAuthors(List<Post> posts, User currentUser) {
        List<Integer> authorIds = posts.stream()
                .map(p -> p.getUser() != null ? p.getUser().getUserId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (authorIds.isEmpty()) return Set.of();
        List<Integer> followed = userRepository.findFollowedAuthorIds(currentUser.getUserId(), authorIds);
        return new HashSet<>(followed);
    }

    private PostSummary toPostSummaryDto(Post p, boolean hasLiked, boolean hasSaved, boolean followingAuthor, long likeCount) {
        PostSummary summary = new PostSummary();
        User author = p.getUser();

        if (author != null) {
            summary.setAuthorId(author.getUserId());
            summary.setCreatedBy(author.getName());
            summary.setCreatedByProfilePicUrl(author.getProfilePic());
            summary.setAuthorIsMechanic(author.getUserRoles() != null && Boolean.TRUE.equals(author.getUserRoles().getIsMechanic()));
        } else {
            summary.setAuthorId(null);
            summary.setCreatedBy(null);
            summary.setCreatedByProfilePicUrl(null);
            summary.setAuthorIsMechanic(false);
        }

        summary.setPostId(p.getPostId());
        summary.setDescription(p.getDescription());
        summary.setCreatedAt(p.getCreatedAt());
        summary.setLikeCount(Math.toIntExact(likeCount));

        List<String> imageUrls = p.getImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());
        summary.setImageUrls(imageUrls);

        summary.setHasLiked(hasLiked);
        summary.setHasSaved(hasSaved);
        summary.setFollowingAuthor(followingAuthor);
        return summary;
    }
}