package com.example.revly.service;

import com.example.revly.dto.request.CommentCreateRequestDTO;
import com.example.revly.dto.request.CommentUpdateRequestDTO;
import com.example.revly.dto.response.CommentResponseDTO;
import com.example.revly.model.Comment;
import com.example.revly.model.CommentImage;
import com.example.revly.model.Post;
import com.example.revly.model.User;
import com.example.revly.repository.CommentImageRepository;
import com.example.revly.repository.CommentRepository;
import com.example.revly.repository.PostRepository;
import com.example.revly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentImageRepository commentImageRepository;

    @Autowired
    private FileUploadService fileUploadService;

    public CommentResponseDTO createComment(CommentCreateRequestDTO dto, MultipartFile[] images) throws IOException {
        Post post = postRepository.findById(dto.getPostId()).orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment();
        comment.setContent(dto.getContent());
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : Instant.now());
        comment = commentRepository.save(comment);

        if (images != null) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String url = fileUploadService.uploadFile(image);
                    CommentImage commentImage = new CommentImage();
                    commentImage.setImageUrl(url);
                    commentImage.setComment(comment);
                    commentImageRepository.save(commentImage);
                }
            }
        }
        return mapToResponseDTO(comment);
    }

    //  Only the owner of the comment can updateComment
    public CommentResponseDTO updateComment(CommentUpdateRequestDTO dto, MultipartFile[] newImages) throws IOException {
        Comment comment = commentRepository.findById(dto.getCommentId()).orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findById(dto.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));

        if (!isAuthorizedToModify(comment, user)) {
            throw new RuntimeException("Unauthorized");
        }

        if (dto.getContent() != null) {
            comment.setContent(dto.getContent());
        }

        // Handle images: remove those not in keep list
        List<CommentImage> currentImages = commentImageRepository.findByComment(comment);
        List<String> keepUrls = dto.getExistingImageUrlsToKeep() != null ? dto.getExistingImageUrlsToKeep() : new ArrayList<>();
        currentImages.removeIf(img -> !keepUrls.contains(img.getImageUrl()));
        commentImageRepository.deleteAll(currentImages.stream().filter(img -> !keepUrls.contains(img.getImageUrl())).collect(Collectors.toSet()));

        // Add new images
        if (newImages != null) {
            for (MultipartFile image : newImages) {
                if (!image.isEmpty()) {
                    String url = fileUploadService.uploadFile(image);
                    CommentImage commentImage = new CommentImage();
                    commentImage.setImageUrl(url);
                    commentImage.setComment(comment);
                    commentImageRepository.save(commentImage);
                }
            }
        }

        comment = commentRepository.save(comment);
        return mapToResponseDTO(comment);
    }

    //  comments can be deleted by author, post owner, or admin
    public void deleteComment(Integer commentId, Integer userId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!isAuthorizedToDelete(comment, user)) {
            throw new RuntimeException("Unauthorized");
        }

        commentRepository.delete(comment);
    }

    public Page<CommentResponseDTO> getCommentsByPostId(Integer postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentRepository.findByPost_PostId(postId, pageable);
        return comments.map(this::mapToResponseDTO);
    }

    private boolean isAuthorizedToDelete(Comment comment, User user) {
        boolean isOwner = comment.getUser().getUserId().equals(user.getUserId());
        boolean isAdmin = user.getUserRoles().getIsAdmin();
        boolean isPostOwner = comment.getPost().getUser().getUserId().equals(user.getUserId());
        return isOwner || isAdmin || isPostOwner;
    }

    private boolean isAuthorizedToModify(Comment comment, User user) {
        boolean isOwner = comment.getUser().getUserId().equals(user.getUserId());
        return isOwner;
    }

    private CommentResponseDTO mapToResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setCommentId(comment.getCommentId());
        dto.setAuthorId(comment.getUser().getUserId());
        dto.setCreatedByName(comment.getUser().getName());
        dto.setCreatedByProfilePicUrl(comment.getUser().getProfilePic());
        dto.setContent(comment.getContent());
        List<CommentImage> images = commentImageRepository.findByComment(comment);
        dto.setImageUrls(images.stream().map(CommentImage::getImageUrl).collect(Collectors.toList()));
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}