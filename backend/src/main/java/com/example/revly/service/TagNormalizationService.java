package com.example.revly.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagNormalizationService {

    /**
     * Main public API used by PostService, SearchService, etc.
     * - Strips leading #
     * - Trims whitespace
     * - Converts to lowercase
     * - Validates: 1-64 chars, only a-z0-9_
     * - Rejects empty/invalid tags (filters them out)
     * - Deduplicates automatically (returns Set)
     */
    public Set<String> normalizeTags(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return Collections.emptySet();
        }

        return rawTags.stream()
                .map(this::normalizeSingleTag)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());   // Set = automatic deduplication
    }

    // Returns null for any invalid tag so it gets filtered out.
    private String normalizeSingleTag(String input) {
        if (input == null) return null;

        String cleaned = input.trim()
                .replaceFirst("^#", "")           // strip leading #
                .toLowerCase(Locale.ROOT);

        // Reject empty, >64 chars, or invalid characters
        if (cleaned.isEmpty() || !cleaned.matches("^[a-z0-9_]{1,64}$")) {
            return null;
        }
        return cleaned;
    }
}