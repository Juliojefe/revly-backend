package com.example.revly.service;

import com.example.revly.dto.response.SearchResponse;
import com.example.revly.dto.response.UserSearchResult;
import com.example.revly.model.User;
import com.example.revly.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final UserRepository userRepository;

    public SearchService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public SearchResponse<UserSearchResult> searchUsers(String query, boolean mechanicOnly, int offset, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return new SearchResponse<>(List.of(), false);
        }

        String q = query.trim();
        List<User> users = userRepository.searchUsers(q, mechanicOnly);

        // Simple offset pagination
        int total = users.size();
        int end = Math.min(offset + limit, total);
        List<User> page = (offset < total) ? users.subList(offset, end) : List.of();

        List<UserSearchResult> results = page.stream()
                .map(u -> {
                    boolean isMech = u.getUserRoles() != null && Boolean.TRUE.equals(u.getUserRoles().getIsMechanic());
                    return new UserSearchResult(
                            u.getUserId(),
                            u.getName(),
                            u.getProfilePic(),
                            isMech
                    );
                })
                .collect(Collectors.toList());

        boolean hasMore = end < total;

        return new SearchResponse<>(results, hasMore);
    }
}