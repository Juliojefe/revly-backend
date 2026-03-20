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
import com.example.revly.repository.PostEmbeddingJobRepository;
import com.example.revly.repository.TagRepository;
import com.example.revly.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.revly.repository.PostRepository;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

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
    private PostEmbeddingJobRepository postEmbeddingJobRepository;

    @Autowired
    private TagNormalizationService tagNormalizationService;

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
        List<Integer> ids = followingPosts.stream()
                .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
                .map(Post::getPostId)
                .collect(Collectors.toList());
        return ids;
    }

    public Set<Integer> getOwnedPostByUserId(int userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User u = optUser.get();
        Set<Post> ownedPosts = u.getOwnedPosts();
        Set<Integer> ids = new HashSet<>();
        for (Post p : ownedPosts) {
            ids.add(p.getPostId());
        }
        return ids;
    }

    public Set<Integer> getLikedPostByUserId(int userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User u = optUser.get();
        Set<Post> ownedPosts = u.getLikedPosts();
        Set<Integer> ids = new HashSet<>();
        for (Post p : ownedPosts) {
            ids.add(p.getPostId());
        }
        return ids;
    }

    public Set<Integer> getSavedPostsByUserId(int userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User u = optUser.get();
        Set<Post> ownedPosts = u.getSavedPosts();
        Set<Integer> ids = new HashSet<>();
        for (Post p : ownedPosts) {
            ids.add(p.getPostId());
        }
        return ids;
    }

    @Transactional
    public void likePost(int postId, int userId) {
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

        // Build PostImage entities (URLs already provided)
        List<PostImage> postImages = new ArrayList<>();
        for (String imageUrl : request.getImages()) {
            PostImage pi = new PostImage();
            pi.setImageUrl(imageUrl);
            postImages.add(pi);
        }

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

        // Upload files and build PostImage entities
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

        // Owner check
        if (post.getUser() == null || !Objects.equals(post.getUser().getUserId(), userId)) {
            throw new UnauthorizedException("Only the post owner can update this post");
        }

        String newDescription = request.getDescription();
        List<String> newTags = request.getTags();

        boolean descriptionChanged = false;

        // 1. Description update (if provided)
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

        // 2. Tags update (if provided → full replace)
        if (newTags != null) {
            syncPostTags(post, newTags);
        }

        // 3. If description changed → prepare new embedding version
        if (descriptionChanged) {
            prepareNewEmbeddingVersion(post);
        }

        // 4. Save (cascades tags + searchDocument)
        Post savedPost = postRepository.save(post);

        // 5. If description changed → enqueue fresh job (new version)
        if (descriptionChanged) {
            enqueueNewEmbeddingJob(savedPost);
        }

        return new CreatePostConfirmation(true, "Post updated successfully!");
    }

    // Helper: Sync tags exactly like creation (upsert + remove obsolete)
    private void syncPostTags(Post post, List<String> rawTags) {
        Set<String> normalized = tagNormalizationService.normalizeTags(rawTags);

        // Remove tags that are no longer wanted
        post.getTags().removeIf(tag -> !normalized.contains(tag.getTagName()));

        // Add missing tags (upsert)
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

    // Helper: Increment version, clear old embedding, set pending
    // (handles legacy posts that never had a search document)
    private void prepareNewEmbeddingVersion(Post post) {
        PostSearchDocument doc = post.getSearchDocument();

        if (doc == null) {
            // Legacy post – create fresh document
            doc = new PostSearchDocument();
            doc.setPost(post);
            doc.setDescriptionVersion(1);
            doc.setEmbeddingStatus("pending");
            doc.setEmbeddingModelKey("post_description_embedding_v1");
            post.setSearchDocument(doc);
        } else {
            // Normal case – bump version and clear stale embedding
            doc.setDescriptionVersion(doc.getDescriptionVersion() + 1);
            doc.setEmbeddingStatus("pending");
            doc.setDescriptionEmbedding(null);          // never keep old vector
            doc.setEmbeddingUpdatedAt(null);
        }
    }

    // Helper: Enqueue job for the NEW version
    private void enqueueNewEmbeddingJob(Post post) {
        PostSearchDocument doc = post.getSearchDocument(); // guaranteed to exist now

        PostEmbeddingJob job = new PostEmbeddingJob();
        job.setPost(post);
        job.setDescriptionVersion(doc.getDescriptionVersion());
        job.setStatus("pending");
        job.setNextAttemptAt(Instant.now());
        postEmbeddingJobRepository.save(job);
    }

    private Post createPostCore(String description, Instant createdAt, List<PostImage> postImages, List<String> rawTags, User user) {
        Post post = new Post();
        post.setDescription(description);
        post.setUser(user);
        post.setCreatedAt(createdAt != null ? createdAt : Instant.now());

        // 2. Insert images (bidirectional relationship)
        post.setPostImages(postImages);
        for (PostImage img : postImages) {
            img.setPost(post);
        }

        // 3. Normalize + upsert tags → post_tag (using shared service)
        Set<String> normalizedTags = tagNormalizationService.normalizeTags(rawTags);
        for (String normTag : normalizedTags) {
            Tag tag = tagRepository.findByTagName(normTag)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setTagName(normTag);
                        return tagRepository.save(newTag);   // created_at defaults in DB
                    });
            post.getTags().add(tag);   // adds to post_tag join table on save
        }

        // 4. Insert post_search_document (pending, version 1)
        PostSearchDocument searchDoc = new PostSearchDocument();
        searchDoc.setCreatedAt(post.getCreatedAt());
        searchDoc.setUpdatedAt(Instant.now());
        searchDoc.setPost(post);
        searchDoc.setDescriptionVersion(1);
        searchDoc.setEmbeddingStatus("pending");
        searchDoc.setEmbeddingModelKey("post_description_embedding_v1");
        // description_embedding = null, embedding_updated_at = null (defaults)
        post.setSearchDocument(searchDoc);

        // 1+4+3 → single save (cascades images + searchDocument + post_tag)
        Post savedPost = postRepository.save(post);

        // 5. Insert post_embedding_job
        PostEmbeddingJob job = new PostEmbeddingJob();
        job.setPost(savedPost);
        job.setDescriptionVersion(1);
        job.setStatus("pending");
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        job.setNextAttemptAt(Instant.now());
        postEmbeddingJobRepository.save(job);

        return savedPost;
    }

    private PostSummary toPostSummaryDto(Post p, boolean hasLiked, boolean hasSaved, boolean followingAuthor) {
        PostSummary summary = new PostSummary();
        User author = p.getUser();

        if (author != null) {
            summary.setAuthorId(author.getUserId());
            summary.setCreatedBy(author.getName());
            summary.setCreatedByProfilePicUrl(author.getProfilePic());
            boolean  isMechanic = author.getUserRoles() != null&& Boolean.TRUE.equals(author.getUserRoles().getIsMechanic());
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

        List<String> imageUrls = new ArrayList<>();
        if (p.getPostImages() != null) {
            for (PostImage img : p.getPostImages()) {
                if (img != null && img.getImageUrl() != null) {
                    imageUrls.add(img.getImageUrl());
                }
            }
        }
        summary.setImageUrls(imageUrls);
        summary.setHasLiked(hasLiked);
        summary.setHasSaved(hasSaved);
        summary.setFollowingAuthor(followingAuthor);
        return summary;
    }
}