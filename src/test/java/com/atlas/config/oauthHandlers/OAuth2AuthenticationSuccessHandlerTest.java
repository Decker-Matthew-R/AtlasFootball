package com.atlas.config.oauthHandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.atlas.jwt.service.JwtTokenGenerationException;
import com.atlas.jwt.service.JwtTokenProvider;
import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock private UserService userService;

    @Mock private JwtTokenProvider jwtTokenProvider;

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @Mock private Authentication authentication;

    @Mock private OAuth2User oAuth2User;

    private UserEntity mockUser;
    private Map<String, Object> oauthAttributes;
    private String mockJwtToken;

    // We'll create the handler manually to test different frontend URL scenarios
    private OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        // Set up mock user
        mockUser = new UserEntity();
        mockUser.setId(123L);
        mockUser.setEmail("john.doe@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        // Set up OAuth attributes
        oauthAttributes = new HashMap<>();
        oauthAttributes.put("email", "john.doe@example.com");
        oauthAttributes.put("name", "John Doe");
        oauthAttributes.put("sub", "google123");
        oauthAttributes.put("picture", "https://example.com/profile.jpg");

        // Mock JWT token
        mockJwtToken =
                "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEyMywiZW1haWwiOiJqb2huLmRvZUBleGFtcGxlLmNvbSJ9.signature";
    }

    @Test
    void onAuthenticationSuccess_shouldProcessUserAndSetCookiesWithConfiguredUrl()
            throws Exception {
        // Given - Handler with configured frontend URL
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(mockUser);

        // Verify JWT cookie
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        Cookie jwtCookie = cookieCaptor.getAllValues().get(0);
        assertThat(jwtCookie.getName()).isEqualTo("jwt");
        assertThat(jwtCookie.getValue()).isEqualTo(mockJwtToken);
        assertThat(jwtCookie.isHttpOnly()).isTrue();
        assertThat(jwtCookie.getSecure()).isFalse();
        assertThat(jwtCookie.getPath()).isEqualTo("/");
        assertThat(jwtCookie.getDomain()).isEqualTo("localhost");
        assertThat(jwtCookie.getMaxAge()).isEqualTo(24 * 60 * 60);

        // Verify user info cookie
        Cookie userCookie = cookieCaptor.getAllValues().get(1);
        assertThat(userCookie.getName()).isEqualTo("user");
        assertThat(userCookie.getValue())
                .isEqualTo("id=123&email=john.doe%40example.com&name=John+Doe");
        assertThat(userCookie.isHttpOnly()).isFalse();

        // Verify redirect
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void onAuthenticationSuccess_shouldUseReferrerForRedirectWhenNoConfiguredUrl()
            throws Exception {
        // Given - Handler with no configured frontend URL
        handler = new OAuth2AuthenticationSuccessHandler(userService, jwtTokenProvider, "");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(request.getHeader("Referer")).thenReturn("http://localhost:8080/some-page");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:8080/");
    }

    @Test
    void onAuthenticationSuccess_shouldUsePort3000FromReferrer() throws Exception {
        // Given - Handler with no configured URL, referrer has port 3000
        handler = new OAuth2AuthenticationSuccessHandler(userService, jwtTokenProvider, null);

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(request.getHeader("Referer")).thenReturn("http://localhost:3000/oauth/login");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void onAuthenticationSuccess_shouldUseDefaultWhenNoReferrer() throws Exception {
        // Given - No configured URL and no referrer
        handler = new OAuth2AuthenticationSuccessHandler(userService, jwtTokenProvider, "   ");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(request.getHeader("Referer")).thenReturn(null);

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void onAuthenticationSuccess_shouldAddSlashToConfiguredUrlWithoutSlash() throws Exception {
        // Given - Configured URL without trailing slash
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void onAuthenticationSuccess_shouldNotAddSlashToConfiguredUrlWithSlash() throws Exception {
        // Given - Configured URL with trailing slash
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000/");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void onAuthenticationSuccess_shouldHandleSpecialCharactersInUserData() throws Exception {
        // Given - User with special characters
        UserEntity specialCharUser = new UserEntity();
        specialCharUser.setId(456L);
        specialCharUser.setEmail("user+test@example.com");
        specialCharUser.setFirstName("José");
        specialCharUser.setLastName("María & Co");

        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google"))
                .thenReturn(specialCharUser);
        when(userService.getFullName(specialCharUser)).thenReturn("José María & Co");
        when(jwtTokenProvider.generateToken(specialCharUser)).thenReturn(mockJwtToken);

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        Cookie userCookie = cookieCaptor.getAllValues().get(1);
        String expectedUserInfo =
                String.format(
                        "id=456&email=%s&name=%s",
                        URLEncoder.encode("user+test@example.com", StandardCharsets.UTF_8),
                        URLEncoder.encode("José María & Co", StandardCharsets.UTF_8));
        assertThat(userCookie.getValue()).isEqualTo(expectedUserInfo);
    }

    @Test
    void onAuthenticationSuccess_shouldPropagateJwtGenerationException() throws Exception {
        // Given
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(jwtTokenProvider.generateToken(mockUser))
                .thenThrow(new JwtTokenGenerationException("JWT generation failed"));

        // When & Then
        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(JwtTokenGenerationException.class)
                .hasMessage("JWT generation failed");

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(mockUser);
        verify(response, never()).addCookie(any()); // Should not set cookies on JWT failure
        verify(response, never()).sendRedirect(any()); // Should not redirect on JWT failure
    }

    @Test
    void onAuthenticationSuccess_shouldPropagateUserServiceException() throws Exception {
        // Given
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google"))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider, never()).generateToken(any()); // Should not call JWT generation
        verify(response, never()).addCookie(any()); // Should not set cookies
        verify(response, never()).sendRedirect(any()); // Should not redirect
    }

    @Test
    void onAuthenticationSuccess_shouldHandleIOExceptionDuringRedirect() throws Exception {
        // Given
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        doThrow(new IOException("Network error")).when(response).sendRedirect(any());

        // When & Then
        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(IOException.class)
                .hasMessage("Network error");

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(mockUser);
        verify(response, times(2)).addCookie(any()); // Cookies should still be set
    }

    @Test
    void onAuthenticationSuccess_shouldUseCorrectProviderName() throws Exception {
        // Given
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userService).createOrUpdateUserFromOAuth(eq(oAuth2User), eq("google"));
    }

    @Test
    void onAuthenticationSuccess_shouldLogUserAttributesAndProcessing() throws Exception {
        // Given
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(oAuth2User, atLeast(1)).getAttributes(); // Should log user attributes
        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(mockUser);
    }

    @Test
    void onAuthenticationSuccess_shouldHandleReferrerWithoutPortInfo() throws Exception {
        // Given - Referrer doesn't contain port info
        handler = new OAuth2AuthenticationSuccessHandler(userService, jwtTokenProvider, "");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(request.getHeader("Referer")).thenReturn("http://localhost/some-page");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue())
                .isEqualTo("http://localhost:3000/"); // Should use default
    }

    @Test
    void setUserInfoCookie_shouldHandleEncodingExceptionGracefully() throws Exception {
        // Given - This test verifies the catch block in setUserInfoCookie
        UserEntity userWithProblematicData = new UserEntity();
        userWithProblematicData.setId(999L);
        userWithProblematicData.setEmail("test@example.com");
        userWithProblematicData.setFirstName("Test");
        userWithProblematicData.setLastName("User");

        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google"))
                .thenReturn(userWithProblematicData);
        when(userService.getFullName(userWithProblematicData)).thenReturn("Test User");
        when(jwtTokenProvider.generateToken(userWithProblematicData)).thenReturn(mockJwtToken);

        // Mock addCookie to throw exception for user cookie (second call)
        doNothing().when(response).addCookie(any()); // First call (JWT cookie) succeeds
        doThrow(new RuntimeException("Cookie error"))
                .when(response)
                .addCookie(argThat(cookie -> "user".equals(cookie.getName()))); // Second call (user
        // cookie) fails

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(userWithProblematicData);
        verify(response, times(2)).addCookie(any()); // Both cookies attempted
        verify(response).sendRedirect(any()); // Should still redirect despite user cookie failure
    }
}
