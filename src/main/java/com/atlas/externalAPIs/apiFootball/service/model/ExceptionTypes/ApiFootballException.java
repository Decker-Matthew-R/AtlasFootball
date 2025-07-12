package com.atlas.externalAPIs.apiFootball.service.model.ExceptionTypes;

public class ApiFootballException extends RuntimeException {
    public ApiFootballException(String message, Throwable cause) {
        super(message, cause);
    }
}
