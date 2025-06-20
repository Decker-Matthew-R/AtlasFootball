package com.atlas.jwt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.config.jwt.JwtExceptions.JwtTokenGenerationException;
import org.junit.jupiter.api.Test;

class JwtTokenGenerationExceptionTest {

    @Test
    void constructor_shouldCreateExceptionWithMessage() {
        String message = "JWT token generation failed";

        JwtTokenGenerationException exception = new JwtTokenGenerationException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_shouldCreateExceptionWithMessageAndCause() {
        String message = "JWT token generation failed";
        RuntimeException cause = new RuntimeException("Underlying cause");

        JwtTokenGenerationException exception = new JwtTokenGenerationException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_shouldHandleNullMessage() {
        JwtTokenGenerationException exception = new JwtTokenGenerationException(null);

        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructor_shouldHandleNullCause() {
        String message = "JWT token generation failed";

        JwtTokenGenerationException exception = new JwtTokenGenerationException(message, null);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void exception_shouldBeThrowableAndCatchable() {
        String message = "Test exception";

        assertThatThrownBy(
                        () -> {
                            throw new JwtTokenGenerationException(message);
                        })
                .isInstanceOf(JwtTokenGenerationException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(message);
    }

    @Test
    void exception_shouldPreserveCauseStackTrace() {
        RuntimeException originalCause = new RuntimeException("Original error");
        String message = "JWT generation failed";

        JwtTokenGenerationException exception =
                new JwtTokenGenerationException(message, originalCause);

        assertThat(exception.getCause()).isEqualTo(originalCause);
        assertThat(exception.getCause().getMessage()).isEqualTo("Original error");
    }
}
