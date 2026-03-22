package com.example.revly.dto.response;

import java.util.List;

public class SearchResponse<T> {

    private List<T> results;
    private boolean hasMore;

    public SearchResponse(List<T> results, boolean hasMore) {
        this.results = results;
        this.hasMore = hasMore;
    }

    public List<T> getResults() { return results; }
    public boolean isHasMore() { return hasMore; }
}