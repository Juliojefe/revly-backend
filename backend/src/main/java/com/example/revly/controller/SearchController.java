package com.example.revly.controller;

import com.example.revly.dto.response.PostSummary;
import com.example.revly.dto.response.UserSearchResult;
import com.example.revly.model.User;
import com.example.revly.repository.UserRepository;
import com.example.revly.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired private SearchService searchService;
    @Autowired private UserRepository userRepository;

    // ===================== POST SEARCHES (works for guests + logged-in users) =====================

    @GetMapping("/posts/text")
    public ResponseEntity<Page<PostSummary>> searchByText(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        User user = getUserOrNull(principal);
        Pageable pageable = PageRequest.of(page, size); // similarity order is handled by DB
        return ResponseEntity.ok(searchService.searchPostsByText(query, pageable, user));
    }

    @GetMapping("/posts/tag")
    public ResponseEntity<Page<PostSummary>> searchByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        User user = getUserOrNull(principal);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("postId")));
        return ResponseEntity.ok(searchService.searchPostsByTag(tag, pageable, user));
    }

    @GetMapping("/posts/hybrid")
    public ResponseEntity<Page<PostSummary>> searchHybrid(
            @RequestParam String query,
            @RequestParam String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        User user = getUserOrNull(principal);
        Pageable pageable = PageRequest.of(page, size); // similarity order
        return ResponseEntity.ok(searchService.searchPostsHybrid(query, tag, pageable, user));
    }

    // ===================== USER SEARCH =====================

    @GetMapping("/users")
    public ResponseEntity<Page<UserSearchResult>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "false") boolean mechanicOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(searchService.searchUsers(query, mechanicOnly, pageable));
    }

    private User getUserOrNull(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }
}