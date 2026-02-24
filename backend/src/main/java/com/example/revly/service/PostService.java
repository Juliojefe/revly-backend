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
    public PostSummary getPostSummaryById(int postId, User u) {

        Optional<Post> OptPost = postRepository.findById(postId);
        if (OptPost.isEmpty()) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }
        Post p = OptPost.get();

        Boolean followingAuthor, hasLiked, hasSaved;
        if (u != null) {
         followingAuthor = p.getUser().getFollowers().contains(u);
         hasLiked = u.getLikedPosts().contains(p);
         hasSaved = u.getSavedPosts().contains(p);
        } else {
            followingAuthor = false;
            hasLiked = false;
            hasSaved = false;
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
        List<PostImage> postImages = new ArrayList<>();
        for (String imageUrl : request.getImages()) {
            PostImage postImage = new PostImage();
            postImage.setImageUrl(imageUrl);
            postImage.setPost(post);
            postImages.add(postImage);
        }
        post.setPostImages(postImages);
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
        List<PostImage> postImages = new ArrayList<>();
        for (MultipartFile image : requestImages.getImages()) {
            String imageUrl = fileUploadService.uploadFile(image);
            PostImage postImage = new PostImage();
            postImage.setImageUrl(imageUrl);
            postImage.setPost(post);
            postImages.add(postImage);
        }
        post.setPostImages(postImages);
        Post savedPost = postRepository.save(post);
        return new CreatePostConfirmation(true, "Post uploaded successfully!");
    }

    private PostSummary toPostSummaryDto(Post p, Boolean hasLiked, Boolean hasSaved, Boolean followingAuthor) {
        PostSummary summary = new PostSummary();
        User user = p.getUser();
        if (user != null) { //  user exists case
            summary.setAuthorId(user.getUserId());
            summary.setCreatedBy(user.getName());
            summary.setCreatedByProfilePicUrl(user.getProfilePic());
        } else {    //  user is null case such as when user has deleted their account
            summary.setAuthorId(null);
            summary.setCreatedBy(null);
            summary.setCreatedByProfilePicUrl(null);
        }

        summary.setPostId(p.getPostId());
        summary.setDescription(p.getDescription());
        summary.setCreatedAt(p.getCreatedAt());
        summary.setLikeCount(p.getLikers().size());

        List<String> imageUrls = new ArrayList<>();
        for (PostImage img : p.getPostImages()) {
            imageUrls.add(img.getImageUrl());   //  putting imageUrls in a List
        }
        summary.setImageUrls(imageUrls);

        summary.setHasLiked(hasLiked);
        summary.setHasSaved(hasSaved);
        summary.setAuthorIsMechanic(p.getUser().getUserRoles().getIsMechanic());
        return summary;
    }
}