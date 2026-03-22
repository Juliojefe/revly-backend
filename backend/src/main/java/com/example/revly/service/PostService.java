package com.example.revly.service;

import com.example.revly.dto.request.CreatePostRequestImages;
import com.example.revly.dto.request.CreatePostRequestUrl;
import com.example.revly.dto.request.UpdatePostRequest;
import com.example.revly.dto.response.PostSummary;
import com.example.revly.exception.BadRequestException;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.*;
import com.example.revly.dto.response.CreatePostConfirmation;
import com.example.revly.repository.PostRepository;
import com.example.revly.repository.TagRepository;
import com.example.revly.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagNormalizationService tagNormalizationService;

    @Autowired
    private TextEmbeddingService textEmbeddingService;

    @Transactional(readOnly = true)
    public PostSummary getPostSummaryById(int postId, User u) {
        Optional<Post> optPost = postRepository.findById(postId);
        if (optPost.isEmpty()) {
            return new PostSummary();
        }

        Post p = optPost.get();
        User author = p.getUser();

        boolean followingAuthor = false;
        boolean hasLiked = false;
        boolean hasSaved = false;

        if (u != null) {
            if (author != null && author.getFollowers() != null) {
                followingAuthor = author.getFollowers().contains(u);
            }
            if (u.getLikedPosts() != null) {
                hasLiked = u.getLikedPosts().contains(p);
            }
            if (u.getSavedPosts() != null) {
                hasSaved = u.getSavedPosts().contains(p);
            }
        }

        return toPostSummaryDto(p, hasLiked, hasSaved, followingAuthor);
    }

    public Set<Integer> getAllPostIds() {
        List<Post> allPosts = postRepository.findAll();
        Set<Integer> ids = new HashSet<>();
        for (Post p : allPosts) {
            ids.add(p.getPostId());
        }
        return ids;
    }

    public List<Integer> getFollowingPostIds(User u) {
        Set<User> followedUsers = u.getFollowing();
        List<Post> followingPosts = new ArrayList<>();
        for (User followed : followedUsers) {
            followingPosts.addAll(followed.getOwnedPosts());
        }
        return followingPosts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .map(Post::getPostId)
                .collect(Collectors.toList());
    }

    public Set<Integer> getOwnedPostByUserId(int userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User u = optUser.get();
        return u.getOwnedPosts().stream()
                .map(Post::getPostId)
                .collect(Collectors.toSet());
    }

    public Set<Integer> getLikedPostByUserId(int userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User u = optUser.get();
        return u.getLikedPosts().stream()
                .map(Post::getPostId)
                .collect(Collectors.toSet());
    }

    public Set<Integer> getSavedPostsByUserId(int userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User u = optUser.get();
        return u.getSavedPosts().stream()
                .map(Post::getPostId)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void likePost(int postId, int userId) {
        // ... (unchanged - same as before)
        Optional<Post> optPost = postRepository.findById(postId);
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty() || optPost.isEmpty()) {
            throw new ResourceNotFoundException("Post or user not found");
        }
        Post p = optPost.get();
        User u = optUser.get();
        if (p.getLikers() == null) p.setLikers(new HashSet<>());
        if (u.getLikedPosts() == null) u.setLikedPosts(new HashSet<>());

        if (p.getLikers().contains(u)) {
            throw new BadRequestException("Already liked");
        }
        p.getLikers().add(u);
        u.getLikedPosts().add(p);
    }

    @Transactional
    public void savePost(int postId, int userId) {
        Optional<Post> optPost = postRepository.findById(postId);
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty() || optPost.isEmpty()) {
            throw new ResourceNotFoundException("Post or user not found");
        }
        Post p = optPost.get();
        User u = optUser.get();
        if (p.getSavers() == null) p.setSavers(new HashSet<>());
        if (u.getSavedPosts() == null) u.setSavedPosts(new HashSet<>());

        if (p.getSavers().contains(u)) {
            throw new BadRequestException("Already saved");
        }
        p.getSavers().add(u);
        u.getSavedPosts().add(p);
    }

    @Transactional
    public void unlikePost(int postId, int userId) {
        Optional<Post> optPost = postRepository.findById(postId);
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty() || optPost.isEmpty()) {
            throw new ResourceNotFoundException("Post or user not found");
        }
        Post p = optPost.get();
        User u = optUser.get();
        if (p.getLikers() != null && u.getLikedPosts() != null && p.getLikers().contains(u)) {
            p.getLikers().remove(u);
            u.getLikedPosts().remove(p);
            return;
        }
        throw new BadRequestException("Not liked");
    }

    @Transactional
    public void unSavePost(int postId, int userId) {
        Optional<Post> optPost = postRepository.findById(postId);
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty() || optPost.isEmpty()) {
            throw new ResourceNotFoundException("Post or user not found");
        }
        Post p = optPost.get();
        User u = optUser.get();
        if (p.getSavers() != null && u.getSavedPosts() != null && p.getSavers().contains(u)) {
            p.getSavers().remove(u);
            u.getSavedPosts().remove(p);
            return;
        }
        throw new BadRequestException("Not saved");
    }

    @Transactional
    public CreatePostConfirmation createPost(CreatePostRequestUrl request, int userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (request.getDescription().isEmpty()) {
            return new CreatePostConfirmation(false, "Description must not be empty");
        }

        User user = optUser.get();
        List<PostImage> postImages = request.getImages().stream()
                .map(url -> {
                    PostImage pi = new PostImage();
                    pi.setImageUrl(url);
                    return pi;
                })
                .collect(Collectors.toList());

        Post savedPost = createPostCore(request.getDescription(), request.getCreatedAt(), postImages, request.getTags(), user);
        return new CreatePostConfirmation(true, "Post uploaded successfully!");
    }

    @Transactional
    public CreatePostConfirmation createPost(CreatePostRequestImages requestImages, int userId) throws IOException {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (requestImages.getDescription().isEmpty()) {
            return new CreatePostConfirmation(false, "Description must not be empty");
        }

        User user = optUser.get();

        List<PostImage> postImages = new ArrayList<>();
        for (MultipartFile file : requestImages.getImages()) {
            String imageUrl = fileUploadService.uploadFile(file);
            PostImage pi = new PostImage();
            pi.setImageUrl(imageUrl);
            postImages.add(pi);
        }

        Post savedPost = createPostCore(requestImages.getDescription(), requestImages.getCreatedAt(), postImages, requestImages.getTags(), user);
        return new CreatePostConfirmation(true, "Post uploaded successfully!");
    }

    @Transactional
    public CreatePostConfirmation updatePost(Integer postId, UpdatePostRequest request, int userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getUser() == null || !Objects.equals(post.getUser().getUserId(), userId)) {
            throw new UnauthorizedException("Only the post owner can update this post");
        }

        String newDescription = request.getDescription();
        List<String> newTags = request.getTags();

        boolean descriptionChanged = false;

        // 1. Description update
        if (newDescription != null) {
            String trimmed = newDescription.trim();
            if (trimmed.isEmpty()) {
                return new CreatePostConfirmation(false, "Description must not be empty");
            }
            if (!trimmed.equals(post.getDescription())) {
                post.setDescription(trimmed);
                descriptionChanged = true;
            }
        }

        // 2. Tags update (full replace)
        if (newTags != null) {
            syncPostTags(post, newTags);
        }

        // 3. Generate fresh embedding if description changed
        if (descriptionChanged) {
            List<Float> embeddingList = textEmbeddingService.embed(newDescription);
            post.setDescriptionEmbedding(toFloatArray(embeddingList));
            post.setEmbeddingUpdatedAt(Instant.now());
        }

        Post savedPost = postRepository.save(post);
        return new CreatePostConfirmation(true, "Post updated successfully!");
    }

    // Helper: Sync tags
    private void syncPostTags(Post post, List<String> rawTags) {
        Set<String> normalized = tagNormalizationService.normalizeTags(rawTags);

        post.getTags().removeIf(tag -> !normalized.contains(tag.getTagName()));

        for (String normTag : normalized) {
            if (post.getTags().stream().noneMatch(t -> t.getTagName().equals(normTag))) {
                Tag tag = tagRepository.findByTagName(normTag)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setTagName(normTag);
                            return tagRepository.save(newTag);
                        });
                post.getTags().add(tag);
            }
        }
    }

    // Core creation logic
    private Post createPostCore(String description, Instant createdAt, List<PostImage> postImages, List<String> rawTags, User user) {
        Post post = new Post();
        post.setDescription(description);
        post.setUser(user);
        post.setCreatedAt(createdAt != null ? createdAt : Instant.now());

        // Generate embedding BEFORE saving (one transaction, one DB write)
        List<Float> embeddingList = textEmbeddingService.embed(description);
        post.setDescriptionEmbedding(toFloatArray(embeddingList));
        post.setEmbeddingUpdatedAt(Instant.now());

        // Images
        post.setImages(postImages);
        for (PostImage img : postImages) {
            img.setPost(post);
        }

        // Tags
        Set<String> normalizedTags = tagNormalizationService.normalizeTags(rawTags);
        for (String normTag : normalizedTags) {
            Tag tag = tagRepository.findByTagName(normTag)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setTagName(normTag);
                        return tagRepository.save(newTag);
                    });
            post.getTags().add(tag);
        }

        return postRepository.save(post);
    }

    private PostSummary toPostSummaryDto(Post p, boolean hasLiked, boolean hasSaved, boolean followingAuthor) {
        PostSummary summary = new PostSummary();
        User author = p.getUser();

        if (author != null) {
            summary.setAuthorId(author.getUserId());
            summary.setCreatedBy(author.getName());
            summary.setCreatedByProfilePicUrl(author.getProfilePic());
            boolean isMechanic = author.getUserRoles() != null && Boolean.TRUE.equals(author.getUserRoles().getIsMechanic());
            summary.setAuthorIsMechanic(isMechanic);
        } else {
            summary.setAuthorId(null);
            summary.setCreatedBy(null);
            summary.setCreatedByProfilePicUrl(null);
            summary.setAuthorIsMechanic(false);
        }

        summary.setPostId(p.getPostId());
        summary.setDescription(p.getDescription());
        summary.setCreatedAt(p.getCreatedAt());
        summary.setLikeCount(p.getLikers() == null ? 0 : p.getLikers().size());

        List<String> imageUrls = p.getImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList());
        summary.setImageUrls(imageUrls);

        summary.setHasLiked(hasLiked);
        summary.setHasSaved(hasSaved);
        summary.setFollowingAuthor(followingAuthor);
        return summary;
    }

    private float[] toFloatArray(List<Float> list) {
        if (list == null || list.isEmpty()) return new float[0];
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i) != null ? list.get(i) : 0f;
        }
        return array;
    }
}