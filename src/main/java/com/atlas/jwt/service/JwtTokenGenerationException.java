package com.atlas.jwt.service;

public class JwtTokenGenerationException extends RuntimeException {

    public JwtTokenGenerationException(String message) {
        super(message);
    }

    public JwtTokenGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
