package com.example.revly.service;

import com.example.revly.dto.request.CreateReportRequest;
import com.example.revly.dto.response.MyReportDto;
import com.example.revly.dto.response.ReportReasonDto;
import com.example.revly.dto.response.report.*;
import com.example.revly.exception.BadRequestException;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.*;
import com.example.revly.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportReasonRepository reportReasonRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MessageImageRepository messageImageRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ReviewResponseRepository reviewResponseRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Creates a new report.
     * Called only when the user does NOT already have a report for this entity.
     */
    @Transactional
    public MyReportDto createReport(CreateReportRequest request, Principal principal) {
        User reporter = getUserFromPrincipalOrThrow(principal);

        // Enforce one-report-per-user-per-entity
        if (reportRepository.findByReporterUserIdAndEntityTypeAndEntityId(
                reporter.getUserId(), request.getEntityType(), request.getEntityId()).isPresent()) {
            throw new BadRequestException("You have already reported this " + request.getEntityType().toLowerCase());
        }

        // Validate reasons
        Set<ReportReason> reasons = new HashSet<>();
        for (String code : request.getReasonCodes()) {
            ReportReason reason = reportReasonRepository.findByCode(code)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid reason code: " + code));
            reasons.add(reason);
        }

        if (reasons.isEmpty()) {
            throw new BadRequestException("At least one reason is required");
        }

        if (request.getExplanation() == null || request.getExplanation().trim().isEmpty()) {
            throw new BadRequestException("Explanation is required");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setEntityType(request.getEntityType());
        report.setEntityId(request.getEntityId());
        report.setExplanation(request.getExplanation().trim());
        report.setStatus("PENDING");
        report.setCreatedAt(Instant.now());
        report.setReasons(reasons);

        Report saved = reportRepository.save(report);
        return toMyReportDto(saved);
    }

    /**
     * Returns the user's report for a specific entity.
     * Used when user clicks "View / Modify Report" on an entity they already reported.
     */
    @Transactional(readOnly = true)
    public Optional<MyReportDto> getMyReportForEntity(String entityType, Integer entityId, Principal principal) {
        User user = getUserFromPrincipalOrThrow(principal);

        Optional<Report> reportOpt = reportRepository.findByReporterUserIdAndEntityTypeAndEntityId(
                user.getUserId(), entityType, entityId);

        return reportOpt.map(this::toMyReportDto);
    }

    /**
     * Updates the user's own report (explanation + reasons).
     * only allowed while status is still PENDING.
     * Once admin changes status (IN_REVIEW, RESOLVED, etc.), user can no longer modify it.
     */
    @Transactional
    public MyReportDto updateMyReport(Integer reportId, Set<String> newReasonCodes, String newExplanation, Principal principal) {
        User user = getUserFromPrincipalOrThrow(principal);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        // Security: only the owner can update
        if (!report.getReporter().getUserId().equals(user.getUserId())) {
            throw new UnauthorizedException("You can only modify your own reports");
        }

        // Cannot modify once admin has started reviewing
        if (!"PENDING".equals(report.getStatus())) {
            throw new BadRequestException("This report can no longer be modified because an admin has already reviewed it");
        }

        // Update reasons
        Set<ReportReason> newReasons = new HashSet<>();
        for (String code : newReasonCodes) {
            ReportReason reason = reportReasonRepository.findByCode(code)
                    .orElseThrow(() -> new ResourceNotFoundException("Invalid reason code: " + code));
            newReasons.add(reason);
        }

        if (newReasons.isEmpty()) {
            throw new BadRequestException("At least one reason is required");
        }

        if (newExplanation == null || newExplanation.trim().isEmpty()) {
            throw new BadRequestException("Explanation is required");
        }

        report.setExplanation(newExplanation.trim());
        report.setReasons(newReasons);
        // createdAt and status remain unchanged

        Report updated = reportRepository.save(report);
        return toMyReportDto(updated);
    }

    /**
     * ADMIN ONLY: Review / update status of any report.
     */
    @Transactional
    public MyReportDto reviewReport(Integer reportId, String newStatus, String adminExplanation, Principal principal) {
        User admin = getUserFromPrincipalOrThrow(principal);
        if (admin.getUserRoles() == null || !Boolean.TRUE.equals(admin.getUserRoles().getIsAdmin())) {
            throw new UnauthorizedException("Only admins can review reports");
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        if (!Set.of("PENDING", "IN_REVIEW", "RESOLVED", "CLOSED", "DISMISSED").contains(newStatus)) {
            throw new BadRequestException("Invalid status: " + newStatus);
        }

        report.setStatus(newStatus);
        report.setAdminExplanation(adminExplanation != null ? adminExplanation.trim() : null);
        report.setReviewedBy(admin);
        report.setReviewedAt(Instant.now());

        Report updated = reportRepository.save(report);
        return toMyReportDto(updated);
    }

    /**
     * Returns ALL predefined report reasons (used by the frontend modal).
     * No paging needed — there are only ~13 reasons.
     */
    @Transactional(readOnly = true)
    public List<ReportReasonDto> getAllReportReasons() {
        return reportReasonRepository.findAll().stream()
                .map(r -> {
                    ReportReasonDto dto = new ReportReasonDto();
                    dto.setCode(r.getCode());
                    dto.setDescription(r.getDescription());
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public ReportSummary getSingleReport(Integer reportId, Principal principal) {
        User admin = getUserFromPrincipalOrThrow(principal);
        if (admin.getUserRoles() == null || !Boolean.TRUE.equals(admin.getUserRoles().getIsAdmin())) {
            throw new UnauthorizedException("Only admins can view reports");
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        return mapReportToSpecificDto(report);
    }

    private ReportSummary mapReportToSpecificDto(Report report) {
        return switch (report.getEntityType()) {
            case "USER" -> mapToUserReportSummary(report);
            case "POST" -> mapToPostReportSummary(report);
            case "COMMENT" -> mapToCommentReportSummary(report);
            case "REVIEW" -> mapToReviewReportSummary(report);
            case "REVIEW_RESPONSE" -> mapToReviewResponseReportSummary(report);
            case "MESSAGE" -> mapToMessageReportSummary(report);
            case "MESSAGE_IMAGE" -> mapToMessageImageReportSummary(report);
            default -> throw new BadRequestException("Unknown entity type: " + report.getEntityType());
        };
    }

    private void copyCommonFields(Report report, ReportSummary dto) {
        dto.setReportId(report.getReportId());
        dto.setEntityType(report.getEntityType());
        dto.setEntityId(report.getEntityId());
        dto.setExplanation(report.getExplanation());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setReviewedBy(report.getReviewedBy() != null ? report.getReviewedBy().getUserId() : null);
        dto.setAdminExplanation(report.getAdminExplanation());
        dto.setReviewedAt(report.getReviewedAt());

        dto.setReasons(report.getReasons().stream()
                .map(r -> {
                    ReportReasonDto rd = new ReportReasonDto();
                    rd.setCode(r.getCode());
                    rd.setDescription(r.getDescription());
                    return rd;
                })
                .collect(Collectors.toList()));

        // Reporter info
        User reporter = report.getReporter();
        if (reporter != null) {
            dto.setReporterId(reporter.getUserId());
            dto.setReporterName(reporter.getName());
            dto.setReporterEmail(reporter.getEmail());
            dto.setReporterProfilePic(reporter.getProfilePic());
        }
    }

    private UserReportSummary mapToUserReportSummary(Report report) {
        UserReportSummary dto = new UserReportSummary();
        copyCommonFields(report, dto);

        User user = userRepository.findById(report.getEntityId()).orElse(null);
        if (user != null) {
            dto.setUserId(user.getUserId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setProfilePic(user.getProfilePic());
        }
        return dto;
    }

    private PostReportSummary mapToPostReportSummary(Report report) {
        PostReportSummary dto = new PostReportSummary();
        copyCommonFields(report, dto);

        Post post = postRepository.findById(report.getEntityId()).orElse(null);
        if (post != null) {
            dto.setPostId(post.getPostId());
            dto.setDescription(post.getDescription());
            dto.setImageUrls(post.getImages().stream().map(PostImage::getImageUrl).toList());

            User author = post.getUser();
            if (author != null) {
                dto.setAuthorId(author.getUserId());
                dto.setAuthorName(author.getName());
                dto.setAuthorEmail(author.getEmail());
                dto.setAuthorProfilePic(author.getProfilePic());
            }
        }
        return dto;
    }

    private CommentReportSummary mapToCommentReportSummary(Report report) {
        CommentReportSummary dto = new CommentReportSummary();
        copyCommonFields(report, dto);

        Comment comment = commentRepository.findById(report.getEntityId()).orElse(null);
        if (comment != null) {
            dto.setCommentId(comment.getCommentId());
            dto.setContent(comment.getContent());
            dto.setImageUrls(comment.getImages().stream().map(CommentImage::getImageUrl).toList());

            User author = comment.getUser();
            if (author != null) {
                dto.setAuthorId(author.getUserId());
                dto.setAuthorName(author.getName());
                dto.setAuthorEmail(author.getEmail());
                dto.setAuthorProfilePic(author.getProfilePic());
            }
        }
        return dto;
    }

    private ReviewReportSummary mapToReviewReportSummary(Report report) {
        ReviewReportSummary dto = new ReviewReportSummary();
        copyCommonFields(report, dto);

        Review review = reviewRepository.findById(report.getEntityId()).orElse(null);
        if (review != null) {
            dto.setReviewId(review.getReviewId());
            dto.setRating(review.getRating());
            dto.setContent(review.getContent());
            dto.setImageUrls(review.getImages().stream().map(ReviewImage::getImageUrl).toList());

            User reviewer = review.getReviewer();
            if (reviewer != null) {
                dto.setReviewerId(reviewer.getUserId());
                dto.setReviewerName(reviewer.getName());
                dto.setReviewerEmail(reviewer.getEmail());
                dto.setReviewerProfilePic(reviewer.getProfilePic());
            }
        }
        return dto;
    }

    private ReviewResponseReportSummary mapToReviewResponseReportSummary(Report report) {
        ReviewResponseReportSummary dto = new ReviewResponseReportSummary();
        copyCommonFields(report, dto);

        ReviewResponse response = reviewResponseRepository.findById(report.getEntityId()).orElse(null);
        if (response != null) {
            dto.setResponseId(response.getResponseId());
            dto.setContent(response.getContent());
            dto.setImageUrls(response.getImages().stream().map(ReviewResponseImage::getImageUrl).toList());

            User author = response.getUser();
            if (author != null) {
                dto.setAuthorId(author.getUserId());
                dto.setAuthorName(author.getName());
                dto.setAuthorEmail(author.getEmail());
                dto.setAuthorProfilePic(author.getProfilePic());
            }
        }
        return dto;
    }

    private MessageReportSummary mapToMessageReportSummary(Report report) {
        MessageReportSummary dto = new MessageReportSummary();
        copyCommonFields(report, dto);

        Message message = messageRepository.findById(report.getEntityId()).orElse(null);
        if (message != null) {
            dto.setMessageId(message.getMessageId());
            dto.setContent(message.getContent());
            dto.setImageUrls(message.getImages().stream().map(MessageImage::getImageUrl).toList());

            User author = message.getUser();
            if (author != null) {
                dto.setAuthorId(author.getUserId());
                dto.setAuthorName(author.getName());
                dto.setAuthorEmail(author.getEmail());
                dto.setAuthorProfilePic(author.getProfilePic());
            }
        }
        return dto;
    }

    private MessageImageReportSummary mapToMessageImageReportSummary(Report report) {
        MessageImageReportSummary dto = new MessageImageReportSummary();
        copyCommonFields(report, dto);

        MessageImage image = messageImageRepository.findById(report.getEntityId()).orElse(null);
        if (image != null) {
            dto.setMessageImageId(image.getId());
            dto.setImageUrls(List.of(image.getImageUrl()));

            Message message = image.getMessage();
            if (message != null) {
                User author = message.getUser();
                if (author != null) {
                    dto.setAuthorId(author.getUserId());
                    dto.setAuthorName(author.getName());
                    dto.setAuthorEmail(author.getEmail());
                    dto.setAuthorProfilePic(author.getProfilePic());
                }
            }
        }
        return dto;
    }

    private MyReportDto toMyReportDto(Report report) {
        MyReportDto dto = new MyReportDto();
        dto.setReportId(report.getReportId());
        dto.setEntityType(report.getEntityType());
        dto.setEntityId(report.getEntityId());
        dto.setExplanation(report.getExplanation());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());

        List<ReportReasonDto> reasonDtos = report.getReasons().stream()
                .map(r -> {
                    ReportReasonDto rd = new ReportReasonDto();
                    rd.setCode(r.getCode());
                    rd.setDescription(r.getDescription());
                    return rd;
                })
                .collect(Collectors.toList());

        dto.setReasons(reasonDtos);
        return dto;
    }

    private User getUserFromPrincipalOrThrow(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
