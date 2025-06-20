package com.atlas.config.oauthHandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import com.atlas.config.jwt.JwtExceptions.JwtTokenGenerationException;
import com.atlas.config.jwt.JwtTokenProvider;
import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
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

    private OAuth2AuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setId(123L);
        mockUser.setEmail("john.doe@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");

        oauthAttributes = new HashMap<>();
        oauthAttributes.put("email", "john.doe@example.com");
        oauthAttributes.put("name", "John Doe");
        oauthAttributes.put("sub", "google123");
        oauthAttributes.put("picture", "https://example.com/profile.jpg");

        mockJwtToken =
                "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOjEyMywiZW1haWwiOiJqb2huLmRvZUBleGFtcGxlLmNvbSJ9.signature";
    }

    @Test
    void onAuthenticationSuccess_shouldProcessUserAndSetCookiesWithConfiguredUrl()
            throws Exception {
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(mockUser);

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

        Cookie userCookie = cookieCaptor.getAllValues().get(1);
        assertThat(userCookie.getName()).isEqualTo("user_info");
        assertThat(userCookie.isHttpOnly()).isFalse();

        String decodedJson = URLDecoder.decode(userCookie.getValue(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        Map userData = objectMapper.readValue(decodedJson, Map.class);

        assertThat(userData.get("id")).isEqualTo(123);
        assertThat(userData.get("email")).isEqualTo("john.doe@example.com");
        assertThat(userData.get("name")).isEqualTo("John Doe");
        assertThat(userData.get("firstName")).isEqualTo("John");
        assertThat(userData.get("lastName")).isEqualTo("Doe");
        assertThat(userData.get("profilePicture")).isEqualTo("");

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void onAuthenticationSuccess_shouldUseReferrerForRedirectWhenNoConfiguredUrl()
            throws Exception {
        handler = new OAuth2AuthenticationSuccessHandler(userService, jwtTokenProvider, "");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(request.getHeader("Referer")).thenReturn("http://localhost:8080/some-page");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:8080/");
    }

    @Test
    void onAuthenticationSuccess_shouldUsePort3000FromReferrer() throws Exception {
        handler = new OAuth2AuthenticationSuccessHandler(userService, jwtTokenProvider, null);

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(request.getHeader("Referer")).thenReturn("http://localhost:3000/oauth/login");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void onAuthenticationSuccess_shouldUseDefaultWhenNoReferrer() throws Exception {
        handler = new OAuth2AuthenticationSuccessHandler(userService, jwtTokenProvider, "   ");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(request.getHeader("Referer")).thenReturn(null);

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void onAuthenticationSuccess_shouldAddSlashToConfiguredUrlWithoutSlash() throws Exception {
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void onAuthenticationSuccess_shouldNotAddSlashToConfiguredUrlWithSlash() throws Exception {
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000/");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void onAuthenticationSuccess_shouldHandleSpecialCharactersInUserData() throws Exception {
        UserEntity specialCharUser = new UserEntity();
        specialCharUser.setId(456L);
        specialCharUser.setEmail("user+test@example.com");
        specialCharUser.setFirstName("José");
        specialCharUser.setLastName("María & Co");
        specialCharUser.setProfilePictureUrl("https://example.com/profile.jpg");

        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google"))
                .thenReturn(specialCharUser);
        when(userService.getFullName(specialCharUser)).thenReturn("José María & Co");
        when(jwtTokenProvider.generateToken(specialCharUser)).thenReturn(mockJwtToken);

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        Cookie userCookie = cookieCaptor.getAllValues().get(1);
        assertThat(userCookie.getName()).isEqualTo("user_info");

        String decodedJson = URLDecoder.decode(userCookie.getValue(), StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        Map userData = objectMapper.readValue(decodedJson, Map.class);

        assertThat(userData.get("id")).isEqualTo(456);
        assertThat(userData.get("email")).isEqualTo("user+test@example.com");
        assertThat(userData.get("name")).isEqualTo("José María & Co");
        assertThat(userData.get("firstName")).isEqualTo("José");
        assertThat(userData.get("lastName")).isEqualTo("María & Co");
        assertThat(userData.get("profilePicture")).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    void onAuthenticationSuccess_shouldPropagateJwtGenerationException() throws Exception {
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(jwtTokenProvider.generateToken(mockUser))
                .thenThrow(new JwtTokenGenerationException("JWT generation failed"));

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(JwtTokenGenerationException.class)
                .hasMessage("JWT generation failed");

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(mockUser);
        verify(response, never()).addCookie(any());
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void onAuthenticationSuccess_shouldPropagateUserServiceException() throws Exception {
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google"))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider, never()).generateToken(any());
        verify(response, never()).addCookie(any());
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void onAuthenticationSuccess_shouldHandleIOExceptionDuringRedirect() throws Exception {
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        doThrow(new IOException("Network error")).when(response).sendRedirect(any());

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(IOException.class)
                .hasMessage("Network error");

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(mockUser);
        verify(response, times(2)).addCookie(any());
    }

    @Test
    void onAuthenticationSuccess_shouldUseCorrectProviderName() throws Exception {
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(userService).createOrUpdateUserFromOAuth(eq(oAuth2User), eq("google"));
    }

    @Test
    void onAuthenticationSuccess_shouldLogUserAttributesAndProcessing() throws Exception {
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(oAuth2User, atLeast(1)).getAttributes();
        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(mockUser);
    }

    @Test
    void onAuthenticationSuccess_shouldHandleReferrerWithoutPortInfo() throws Exception {
        handler = new OAuth2AuthenticationSuccessHandler(userService, jwtTokenProvider, "");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(request.getHeader("Referer")).thenReturn("http://localhost/some-page");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void setUserInfoCookie_shouldHandleEncodingExceptionGracefully() throws Exception {
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

        doNothing().when(response).addCookie(argThat(cookie -> "jwt".equals(cookie.getName())));
        doThrow(new RuntimeException("Cookie error"))
                .when(response)
                .addCookie(argThat(cookie -> "user_info".equals(cookie.getName())));

        assertDoesNotThrow(
                () -> handler.onAuthenticationSuccess(request, response, authentication));

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(userWithProblematicData);
        verify(response, times(2)).addCookie(any());
        verify(response).sendRedirect(any());
    }

    @Test
    void setUserInfoCookie_shouldHandleJsonSerializationException() throws Exception {
        UserEntity problematicUser = new UserEntity();
        problematicUser.setId(999L);
        problematicUser.setEmail("test@example.com");
        problematicUser.setFirstName("Test");
        problematicUser.setLastName("User");
        problematicUser.setProfilePictureUrl(null);

        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google"))
                .thenReturn(problematicUser);
        when(userService.getFullName(problematicUser)).thenReturn("Test User");
        when(jwtTokenProvider.generateToken(problematicUser)).thenReturn(mockJwtToken);

        assertDoesNotThrow(
                () -> handler.onAuthenticationSuccess(request, response, authentication));

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(jwtTokenProvider).generateToken(problematicUser);
    }

    @Test
    void onAuthenticationSuccess_shouldHandleNullProfilePicture() throws Exception {
        UserEntity userWithNullProfilePic = new UserEntity();
        userWithNullProfilePic.setId(456L);
        userWithNullProfilePic.setEmail("user@example.com");
        userWithNullProfilePic.setFirstName("John");
        userWithNullProfilePic.setLastName("Doe");
        userWithNullProfilePic.setProfilePictureUrl(null);

        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google"))
                .thenReturn(userWithNullProfilePic);
        when(userService.getFullName(userWithNullProfilePic)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(userWithNullProfilePic)).thenReturn(mockJwtToken);

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response, times(2)).addCookie(cookieCaptor.capture());

        Cookie userCookie = cookieCaptor.getAllValues().get(1);
        String decodedJson = URLDecoder.decode(userCookie.getValue(), StandardCharsets.UTF_8);
        ObjectMapper objectMapper = new ObjectMapper();
        Map userData = objectMapper.readValue(decodedJson, Map.class);

        assertThat(userData.get("profilePicture")).isEqualTo("");
    }

    @Test
    void determineRedirectUrl_shouldHandleFrontendUrlWithoutSlash() throws Exception {
        // Test the specific branch where frontendUrl doesn't end with "/"
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void determineRedirectUrl_shouldHandleFrontendUrlWithSlash() throws Exception {
        // Test the specific branch where frontendUrl already ends with "/"
        handler =
                new OAuth2AuthenticationSuccessHandler(
                        userService, jwtTokenProvider, "http://localhost:3000/");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void determineRedirectUrl_shouldHandleEmptyFrontendUrl() throws Exception {
        handler = new OAuth2AuthenticationSuccessHandler(userService, jwtTokenProvider, "");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(request.getHeader("Referer")).thenReturn(null); // No referrer

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }

    @Test
    void determineRedirectUrl_shouldHandleWhitespaceOnlyFrontendUrl() throws Exception {
        handler = new OAuth2AuthenticationSuccessHandler(userService, jwtTokenProvider, "   ");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(oauthAttributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");
        when(jwtTokenProvider.generateToken(mockUser)).thenReturn(mockJwtToken);
        when(request.getHeader("Referer")).thenReturn(null); // No referrer

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());
        assertThat(redirectCaptor.getValue()).isEqualTo("http://localhost:3000/");
    }
}
