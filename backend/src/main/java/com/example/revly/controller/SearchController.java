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

    @GetMapping("/posts/text")
    public ResponseEntity<Page<PostSummary>> searchByText(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        User user = getUserOrNull(principal);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(searchService.searchPostsByText(query, pageable, user));
    }

    // accepts "mechanic" or "#mechanic" – returns matching tags like ["mechanic", "mechanics", "auto-mechanic"]
    @GetMapping("/tags")
    public ResponseEntity<Page<String>> searchSimilarTags(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("tagName").ascending());
        return ResponseEntity.ok(searchService.searchSimilarTags(query, pageable));
    }

    // this is the endpoint the frontend calls after the user picks a tag from /tags
    @GetMapping("/posts/tag")
    public ResponseEntity<Page<PostSummary>> searchPostsByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        User user = getUserOrNull(principal);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("postId")));
        return ResponseEntity.ok(searchService.searchPostsByTag(tag, pageable, user));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserSearchResult>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "false") boolean mechanicOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {
        User currentUser = getUserOrNull(principal);
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return ResponseEntity.ok(searchService.searchUsers(query, mechanicOnly, pageable, currentUser));
    }

    private User getUserOrNull(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByEmail(principal.getName()).orElse(null);
    }
}