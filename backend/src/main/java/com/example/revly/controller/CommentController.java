package com.example.revly.controller;

import com.example.revly.dto.request.CommentCreateRequestDTO;
import com.example.revly.dto.request.CommentDeleteRequestDTO;
import com.example.revly.dto.request.CommentUpdateRequestDTO;
import com.example.revly.dto.response.CommentResponseDTO;
import com.example.revly.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponseDTO> createComment(
            @RequestPart("dto") CommentCreateRequestDTO dto,
            @RequestPart(value = "images", required = false) MultipartFile[] images) throws IOException {
        CommentResponseDTO response = commentService.createComment(dto, images);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<CommentResponseDTO> updateComment(
            @RequestPart("dto") CommentUpdateRequestDTO dto,
            @RequestPart(value = "newImages", required = false) MultipartFile[] newImages) throws IOException {
        CommentResponseDTO response = commentService.updateComment(dto, newImages);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteComment(@RequestBody CommentDeleteRequestDTO dto) {
        commentService.deleteComment(dto.getCommentId(), dto.getUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<CommentResponseDTO>> getCommentsByPost(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<CommentResponseDTO> comments = commentService.getCommentsByPostId(postId, page, size);
        return ResponseEntity.ok(comments);
    }
}