package com.example.revly.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TextEmbeddingService {

    public List<Float> embed(String text) {
        // TODO: Call your actual embedding provider here
        // Must return exactly 1536 floats (matching vector(1536))
        // Example: return openAiClient.createEmbedding(text);
        throw new UnsupportedOperationException("Implement real embedding generation");
    }
}