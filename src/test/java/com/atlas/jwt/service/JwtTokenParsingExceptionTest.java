package com.atlas.jwt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.config.jwt.JwtExceptions.JwtTokenParsingException;
import org.junit.jupiter.api.Test;

class JwtTokenParsingExceptionTest {

    @Test
    void constructor_shouldCreateExceptionWithMessage() {
        String message = "JWT token parsing failed";

        JwtTokenParsingException exception = new JwtTokenParsingException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_shouldCreateExceptionWithMessageAndCause() {
        String message = "JWT token parsing failed";
        RuntimeException cause = new RuntimeException("Underlying parsing error");

        JwtTokenParsingException exception = new JwtTokenParsingException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_shouldHandleNullMessage() {
        JwtTokenParsingException exception = new JwtTokenParsingException(null);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructor_shouldHandleNullCause() {
        String message = "JWT token parsing failed";

        JwtTokenParsingException exception = new JwtTokenParsingException(message, null);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void exception_shouldBeThrowableAndCatchable() {
        String message = "Test parsing exception";

        assertThatThrownBy(
                        () -> {
                            throw new JwtTokenParsingException(message);
                        })
                .isInstanceOf(JwtTokenParsingException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(message);
    }

    @Test
    void exception_shouldPreserveCauseStackTrace() {
        RuntimeException originalCause = new RuntimeException("Invalid JWT format");
        String message = "JWT parsing failed";

        JwtTokenParsingException exception = new JwtTokenParsingException(message, originalCause);

        assertThat(exception.getCause()).isEqualTo(originalCause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Invalid JWT format");
    }
}
