package com.example.revly.service;

import com.example.revly.config.OpenAiProperties;
import com.example.revly.exception.NonRetryableEmbeddingException;
import com.example.revly.exception.RetryableEmbeddingException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TextEmbeddingService {

    private final WebClient openAiWebClient;
    private final OpenAiProperties props;

    public TextEmbeddingService(WebClient openAiWebClient, OpenAiProperties props) {
        this.openAiWebClient = openAiWebClient;
        this.props = props;
    }

    public List<Float> embed(String text) {
        String normalized = normalize(text);

        EmbeddingsRequest req = new EmbeddingsRequest(
                props.getEmbeddings().getModel(),
                normalized,
                props.getEmbeddings().getDimensions(),
                "float"
        );

        try {
            EmbeddingsResponse resp = openAiWebClient
                    .post()
                    .uri("/embeddings")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(EmbeddingsResponse.class)
                    .onErrorMap(this::mapToEmbeddingException)
                    .block(props.getHttp().timeout());

            if (resp == null || resp.data == null || resp.data.isEmpty() || resp.data.get(0).embedding == null) {
                throw new RetryableEmbeddingException("Embedding provider returned an empty response payload.");
            }

            List<Float> vector = resp.data.get(0).embedding;
            int expected = props.getEmbeddings().getDimensions();

            if (vector.size() != expected) {
                throw new NonRetryableEmbeddingException(
                        "Embedding dimension mismatch. Expected " + expected + " but got " + vector.size()
                );
            }

            return vector;

        } catch (RetryableEmbeddingException | NonRetryableEmbeddingException e) {
            throw e;
        } catch (Exception e) {
            throw new RetryableEmbeddingException("Unexpected embedding failure: " + e.getMessage(), e);
        }
    }

    private String normalize(String text) {
        if (text == null) {
            throw new NonRetryableEmbeddingException("Text is null.");
        }
        String normalized = text.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            throw new NonRetryableEmbeddingException("Text is blank after trimming.");
        }
        return normalized;
    }

    private Throwable mapToEmbeddingException(Throwable t) {
        if (!(t instanceof WebClientResponseException)) {
            return new RetryableEmbeddingException("Embedding request failed (network/timeout): " + t.getMessage(), t);
        }
        WebClientResponseException e = (WebClientResponseException) t;

        int statusCode = e.getStatusCode().value();

        HttpStatus status = HttpStatus.valueOf(statusCode);
        if (status == HttpStatus.TOO_MANY_REQUESTS || status.is5xxServerError()) {
            return new RetryableEmbeddingException(
                    "Embedding provider temporary error (" + statusCode + "): " + safeBody(e), e);
        }
        return new NonRetryableEmbeddingException(
                "Embedding provider non-retryable error (" + statusCode + "): " + safeBody(e), e);
    }

    private String safeBody(WebClientResponseException e) {
        try {
            String body = e.getResponseBodyAsString();
            return body.length() > 500 ? body.substring(0, 500) + "…" : body;
        } catch (Exception ex) {
            return "";
        }
    }

    // ... (EmbeddingsRequest and EmbeddingsResponse inner classes stay exactly the same)
    static final class EmbeddingsRequest {
        public final String model;
        public final Object input;
        public final Integer dimensions;
        public final String encoding_format;

        EmbeddingsRequest(String model, String input, Integer dimensions, String encoding_format) {
            this.model = model;
            this.input = input;
            this.dimensions = dimensions;
            this.encoding_format = encoding_format;
        }
    }

    static final class EmbeddingsResponse {
        public List<EmbeddingData> data;

        static final class EmbeddingData {
            public List<Float> embedding;
            public Integer index;
        }
    }
}