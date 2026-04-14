package com.example.revly.controller;

import com.example.revly.dto.request.ReviewCreateRequestDTO;
import com.example.revly.dto.request.ReviewUpdateRequestDTO;
import com.example.revly.dto.request.ReviewDeleteRequestDTO;
import com.example.revly.dto.request.CreateReviewResponseRequestDTO;
import com.example.revly.dto.response.ReviewResponseDTO;
import com.example.revly.dto.response.ReviewResponseItemDTO;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.repository.UserRepository;
import com.example.revly.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserIdFromPrincipal(Principal principal) {
        if (principal == null) throw new UnauthorizedException("User must be authenticated");
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"))
                .getUserId();
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(
            @RequestPart("dto") ReviewCreateRequestDTO dto,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            Principal principal) throws IOException {

        Integer userId = getUserIdFromPrincipal(principal);
        ReviewResponseDTO response = reviewService.createReview(dto, images, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @RequestPart("dto") ReviewUpdateRequestDTO dto,
            @RequestPart(value = "newImages", required = false) MultipartFile[] newImages,
            Principal principal) throws IOException {

        Integer userId = getUserIdFromPrincipal(principal);
        ReviewResponseDTO response = reviewService.updateReview(dto, newImages, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteReview(@RequestBody ReviewDeleteRequestDTO dto, Principal principal) {
        Integer userId = getUserIdFromPrincipal(principal);
        reviewService.deleteReview(dto.getReviewId(), userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mechanic/{mechanicId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviewsByMechanic(
            @PathVariable Integer mechanicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        Integer currentUserId = (principal != null) ? getUserIdFromPrincipal(principal) : null;
        Page<ReviewResponseDTO> reviews = reviewService.getReviewsByMechanic(mechanicId, page, size, currentUserId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/mechanic/{mechanicId}/stats")
    public ResponseEntity<Object[]> getReviewStats(@PathVariable Integer mechanicId) {
        Object[] stats = reviewService.getAverageRatingAndCount(mechanicId);
        return ResponseEntity.ok(stats);   // returns [average, count]
    }

    @PostMapping("/response")
    public ResponseEntity<ReviewResponseItemDTO> createReviewResponse(
            @RequestPart("dto") CreateReviewResponseRequestDTO dto,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            Principal principal) throws IOException {

        Integer userId = getUserIdFromPrincipal(principal);
        ReviewResponseItemDTO response = reviewService.createReviewResponse(dto, images, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reviewId}/responses")
    public ResponseEntity<Page<ReviewResponseItemDTO>> getReviewResponses(
            @PathVariable Integer reviewId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ReviewResponseItemDTO> responses = reviewService.getReviewResponses(reviewId, page, size);
        return ResponseEntity.ok(responses);
    }
}