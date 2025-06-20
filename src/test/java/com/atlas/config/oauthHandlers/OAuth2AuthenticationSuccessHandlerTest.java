package com.atlas.config.oauthHandlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock private UserService userService;

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @Mock private Authentication authentication;

    @Mock private OAuth2User oAuth2User;

    @InjectMocks private OAuth2AuthenticationSuccessHandler handler;

    private UserEntity mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserEntity();
        mockUser.setId(123L);
        mockUser.setEmail("john.doe@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
    }

    @Test
    void onAuthenticationSuccess_shouldProcessUserAndRedirect() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "john.doe@example.com");
        attributes.put("name", "John Doe");
        attributes.put("sub", "google123");
        attributes.put("picture", "https://example.com/picture.jpg");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        String expectedUrl =
                "http://localhost:3000/oauth-success?userId=123&email=john.doe@example.com&name=John Doe";
        assertEquals(expectedUrl, redirectUrl);
    }

    @Test
    void onAuthenticationSuccess_shouldHandleSpecialCharactersInName() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "user@example.com");
        attributes.put("name", "José María");
        attributes.put("sub", "google456");

        UserEntity userWithSpecialChars = new UserEntity();
        userWithSpecialChars.setId(456L);
        userWithSpecialChars.setEmail("user@example.com");
        userWithSpecialChars.setFirstName("José");
        userWithSpecialChars.setLastName("María");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google"))
                .thenReturn(userWithSpecialChars);
        when(userService.getFullName(userWithSpecialChars)).thenReturn("José María");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertTrue(redirectUrl.contains("userId=456"));
        assertTrue(redirectUrl.contains("email=user@example.com"));
        assertTrue(redirectUrl.contains("name=José María"));
    }

    @Test
    void onAuthenticationSuccess_shouldHandleEmptyName() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "noname@example.com");
        attributes.put("sub", "google789");

        UserEntity userWithoutName = new UserEntity();
        userWithoutName.setId(789L);
        userWithoutName.setEmail("noname@example.com");
        userWithoutName.setFirstName("Unknown");
        userWithoutName.setLastName("");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google"))
                .thenReturn(userWithoutName);
        when(userService.getFullName(userWithoutName)).thenReturn("Unknown ");

        handler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertTrue(redirectUrl.contains("userId=789"));
        assertTrue(redirectUrl.contains("email=noname@example.com"));
        assertTrue(redirectUrl.contains("name=Unknown "));
    }

    @Test
    void onAuthenticationSuccess_shouldHandleUserServiceException() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "error@example.com");
        attributes.put("sub", "google999");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google"))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(
                RuntimeException.class,
                () -> {
                    handler.onAuthenticationSuccess(request, response, authentication);
                });

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
        verify(response, never()).sendRedirect(any());
    }

    @Test
    void onAuthenticationSuccess_shouldHandleIOException() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "io@example.com");
        attributes.put("sub", "google111");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");

        doThrow(new IOException("Network error")).when(response).sendRedirect(any());

        assertThrows(
                IOException.class,
                () -> {
                    handler.onAuthenticationSuccess(request, response, authentication);
                });

        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
    }

    @Test
    void onAuthenticationSuccess_shouldUseCorrectProvider() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "provider@example.com");
        attributes.put("sub", "google222");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(userService).createOrUpdateUserFromOAuth(eq(oAuth2User), eq("google"));
    }

    @Test
    void onAuthenticationSuccess_shouldLogUserAttributes() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "log@example.com");
        attributes.put("name", "Log User");
        attributes.put("sub", "google333");

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userService.createOrUpdateUserFromOAuth(oAuth2User, "google")).thenReturn(mockUser);
        when(userService.getFullName(mockUser)).thenReturn("John Doe");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(oAuth2User, atLeast(1)).getAttributes();
        verify(userService).createOrUpdateUserFromOAuth(oAuth2User, "google");
    }
}
