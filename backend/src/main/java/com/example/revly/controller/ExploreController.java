package com.example.revly.controller;

import com.example.revly.dto.response.PostSummary;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.User;
import com.example.revly.repository.UserRepository;
import com.example.revly.service.ExploreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/explore")
public class ExploreController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExploreService exploreService;

    @GetMapping
    public ResponseEntity<Page<PostSummary>> getExplorePosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Principal principal
    ) {
        User user = getUserFromPrincipalOrThrow(principal);
        //  sort by created_at break ties by postId
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("postId"));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                exploreService.getExplorePosts(pageable, user)
        );
    }

    @GetMapping("/guest")
    public ResponseEntity<Page<PostSummary>> getExplorePostsGuest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        //  sort by created_at break ties by postId
        Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("postId"));
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                exploreService.getExplorePostsGuest(pageable)
        );
    }

    private User getUserFromPrincipalOrThrow(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}