package com.atlas.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.atlas.config.jwt.JwtTokenProvider;
import com.atlas.config.oauthHandlers.OAuth2AuthenticationSuccessHandler;
import com.atlas.metrics.repository.MetricsRepository;
import com.atlas.metrics.repository.model.MetricEventEntity;
import com.atlas.metrics.service.MetricsService;
import com.atlas.user.repository.UserRepository;
import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.repository.model.UserOAuthProvider;
import com.atlas.user.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class OAuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private UserRepository userRepository;
    @Autowired private MetricsRepository metricsRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserService userService;
    @Autowired private MetricsService metricsService;
    @Autowired private OAuth2AuthenticationSuccessHandler successHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    @Transactional
    void resetDatabase() {

        entityManager
                .createNativeQuery("TRUNCATE TABLE metrics RESTART IDENTITY CASCADE;")
                .executeUpdate();
        entityManager
                .createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE;")
                .executeUpdate();
        entityManager
                .createNativeQuery("TRUNCATE TABLE user_oauth_providers RESTART IDENTITY CASCADE;")
                .executeUpdate();
    }

    @Test
    @DisplayName(
            "INT - OAuth success handler creates user, generates JWT, sets cookies, and records metrics")
    void oauthSuccessHandler_CreatesUserAndRecordsMetrics() throws Exception {
        Map<String, Object> googleUserAttributes =
                Map.of(
                        "sub", "google-user-id-123",
                        "email", "integration-test@gmail.com",
                        "name", "Integration Test",
                        "picture", "https://example.com/profile.jpg",
                        "email_verified", true);

        OAuth2User mockOAuth2User = new DefaultOAuth2User(List.of(), googleUserAttributes, "sub");

        Authentication mockAuthentication = org.mockito.Mockito.mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockOAuth2User);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Referer", "http://localhost:8080");
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, mockAuthentication);

        Optional<UserEntity> savedUser = userRepository.findByEmail("integration-test@gmail.com");
        assertThat(savedUser).isPresent();

        UserEntity user = savedUser.get();
        assertThat(user.getEmail()).isEqualTo("integration-test@gmail.com");
        assertThat(user.getFirstName()).isEqualTo("Integration");
        assertThat(user.getLastName()).isEqualTo("Test");
        assertThat(user.getProfilePictureUrl()).isEqualTo("https://example.com/profile.jpg");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getLastLogin()).isNotNull();

        assertThat(user.getOauthProviders()).hasSize(1);
        UserOAuthProvider oauthProvider = user.getOauthProviders().get(0);
        assertThat(oauthProvider.getProviderName()).isEqualTo("google");
        assertThat(oauthProvider.getProviderUserId()).isEqualTo("google-user-id-123");
        assertThat(oauthProvider.getProviderEmail()).isEqualTo("integration-test@gmail.com");
        assertThat(oauthProvider.getIsPrimary()).isTrue();

        Cookie[] cookies = response.getCookies();
        Cookie jwtCookie = null;
        Cookie userInfoCookie = null;

        for (Cookie cookie : cookies) {
            if ("jwt".equals(cookie.getName())) {
                jwtCookie = cookie;
            } else if ("user_info".equals(cookie.getName())) {
                userInfoCookie = cookie;
            }
        }

        assertThat(jwtCookie).isNotNull();
        assertThat(jwtCookie.getValue()).isNotEmpty();
        assertThat(jwtCookie.isHttpOnly()).isTrue();
        assertThat(jwtCookie.getSecure()).isFalse();
        assertThat(jwtCookie.getPath()).isEqualTo("/");
        assertThat(jwtCookie.getDomain()).isEqualTo("localhost");
        assertThat(jwtCookie.getMaxAge()).isEqualTo(24 * 60 * 60);

        String jwtToken = jwtCookie.getValue();
        assertThat(jwtTokenProvider.validateToken(jwtToken)).isTrue();

        assertThat(userInfoCookie).isNotNull();
        assertThat(userInfoCookie.getValue()).isNotEmpty();
        assertThat(userInfoCookie.isHttpOnly()).isFalse();
        assertThat(userInfoCookie.getPath()).isEqualTo("/");
        assertThat(userInfoCookie.getDomain()).isEqualTo("localhost");

        String decodedUserInfo =
                URLDecoder.decode(userInfoCookie.getValue(), StandardCharsets.UTF_8);
        Map<String, Object> userInfoData =
                objectMapper.readValue(decodedUserInfo, new TypeReference<>() {});

        assertThat(userInfoData.get("email")).isEqualTo("integration-test@gmail.com");
        assertThat(userInfoData.get("name")).isEqualTo("Integration Test");
        assertThat(userInfoData.get("firstName")).isEqualTo("Integration");
        assertThat(userInfoData.get("lastName")).isEqualTo("Test");
        assertThat(userInfoData.get("profilePicture")).isEqualTo("https://example.com/profile.jpg");
        assertThat(userInfoData.get("id")).isEqualTo(user.getId().intValue());

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:8080/");

        List<MetricEventEntity> metrics = metricsRepository.findAll();
        assertThat(metrics).hasSize(1);

        MetricEventEntity loginMetric = metrics.get(0);
        assertThat(loginMetric.getEvent()).isEqualTo("LOGIN");
        assertThat(loginMetric.getUserId()).isEqualTo(user.getId());
        assertThat(loginMetric.getEventTime()).isNotNull();

        String metadataJson = loginMetric.getMetadata();
        Map<String, Object> metadata =
                objectMapper.readValue(metadataJson, new TypeReference<>() {});
        assertThat(metadata.get("triggerId")).isEqualTo("OAuth Success");
        assertThat(metadata.get("screen")).isEqualTo("N/A");
    }

    @Test
    @DisplayName("INT - Existing user OAuth login links provider and doesn't create duplicate")
    void existingUserOAuthLogin_LinksProviderOnly() throws Exception {
        UserEntity existingUser = new UserEntity();
        existingUser.setEmail("existing-user@gmail.com");
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setProfilePictureUrl("https://example.com/old-profile.jpg");
        existingUser = userRepository.save(existingUser);

        Long existingUserId = existingUser.getId();

        Map<String, Object> googleUserAttributes =
                Map.of(
                        "sub", "google-user-id-456",
                        "email", "existing-user@gmail.com",
                        "name", "Updated Name",
                        "picture", "https://example.com/new-profile.jpg",
                        "email_verified", true);

        OAuth2User mockOAuth2User = new DefaultOAuth2User(List.of(), googleUserAttributes, "sub");

        Authentication mockAuthentication = org.mockito.Mockito.mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockOAuth2User);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, mockAuthentication);

        List<UserEntity> allUsers = userRepository.findAll();
        assertThat(allUsers).hasSize(1);

        UserEntity updatedUser = userRepository.findById(existingUserId).orElseThrow();
        assertThat(updatedUser.getId()).isEqualTo(existingUserId);
        assertThat(updatedUser.getEmail()).isEqualTo("existing-user@gmail.com");

        assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
        assertThat(updatedUser.getLastName()).isEqualTo("Name");
        assertThat(updatedUser.getProfilePictureUrl())
                .isEqualTo("https://example.com/new-profile.jpg");
        assertThat(updatedUser.getLastLogin()).isNotNull();

        assertThat(updatedUser.getOauthProviders()).hasSize(1);
        UserOAuthProvider oauthProvider = updatedUser.getOauthProviders().get(0);
        assertThat(oauthProvider.getProviderName()).isEqualTo("google");
        assertThat(oauthProvider.getProviderUserId()).isEqualTo("google-user-id-456");
        assertThat(oauthProvider.getProviderEmail()).isEqualTo("existing-user@gmail.com");
        assertThat(oauthProvider.getIsPrimary()).isTrue();

        List<MetricEventEntity> metrics = metricsRepository.findAll();
        assertThat(metrics).hasSize(1);
        assertThat(metrics.get(0).getUserId()).isEqualTo(existingUserId);
    }

    @Test
    @DisplayName("INT - OAuth login with missing optional fields handles gracefully")
    void oauthLoginWithMissingFields_HandlesGracefully() throws Exception {
        Map<String, Object> googleUserAttributes =
                Map.of(
                        "sub", "google-user-id-789",
                        "email", "minimal-user@gmail.com",
                        "email_verified", true);

        OAuth2User mockOAuth2User = new DefaultOAuth2User(List.of(), googleUserAttributes, "sub");

        Authentication mockAuthentication = org.mockito.Mockito.mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockOAuth2User);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, mockAuthentication);

        Optional<UserEntity> savedUser = userRepository.findByEmail("minimal-user@gmail.com");
        assertThat(savedUser).isPresent();

        UserEntity user = savedUser.get();
        assertThat(user.getEmail()).isEqualTo("minimal-user@gmail.com");
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getLastLogin()).isNotNull();
    }

    @Test
    @DisplayName("INT - Multiple OAuth logins for same user create multiple login metrics")
    void multipleOAuthLogins_CreateMultipleMetrics() throws Exception {
        Map<String, Object> googleUserAttributes =
                Map.of(
                        "sub", "google-user-id-multiple",
                        "email", "multiple-login@gmail.com",
                        "name", "Multiple Login",
                        "picture", "https://example.com/profile.jpg",
                        "email_verified", true);

        OAuth2User mockOAuth2User = new DefaultOAuth2User(List.of(), googleUserAttributes, "sub");

        Authentication mockAuthentication = org.mockito.Mockito.mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockOAuth2User);

        MockHttpServletRequest request1 = new MockHttpServletRequest();
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(request1, response1, mockAuthentication);

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        successHandler.onAuthenticationSuccess(request2, response2, mockAuthentication);

        List<UserEntity> users = userRepository.findAll();
        assertThat(users).hasSize(1);

        List<MetricEventEntity> metrics = metricsRepository.findAll();
        assertThat(metrics).hasSize(2);

        assertThat(metrics.get(0).getUserId()).isEqualTo(users.get(0).getId());
        assertThat(metrics.get(1).getUserId()).isEqualTo(users.get(0).getId());
        assertThat(metrics.get(0).getEvent()).isEqualTo("LOGIN");
        assertThat(metrics.get(1).getEvent()).isEqualTo("LOGIN");
    }
}
