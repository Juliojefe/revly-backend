package com.example.revly.exception;

public class NonRetryableEmbeddingException extends RuntimeException {
    public NonRetryableEmbeddingException(String message) { super(message); }
    public NonRetryableEmbeddingException(String message, Throwable cause) { super(message, cause); }
}