package com.example.revly.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Service
public class GeocodeService {
    private static final Logger log = LoggerFactory.getLogger(GeocodeService.class);
    private static final String LOCATIONIQ_BASE = "https://api.locationiq.com/v1";

    @Value("${locationiq.key:}")
    private String locationIqKey;

    private final RestTemplate rest = new RestTemplate();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> autocomplete(String query, int limit) {
        requireKey();

        String q = (query == null) ? "" : query.trim();
        if (q.isEmpty()) return List.of();

        int safeLimit = clamp(limit, 1, 20);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(LOCATIONIQ_BASE + "/autocomplete")
                .queryParam("key", locationIqKey)
                .queryParam("q", q)
                .queryParam("limit", safeLimit)
                .queryParam("format", "json")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        try {
            ResponseEntity<List> resp = rest.getForEntity(uri, List.class);
            List<Map<String, Object>> body = (List<Map<String, Object>>) resp.getBody();
            return (body == null) ? List.of() : body;
        } catch (RestClientResponseException e) {
            log.error("LocationIQ autocomplete failed status={} body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "LocationIQ autocomplete failed: " + e.getRawStatusCode() + " " + safeBody(e.getResponseBodyAsString())
            );
        } catch (Exception e) {
            log.error("Unexpected error calling LocationIQ autocomplete", e);
            throw new ResponseStatusException(BAD_GATEWAY, "LocationIQ autocomplete failed unexpectedly");
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> search(String query, int limit) {
        requireKey();

        String q = (query == null) ? "" : query.trim();
        if (q.isEmpty()) return List.of();

        int safeLimit = clamp(limit, 1, 20);

        URI uri = UriComponentsBuilder
                .fromHttpUrl(LOCATIONIQ_BASE + "/search")
                .queryParam("key", locationIqKey)
                .queryParam("q", q)
                .queryParam("limit", safeLimit)
                .queryParam("format", "json")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        try {
            ResponseEntity<List> resp = rest.getForEntity(uri, List.class);
            List<Map<String, Object>> body = (List<Map<String, Object>>) resp.getBody();
            return (body == null) ? List.of() : body;
        } catch (RestClientResponseException e) {
            log.error("LocationIQ search failed status={} body={}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "LocationIQ search failed: " + e.getRawStatusCode() + " " + safeBody(e.getResponseBodyAsString())
            );
        } catch (Exception e) {
            log.error("Unexpected error calling LocationIQ search", e);
            throw new ResponseStatusException(BAD_GATEWAY, "LocationIQ search failed unexpectedly");
        }
    }

    private void requireKey() {
        if (locationIqKey == null || locationIqKey.isBlank()) {
            throw new ResponseStatusException(BAD_GATEWAY, "Missing LocationIQ API key (locationiq.key)");
        }
    }

    private static int clamp(int val, int min, int max) {
        if (val < min) return min;
        if (val > max) return max;
        return val;
    }

    private static String safeBody(String body) {
        if (body == null) return "";
        return body.length() > 500 ? body.substring(0, 500) + "…" : body;
    }
}