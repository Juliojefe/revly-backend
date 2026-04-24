package com.example.revly.service;

import com.example.revly.dto.request.ReviewCreateRequestDTO;
import com.example.revly.dto.request.ReviewUpdateRequestDTO;
import com.example.revly.dto.request.ReviewDeleteRequestDTO;
import com.example.revly.dto.request.CreateReviewResponseRequestDTO;
import com.example.revly.dto.response.ReviewResponseDTO;
import com.example.revly.dto.response.ReviewResponseItemDTO;
import com.example.revly.exception.BadRequestException;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.*;
import com.example.revly.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @Autowired
    private ReviewResponseRepository reviewResponseRepository;

    @Autowired
    private ReviewResponseImageRepository reviewResponseImageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Transactional
    public ReviewResponseDTO createReview(ReviewCreateRequestDTO dto, MultipartFile[] images, Integer reviewerId) throws IOException {
        User reviewer = userRepository.findById(reviewerId).orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));
        User mechanic = userRepository.findById(dto.getMechanicId()).orElseThrow(() -> new ResourceNotFoundException("Mechanic not found"));

        if (mechanic.getUserRoles() == null || !Boolean.TRUE.equals(mechanic.getUserRoles().getIsMechanic())) {
            throw new BadRequestException("Can only review mechanics");
        }

        // enforce one review per reviewer-mechanic
        if (reviewRepository.findByReviewer_UserIdAndMechanic_UserId(reviewerId, dto.getMechanicId()).isPresent()) {
            throw new BadRequestException("You already reviewed this mechanic");
        }

        // 5 image max
        if (images != null && images.length > 5) {
            throw new BadRequestException("Maximum 5 images allowed per review");
        }

        Review review = new Review();
        review.setReviewer(reviewer);
        review.setMechanic(mechanic);

        if (dto.getBusinessId() != null) {
            Business business = businessRepository.findById(dto.getBusinessId())
                    .orElseThrow(() -> new ResourceNotFoundException("Business not found with id: " + dto.getBusinessId()));
            review.setBusiness(business);
        }

        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());

        review = reviewRepository.save(review);

        if (images != null) {
            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    String url = fileUploadService.uploadFile(img);
                    ReviewImage ri = new ReviewImage();
                    ri.setImageUrl(url);
                    ri.setReview(review);
                    reviewImageRepository.save(ri);
                }
            }
        }

        return mapToReviewResponseDTO(review, reviewerId);
    }

    @Transactional
    public ReviewResponseDTO updateReview(ReviewUpdateRequestDTO dto, MultipartFile[] newImages, Integer reviewerId) throws IOException {
        Review review = reviewRepository.findById(dto.getReviewId()).orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (!review.getReviewer().getUserId().equals(reviewerId)) {
            throw new UnauthorizedException("Only the reviewer can edit this review");
        }

        if (dto.getRating() != null) review.setRating(dto.getRating());
        if (dto.getContent() != null) review.setContent(dto.getContent());
        review.setUpdatedAt(Instant.now());

        //  five image max
        if (newImages != null && newImages.length > 5) {
            throw new BadRequestException("Maximum 5 images allowed per review");
        }

        // handle images exactly like CommentService
        List<ReviewImage> currentImages = reviewImageRepository.findByReview(review);
        List<String> keep = dto.getExistingImageUrlsToKeep() != null ? dto.getExistingImageUrlsToKeep() : new ArrayList<>();
        List<ReviewImage> toDelete = currentImages.stream()
                .filter(img -> !keep.contains(img.getImageUrl()))
                .collect(Collectors.toList());
        reviewImageRepository.deleteAll(toDelete);

        if (newImages != null) {
            for (MultipartFile img : newImages) {
                if (!img.isEmpty()) {
                    String url = fileUploadService.uploadFile(img);
                    ReviewImage ri = new ReviewImage();
                    ri.setImageUrl(url);
                    ri.setReview(review);
                    reviewImageRepository.save(ri);
                }
            }
        }

        review = reviewRepository.save(review);
        return mapToReviewResponseDTO(review, reviewerId);
    }

    @Transactional
    public void deleteReview(Integer reviewId, Integer reviewerId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        if (!review.getReviewer().getUserId().equals(reviewerId)) {
            throw new UnauthorizedException("Only the reviewer can delete this review");
        }
        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByMechanic(Integer mechanicId, int page, int size, Integer currentUserId) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Review> allReviews;

        if (currentUserId != null) {
            allReviews = reviewRepository.findByMechanicWithUserPriority(mechanicId, currentUserId, pageable);
        } else {
            allReviews = reviewRepository.findByMechanic_UserId(
                    mechanicId,
                    PageRequest.of(page, size, Sort.by("createdAt").descending())
            );
        }

        return allReviews.map(review -> mapToReviewResponseDTO(review, currentUserId));
    }

    public Object[] getAverageRatingAndCount(Integer mechanicId) {
        return reviewRepository.getAverageRatingAndCount(mechanicId);
    }

    @Transactional
    public ReviewResponseItemDTO createReviewResponse(CreateReviewResponseRequestDTO dto, MultipartFile[] images, Integer userId) throws IOException {
        Review review = reviewRepository.findById(dto.getReviewId()).orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //  five image max
        if (images != null && images.length > 5) {
            throw new BadRequestException("Maximum 5 images allowed per response");
        }

        ReviewResponse response = new ReviewResponse();
        response.setContent(dto.getContent());
        response.setUser(user);
        response.setReview(review);
        response.setCreatedAt(Instant.now());

        response = reviewResponseRepository.save(response);

        if (images != null) {
            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    String url = fileUploadService.uploadFile(img);
                    ReviewResponseImage ri = new ReviewResponseImage();
                    ri.setImageUrl(url);
                    ri.setResponse(response);
                    reviewResponseImageRepository.save(ri);
                }
            }
        }
        return mapToReviewResponseItemDTO(response);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponseItemDTO> getReviewResponses(Integer reviewId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ReviewResponse> responses = reviewResponseRepository.findByReview_ReviewId(reviewId, pageable);
        return responses.map(this::mapToReviewResponseItemDTO);
    }

    private ReviewResponseDTO mapToReviewResponseDTO(Review review, Integer currentUserId) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setReviewId(review.getReviewId());
        dto.setReviewerId(review.getReviewer().getUserId());
        dto.setReviewerName(review.getReviewer().getName());
        dto.setReviewerProfilePicUrl(review.getReviewer().getProfilePic());
        dto.setMechanicId(review.getMechanic().getUserId());
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        List<String> urls = reviewImageRepository.findByReview(review)
                .stream().map(ReviewImage::getImageUrl).collect(Collectors.toList());
        dto.setImageUrls(urls);

        dto.setCurrentUsersReview(currentUserId != null && currentUserId.equals(review.getReviewer().getUserId()));
        return dto;
    }

    private ReviewResponseItemDTO mapToReviewResponseItemDTO(ReviewResponse response) {
        ReviewResponseItemDTO dto = new ReviewResponseItemDTO();
        dto.setResponseId(response.getResponseId());
        dto.setUserId(response.getUser().getUserId());
        dto.setUserName(response.getUser().getName());
        dto.setUserProfilePicUrl(response.getUser().getProfilePic());
        dto.setContent(response.getContent());
        dto.setCreatedAt(response.getCreatedAt());

        List<String> urls = reviewResponseImageRepository.findByResponse(response)
                .stream().map(ReviewResponseImage::getImageUrl).collect(Collectors.toList());
        dto.setImageUrls(urls);
        return dto;
    }
}
