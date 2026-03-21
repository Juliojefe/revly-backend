package com.example.revly.controller;

import com.example.revly.dto.response.SearchResponse;
import com.example.revly.dto.response.UserSearchResult;
import com.example.revly.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*") // public endpoint
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/users")
    public ResponseEntity<SearchResponse<UserSearchResult>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "false") boolean mechanicOnly,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {

        SearchResponse<UserSearchResult> response = searchService.searchUsers(q, mechanicOnly, offset, limit);
        return ResponseEntity.ok(response);
    }
}