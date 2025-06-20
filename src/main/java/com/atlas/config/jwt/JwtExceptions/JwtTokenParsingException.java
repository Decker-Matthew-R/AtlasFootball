package com.atlas.config.jwt.JwtExceptions;

/** Exception thrown when JWT token parsing fails */
public class JwtTokenParsingException extends RuntimeException {

    public JwtTokenParsingException(String message) {
        super(message);
    }

    public JwtTokenParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
