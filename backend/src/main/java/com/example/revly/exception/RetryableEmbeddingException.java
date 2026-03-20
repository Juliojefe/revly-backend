package com.example.revly.exception;

public class RetryableEmbeddingException extends RuntimeException {
    public RetryableEmbeddingException(String message) { super(message); }
    public RetryableEmbeddingException(String message, Throwable cause) { super(message, cause); }
}
