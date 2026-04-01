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
    @Autowired
    private TagNormalizationService tagNormalizationService;

    public Page<PostSummary> searchPostsByText(String query, Pageable pageable, User currentUser) {
        if (query == null || query.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        float[] embedding = toFloatArray(textEmbeddingService.embed(query.trim()));
        Page<Integer> postIdsPage = postRepository.findPostIdsBySemanticSimilarity(embedding, pageable);
        return buildPostSummaryPageFromIds(postIdsPage, pageable, currentUser);
    }

    public Page<PostSummary> searchPostsByTag(String tagInput, Pageable pageable, User currentUser) {
        if (tagInput == null || tagInput.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        Set<String> normalizedTags = normalizeTags(tagInput);
        if (normalizedTags.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Integer> postIdsPage = postRepository.findPostIdsByAnyTag(normalizedTags, pageable);
        return buildPostSummaryPageFromIds(postIdsPage, pageable, currentUser);
    }

    public Page<PostSummary> searchPostsHybrid(String query, String tagInput, Pageable pageable, User currentUser) {
        if (query == null || query.trim().isEmpty() || tagInput == null || tagInput.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        float[] embedding = toFloatArray(textEmbeddingService.embed(query.trim()));
        Set<String> normalizedTags = normalizeTags(tagInput);
        if (normalizedTags.isEmpty()) {
            return Page.empty(pageable);
        }
        Page<Integer> postIdsPage = postRepository.findPostIdsByHybridAnyTags(embedding, normalizedTags, pageable);
        return buildPostSummaryPageFromIds(postIdsPage, pageable, currentUser);
    }

    public Page<UserSearchResult> searchUsers(String query, boolean mechanicOnly, Pageable pageable, User currentUser) {
        if (query == null || query.trim().isEmpty()) {
            return Page.empty(pageable);
        }
        Page<User> userPage = userRepository.searchUsersPaged(query.trim(), mechanicOnly, pageable);
        List<UserSearchResult> results = userPage.getContent().stream()
                .map(u -> buildUserSearchResult(u, currentUser))
                .collect(Collectors.toList());
        return new PageImpl<>(results, pageable, userPage.getTotalElements());
    }

    /** Converts OpenAI embedding (List<Float>) to primitive float[] required by pgvector queries */
    private float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private Set<String> normalizeTags(String input) {
        List<String> rawTags = Arrays.asList(input.trim().split("\\s+"));
        return tagNormalizationService.normalizeTags(rawTags);
    }

    /** Builds a single UserSearchResult, correctly handling authenticated vs guest users */
    private UserSearchResult buildUserSearchResult(User user, User currentUser) {
        boolean isFollowing = false;
        if (currentUser != null) {
            List<Integer> followed = userRepository.findFollowedAuthorIds(
                    currentUser.getUserId(), List.of(user.getUserId()));
            isFollowing = !followed.isEmpty();
        }
        return new UserSearchResult(
                user.getUserId(),
                user.getName(),
                user.getProfilePic(),
                user.getUserRoles() != null && Boolean.TRUE.equals(user.getUserRoles().getIsMechanic()),
                isFollowing
        );
    }

    private Map<Integer, Long> loadLikeCounts(List<Integer> postIds) {
        List<Object[]> raw = postRepository.findLikeCountsByPostIds(postIds);
        return raw.stream().collect(Collectors.toMap(
                o -> (Integer) o[0],
                o -> (Long) o[1]
        ));
    }

    private Set<Integer> loadLikedIds(List<Integer> postIds, Integer userId) {
        return new HashSet<>(postRepository.findLikedPostIdsForUser(postIds, userId));
    }

    private Set<Integer> loadSavedIds(List<Integer> postIds, Integer userId) {
        return new HashSet<>(postRepository.findSavedPostIdsForUser(postIds, userId));
    }

    private Set<Integer> loadFollowedAuthors(List<Post> posts, User currentUser) {
        List<Integer> authorIds = posts.stream()
                .map(p -> p.getUser() != null ? p.getUser().getUserId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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
            summary.setAuthorIsMechanic(
                    author.getUserRoles() != null && Boolean.TRUE.equals(author.getUserRoles().getIsMechanic()));
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

    /**
     * Core builder used by ALL post searches.
     * IMPORTANT: currentUser (from Principal) is fully taken into account here.
     * For authenticated users → real hasLiked / hasSaved / followingAuthor values.
     * For guests (currentUser == null) → all three flags stay false.
     */
    private Page<PostSummary> buildPostSummaryPageFromIds(Page<Integer> postIdsPage, Pageable pageable, User currentUser) {
        List<Integer> postIds = postIdsPage.getContent();
        if (postIds.isEmpty()) {
            return Page.empty(pageable);
        }
        List<Post> posts = postRepository.findByPostIdInWithDetails(postIds);
        Map<Integer, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getPostId, p -> p));
        List<Post> orderedPosts = postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .toList();
        Map<Integer, Long> likeCounts = loadLikeCounts(postIds);

        // authenticated user
        Set<Integer> likedSet = currentUser != null
                ? loadLikedIds(postIds, currentUser.getUserId())
                : Set.of();

        Set<Integer> savedSet = currentUser != null
                ? loadSavedIds(postIds, currentUser.getUserId())
                : Set.of();

        Set<Integer> followedAuthors = currentUser != null
                ? loadFollowedAuthors(orderedPosts, currentUser)
                : Set.of();

        List<PostSummary> summaries = orderedPosts.stream().map(post -> {
            User author = post.getUser();
            boolean followingAuthor = author != null && followedAuthors.contains(author.getUserId());
            boolean hasLiked = likedSet.contains(post.getPostId());
            boolean hasSaved = savedSet.contains(post.getPostId());
            long likeCount = likeCounts.getOrDefault(post.getPostId(), 0L);

            return toPostSummaryDto(post, hasLiked, hasSaved, followingAuthor, likeCount); }).collect(Collectors.toList());

        return new PageImpl<>(summaries, pageable, postIdsPage.getTotalElements());
    }
}