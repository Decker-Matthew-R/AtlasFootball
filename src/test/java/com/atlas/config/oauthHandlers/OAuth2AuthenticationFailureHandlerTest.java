package com.atlas.config.oauthHandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationFailureHandlerTest {

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    private OAuth2AuthenticationFailureHandler failureHandler;
    private ListAppender<ILoggingEvent> logAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        failureHandler = new OAuth2AuthenticationFailureHandler();

        logger = (Logger) LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);
    }

    @Test
    void shouldRedirectToErrorPageWithExceptionMessage() throws IOException, ServletException {
        AuthenticationException exception =
                new BadCredentialsException("Invalid credentials provided");

        failureHandler.onAuthenticationFailure(request, response, exception);

        String expectedRedirectUrl =
                "http://localhost:3000/oauth-error?error=Invalid credentials provided";
        verify(response).sendRedirect(expectedRedirectUrl);
    }

    @Test
    void shouldHandleOAuth2SpecificExceptions() throws IOException, ServletException {
        OAuth2Error oauth2Error =
                new OAuth2Error(
                        "invalid_request", "The request is missing a required parameter", null);
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(oauth2Error);

        failureHandler.onAuthenticationFailure(request, response, exception);

        String expectedRedirectUrl =
                "http://localhost:3000/oauth-error?error=The request is missing a required parameter";
        verify(response).sendRedirect(expectedRedirectUrl);
    }

    @Test
    void shouldHandleExceptionMessagesWithSpecialCharacters() throws IOException, ServletException {
        AuthenticationException exception =
                new BadCredentialsException("Error with spaces & special chars!");

        failureHandler.onAuthenticationFailure(request, response, exception);

        String expectedRedirectUrl =
                "http://localhost:3000/oauth-error?error=Error with spaces & special chars!";
        verify(response).sendRedirect(expectedRedirectUrl);
    }

    @Test
    void shouldHandleNullExceptionMessage() throws IOException, ServletException {
        AuthenticationException exception = new BadCredentialsException(null);

        failureHandler.onAuthenticationFailure(request, response, exception);

        String expectedRedirectUrl = "http://localhost:3000/oauth-error?error=null";
        verify(response).sendRedirect(expectedRedirectUrl);
    }

    @Test
    void shouldHandleEmptyExceptionMessage() throws IOException, ServletException {
        AuthenticationException exception = new BadCredentialsException("");

        failureHandler.onAuthenticationFailure(request, response, exception);

        String expectedRedirectUrl = "http://localhost:3000/oauth-error?error=";
        verify(response).sendRedirect(expectedRedirectUrl);
    }

    @Test
    void shouldLogAuthenticationFailureDetails() throws IOException, ServletException {
        AuthenticationException exception = new BadCredentialsException("Test error message");

        failureHandler.onAuthenticationFailure(request, response, exception);

        assertThat(logAppender.list).hasSize(3);

        ILoggingEvent headerLog = logAppender.list.get(0);
        assertThat(headerLog.getLevel()).isEqualTo(Level.ERROR);
        assertThat(headerLog.getMessage()).isEqualTo("=== OAuth2 Authentication Failed ===");

        ILoggingEvent messageLog = logAppender.list.get(1);
        assertThat(messageLog.getLevel()).isEqualTo(Level.ERROR);
        assertThat(messageLog.getMessage()).isEqualTo("Error: {}");
        assertThat(messageLog.getArgumentArray()).containsExactly("Test error message");

        ILoggingEvent typeLog = logAppender.list.get(2);
        assertThat(typeLog.getLevel()).isEqualTo(Level.ERROR);
        assertThat(typeLog.getMessage()).isEqualTo("Exception type: {}");
        assertThat(typeLog.getArgumentArray()).containsExactly("BadCredentialsException");
    }

    @Test
    void shouldLogDifferentExceptionTypes() throws IOException, ServletException {
        OAuth2Error oauth2Error =
                new OAuth2Error("access_denied", "User denied authorization", null);
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(oauth2Error);

        failureHandler.onAuthenticationFailure(request, response, exception);

        ILoggingEvent typeLog = logAppender.list.get(2);
        assertThat(typeLog.getArgumentArray()).containsExactly("OAuth2AuthenticationException");
    }

    @Test
    void shouldHandleIOExceptionFromResponse() throws IOException, ServletException {
        AuthenticationException exception = new BadCredentialsException("Test error");
        doThrow(new IOException("Network error")).when(response).sendRedirect(anyString());

        org.junit.jupiter.api.Assertions.assertThrows(
                IOException.class,
                () -> {
                    failureHandler.onAuthenticationFailure(request, response, exception);
                });

        assertThat(logAppender.list).hasSize(3);
    }

    @Test
    void shouldHandleVariousAuthenticationExceptionTypes() throws IOException, ServletException {
        AuthenticationException[] exceptions = {
            new BadCredentialsException("Bad credentials"),
            new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_grant", "Invalid authorization code", null)),
        };

        for (AuthenticationException exception : exceptions) {
            logAppender.list.clear();
            reset(response);

            failureHandler.onAuthenticationFailure(request, response, exception);

            verify(response).sendRedirect(contains("http://localhost:3000/oauth-error?error="));
            assertThat(logAppender.list).hasSize(3);
        }
    }

    @Test
    void shouldConstructCorrectRedirectUrl() throws IOException, ServletException {

        AuthenticationException exception =
                new BadCredentialsException("OAuth2 provider unavailable");

        failureHandler.onAuthenticationFailure(request, response, exception);

        verify(response)
                .sendRedirect(
                        "http://localhost:3000/oauth-error?error=OAuth2 provider unavailable");
    }

    @Test
    void shouldUseConsistentBaseUrl() throws IOException, ServletException {
        AuthenticationException exception = new BadCredentialsException("Any error");

        failureHandler.onAuthenticationFailure(request, response, exception);

        verify(response)
                .sendRedirect(
                        argThat(url -> url.startsWith("http://localhost:3000/oauth-error?error=")));
    }

    @Test
    void shouldCompleteEntireFailureHandlingFlow() throws IOException, ServletException {
        OAuth2Error oauth2Error =
                new OAuth2Error(
                        "invalid_client",
                        "Client authentication failed",
                        "https://tools.ietf.org/html/rfc6749#section-5.2");
        OAuth2AuthenticationException exception = new OAuth2AuthenticationException(oauth2Error);

        failureHandler.onAuthenticationFailure(request, response, exception);

        verify(response)
                .sendRedirect(
                        "http://localhost:3000/oauth-error?error=Client authentication failed");

        assertThat(logAppender.list).hasSize(3);
        assertThat(logAppender.list.get(1).getArgumentArray())
                .containsExactly("Client authentication failed");
        assertThat(logAppender.list.get(2).getArgumentArray())
                .containsExactly("OAuth2AuthenticationException");

        verifyNoMoreInteractions(response);
    }

    @Test
    void shouldHandleVeryLongErrorMessages() throws IOException, ServletException {
        String longMessage =
                "This is a very long error message that might be returned by an OAuth2 provider when something goes wrong with the authentication process and they want to provide detailed information about what happened"
                        .repeat(3);
        AuthenticationException exception = new BadCredentialsException(longMessage);

        failureHandler.onAuthenticationFailure(request, response, exception);

        verify(response).sendRedirect("http://localhost:3000/oauth-error?error=" + longMessage);
        assertThat(logAppender.list.get(1).getArgumentArray()).containsExactly(longMessage);
    }
}
