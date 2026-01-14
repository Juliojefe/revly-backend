package com.example.revly.service;

import com.example.revly.dto.request.CreatePostRequestImages;
import com.example.revly.dto.request.CreatePostRequestUrl;
import com.example.revly.dto.response.PostSummary;
import com.example.revly.exception.BadRequestException;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.model.Post;
import com.example.revly.model.PostImage;
import com.example.revly.dto.response.CreatePostConfirmation;
import com.example.revly.model.User;
import com.example.revly.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Transactional(readOnly = true)
    public PostSummary getPostSummaryById(int postId) {
        Optional<Post> OptPost = postRepository.findById(postId);
        if (OptPost.isEmpty()) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }
        Post p = OptPost.get();
        return new PostSummary(p);
    }

    public Set<Integer> getAllPostIds() {
        List<Post> allPosts = postRepository.findAll();
        Set<Integer> ids = new HashSet<>();
        for (Post p : allPosts) {
            ids.add(p.getPostId());
        }
        return ids;
    }

    public Page<PostSummary> getExplorePosts(Pageable pageable, User u) {
        Set<Integer> excludedUserIds = new HashSet<>();
        for (User followedUser : u.getFollowing()) {
            excludedUserIds.add(followedUser.getUserId());
        }
        excludedUserIds.add(u.getUserId()); // Add the current user to the excluded set
        Page<Post> posts = postRepository.findByUserUserIdNotIn(excludedUserIds, pageable);
        List<PostSummary> summaries = new ArrayList<>();
        for (Post post : posts.getContent()) {
            summaries.add(new PostSummary(post));
        }
        return new PageImpl<>(summaries, pageable, posts.getTotalElements());
    }

    public Page<PostSummary> getExplorePostsGuest(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        List<PostSummary> summaries = new ArrayList<>();
        for (Post post : posts.getContent()) {
            summaries.add(new PostSummary(post));
        }
        return new PageImpl<>(summaries, pageable, posts.getTotalElements());
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

    public CreatePostConfirmation createPost(CreatePostRequestUrl request, int userId) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (request.getDescription().isEmpty()) {
            return new CreatePostConfirmation(false, "Description must not be empty");
        }
        User u = optUser.get();
        Post post = new Post();
        post.setDescription(request.getDescription());
        post.setUser(u);
        post.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt() : Instant.now());
        Set<PostImage> postImages = new HashSet<>();
        for (String imageUrl : request.getImages()) {
            PostImage postImage = new PostImage();
            postImage.setImageUrl(imageUrl);
            postImage.setPost(post);
            postImages.add(postImage);
        }
        post.setImages(postImages);
        Post savedPost = postRepository.save(post);
        return new CreatePostConfirmation(true, "Post uploaded successfully!");
    }

    public CreatePostConfirmation createPost(CreatePostRequestImages requestImages, int userId) throws IOException {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (requestImages.getDescription().isEmpty()) {
            return new CreatePostConfirmation(false, "Description must not be empty");
        }
        User u = optUser.get();
        Post post = new Post();
        post.setDescription(requestImages.getDescription());
        post.setUser(u);
        post.setCreatedAt(requestImages.getCreatedAt() != null ? requestImages.getCreatedAt() : Instant.now());
        Set<PostImage> postImages = new HashSet<>();
        for (MultipartFile image : requestImages.getImages()) {
            String imageUrl = fileUploadService.uploadFile(image);
            PostImage postImage = new PostImage();
            postImage.setImageUrl(imageUrl);
            postImage.setPost(post);
            postImages.add(postImage);
        }
        post.setImages(postImages);
        Post savedPost = postRepository.save(post);
        return new CreatePostConfirmation(true, "Post uploaded successfully!");
    }

}