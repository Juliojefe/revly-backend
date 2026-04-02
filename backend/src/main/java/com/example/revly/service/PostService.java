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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

@Service
public class PostService {

    private static final long MAX_TOTAL_IMAGES_SIZE_BYTES = 9L * 1024 * 1024; // 9 MB total per post
    private static final long MIN_IMAGE_SIZE_BYTES = 150 * 1024;               // never go below ~150 KB per image
    private static final int MAX_IMAGE_WIDTH = 1920;
    private static final int MAX_IMAGE_HEIGHT = 1080;
    private static final float MILD_COMPRESSION_QUALITY = 0.85f;   // normal uploads
    private static final float AGGRESSIVE_COMPRESSION_QUALITY = 0.65f; // when over limit

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
        if (optPost.isEmpty()) return new PostSummary();
        Post p = optPost.get();
        User author = p.getUser();
        boolean followingAuthor = false;
        boolean hasLiked = false;
        boolean hasSaved = false;
        if (u != null) {
            if (author != null && author.getFollowers() != null) followingAuthor = author.getFollowers().contains(u);
            if (u.getLikedPosts() != null) hasLiked = u.getLikedPosts().contains(p);
            if (u.getSavedPosts() != null) hasSaved = u.getSavedPosts().contains(p);
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
        if (optUser.isEmpty()) throw new ResourceNotFoundException("User not found with id: " + userId);
        if (request.getDescription().isEmpty()) return new CreatePostConfirmation(false, "Description must not be empty");

        User user = optUser.get();
        List<PostImage> postImages = request.getImages().stream()
                .map(url -> { PostImage pi = new PostImage(); pi.setImageUrl(url); return pi; })
                .collect(Collectors.toList());

        Post savedPost = createPostCore(request.getDescription(), request.getCreatedAt(), postImages, request.getTags(), user);
        return new CreatePostConfirmation(true, "Post uploaded successfully!");
    }

    @Transactional
    public CreatePostConfirmation createPost(CreatePostRequestImages requestImages, int userId) throws IOException {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) throw new ResourceNotFoundException("User not found with id: " + userId);
        if (requestImages.getDescription().isEmpty())
            return new CreatePostConfirmation(false, "Description must not be empty");

        User user = optUser.get();

        // ====================== SMART COMPRESSION ======================
        List<MultipartFile> originalImages = requestImages.getImages();
        long totalOriginalSize = originalImages.stream().mapToLong(MultipartFile::getSize).sum();

        boolean isOverLimit = totalOriginalSize > MAX_TOTAL_IMAGES_SIZE_BYTES;
        float quality = isOverLimit ? AGGRESSIVE_COMPRESSION_QUALITY : MILD_COMPRESSION_QUALITY;

        List<PostImage> postImages = new ArrayList<>();

        for (MultipartFile file : originalImages) {
            // compress (and resize if needed)
            byte[] compressedBytes = compressImage(file, quality);

            // upload the compressed version
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
            String fileName = UUID.randomUUID().toString() + "_" + originalName;

            String imageUrl = fileUploadService.uploadBytes(compressedBytes, fileName);

            PostImage pi = new PostImage();
            pi.setImageUrl(imageUrl);
            postImages.add(pi);
        }

        Post savedPost = createPostCore(requestImages.getDescription(), requestImages.getCreatedAt(), postImages, requestImages.getTags(), user);
        return new CreatePostConfirmation(true, "Post uploaded successfully!");
    }

    @Transactional
    public CreatePostConfirmation updatePost(Integer postId, UpdatePostRequest request, int userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        if (post.getUser() == null || !Objects.equals(post.getUser().getUserId(), userId)) {
            throw new UnauthorizedException("Only the post owner can update this post");
        }

        String newDescription = request.getDescription();
        List<String> newTags = request.getTags();
        boolean descriptionChanged = false;

        if (newDescription != null) {
            String trimmed = newDescription.trim();
            if (trimmed.isEmpty()) return new CreatePostConfirmation(false, "Description must not be empty");
            if (!trimmed.equals(post.getDescription())) {
                post.setDescription(trimmed);
                descriptionChanged = true;
            }
        }

        if (newTags != null) syncPostTags(post, newTags);

        if (descriptionChanged) {
            List<Float> embedding = textEmbeddingService.embed(newDescription);
            post.setDescriptionEmbedding(embedding);
            post.setEmbeddingUpdatedAt(Instant.now());
        }

        postRepository.save(post);
        return new CreatePostConfirmation(true, "Post updated successfully!");
    }

    private Post createPostCore(String description, Instant createdAt, List<PostImage> postImages, List<String> rawTags, User user) {
        Post post = new Post();
        post.setDescription(description);
        post.setUser(user);
        post.setCreatedAt(createdAt != null ? createdAt : Instant.now());

        List<Float> embedding = textEmbeddingService.embed(description);
        post.setDescriptionEmbedding(embedding);
        post.setEmbeddingUpdatedAt(Instant.now());

        post.setImages(postImages);
        for (PostImage img : postImages) img.setPost(post);

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
            summary.setAuthorId(null); summary.setCreatedBy(null); summary.setCreatedByProfilePicUrl(null); summary.setAuthorIsMechanic(false);
        }
        summary.setPostId(p.getPostId());
        summary.setDescription(p.getDescription());
        summary.setCreatedAt(p.getCreatedAt());
        summary.setLikeCount(p.getLikers() == null ? 0 : p.getLikers().size());
        List<String> imageUrls = p.getImages().stream().map(PostImage::getImageUrl).collect(Collectors.toList());
        summary.setImageUrls(imageUrls);
        summary.setHasLiked(hasLiked);
        summary.setHasSaved(hasSaved);
        summary.setFollowingAuthor(followingAuthor);
        return summary;
    }

    //  image compression/resize
    private byte[] compressImage(MultipartFile file, float quality) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            throw new IOException("Invalid image file");
        }

        // Resize while keeping aspect ratio
        BufferedImage resized = resizeImage(original, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);

        // Write with JPEG compression (best size/quality ratio for social posts)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }

        writer.setOutput(ImageIO.createImageOutputStream(baos));
        writer.write(null, new IIOImage(resized, null, null), param);
        writer.dispose();

        byte[] result = baos.toByteArray();

        // Safety: never let any single image go under minimum size (rare, but possible with tiny originals)
        if (result.length < MIN_IMAGE_SIZE_BYTES) {
            // fallback: re-compress with higher quality if too small
            param.setCompressionQuality(0.92f);
            baos.reset();
            writer = ImageIO.getImageWritersByFormatName("jpg").next();
            writer.setOutput(ImageIO.createImageOutputStream(baos));
            writer.write(null, new IIOImage(resized, null, null), param);
            writer.dispose();
            result = baos.toByteArray();
        }

        return result;
    }

    private BufferedImage resizeImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        double ratio = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        if (ratio >= 1.0) return original; // no need to resize

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resized;
    }
}