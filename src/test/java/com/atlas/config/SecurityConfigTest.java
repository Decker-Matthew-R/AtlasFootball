package com.atlas.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.atlas.config.jwt.JwtTokenProvider;
import com.atlas.config.oauthHandlers.OAuth2AuthenticationFailureHandler;
import com.atlas.config.oauthHandlers.OAuth2AuthenticationSuccessHandler;
import com.atlas.user.repository.model.UserEntity;
import com.atlas.user.service.UserService;
import jakarta.servlet.http.Cookie;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SecurityConfigWorkingTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private OAuth2AuthenticationSuccessHandler successHandler;

    @MockitoBean private OAuth2AuthenticationFailureHandler failureHandler;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @MockitoBean private UserService userService;

    @MockitoBean private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Test
    void shouldAllowRootPath() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(
                        result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 404);
                        });
    }

    @Test
    void shouldAllowPublicApiEndpoints() throws Exception {
        mockMvc.perform(get("/api/public/test"))
                .andExpect(
                        result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 404, 405);
                        });
    }

    @Test
    void shouldAllowMetricsEndpointThroughSecurity() throws Exception {
        mockMvc.perform(post("/api/save-metric").contentType("application/json").content("{}"))
                .andExpect(
                        result -> {
                            int status = result.getResponse().getStatus();
                            String location = result.getResponse().getHeader("Location");

                            if (status == 302 && location != null) {
                                assertThat(location).doesNotContain("oauth2/authorization");
                            }
                            assertThat(status).isNotEqualTo(302);
                        });
    }

    @Test
    void shouldAllowOAuth2Endpoints() throws Exception {
        mockMvc.perform(get("/oauth2/anything"))
                .andExpect(
                        result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 404);
                        });
    }

    @Test
    void shouldRequireAuthenticationForUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldRequireAuthenticationForAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldRequireAuthenticationForRandomPaths() throws Exception {
        mockMvc.perform(get("/protected/resource"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("oauth2/authorization")));
    }

    @Test
    void shouldAllowAccessWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/user/profile").with(oauth2Login()))
                .andExpect(
                        result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 404);
                        });
    }

    @Test
    void shouldAllowCorsFromLocalhost3000() throws Exception {
        mockMvc.perform(
                        options("/api/test")
                                .header("Origin", "http://localhost:3000")
                                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void shouldRejectCorsFromOtherOrigins() throws Exception {
        mockMvc.perform(
                        options("/api/test")
                                .header("Origin", "http://evil-site.com")
                                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldIgnoreCSRFForOAuth2Paths() throws Exception {
        mockMvc.perform(post("/oauth2/test"))
                .andExpect(
                        result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 404);
                        });

        mockMvc.perform(post("/login/oauth2/code/google"))
                .andExpect(
                        result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 404);
                        });
    }

    @Test
    void shouldRequireCSRFForOtherProtectedEndpoints() throws Exception {
        mockMvc.perform(post("/api/user/update").with(oauth2Login()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/user/update").with(oauth2Login()).with(csrf()))
                .andExpect(
                        result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 404);
                        });
    }

    @Test
    void shouldCorrectlyEnforceSecurityRules() throws Exception {
        String[] publicPaths = {
            "/", "/error", "/oauth2/authorization/google", "/api/public/matches", "/api/save-metric"
        };

        for (String path : publicPaths) {
            mockMvc.perform(get(path))
                    .andExpect(
                            result -> {
                                int status = result.getResponse().getStatus();
                                String location = result.getResponse().getHeader("Location");

                                if (status == 302 && location != null) {
                                    assertThat(location)
                                            .as("Path %s should not redirect to OAuth2", path)
                                            .doesNotContain("oauth2/authorization");
                                }
                            });
        }

        String[] protectedApiPaths = {"/api/user/profile", "/api/admin/dashboard"};

        for (String path : protectedApiPaths) {
            mockMvc.perform(get(path))
                    .andExpect(status().isUnauthorized())
                    .andExpect(
                            result -> {
                                String location = result.getResponse().getHeader("Location");
                                assertThat(location).isNull();
                            });
        }

        String[] protectedNonApiPaths = {"/secure/data", "/private/info"};

        for (String path : protectedNonApiPaths) {
            mockMvc.perform(get(path))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(header().string("Location", containsString("oauth2/authorization")));
        }
    }

    @Test
    void shouldAllowCorsFromLocalhost8080() throws Exception {
        mockMvc.perform(
                        options("/api/test")
                                .header("Origin", "http://localhost:8080")
                                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8080"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void shouldAuthenticateUserWithValidJwtCookie() throws Exception {
        String validJwtToken = "valid.jwt.token";
        UserEntity testUser = createTestUser();

        when(jwtTokenProvider.validateToken(validJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validJwtToken)).thenReturn(123L);
        when(jwtTokenProvider.getEmailFromToken(validJwtToken)).thenReturn("test@example.com");
        when(userService.findById(123L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/user/profile").cookie(new Cookie("jwt", validJwtToken)))
                .andExpect(
                        result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 404);

                            String location = result.getResponse().getHeader("Location");
                            if (location != null) {
                                assertThat(location).doesNotContain("oauth2/authorization");
                            }
                        });
    }

    @Test
    void shouldRejectInvalidJwtCookie() throws Exception {
        String invalidJwtToken = "invalid.jwt.token";
        when(jwtTokenProvider.validateToken(invalidJwtToken)).thenReturn(false);

        mockMvc.perform(get("/api/user/profile").cookie(new Cookie("jwt", invalidJwtToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldRejectExpiredJwtCookie() throws Exception {
        String expiredJwtToken = "expired.jwt.token";
        when(jwtTokenProvider.validateToken(expiredJwtToken)).thenReturn(false);

        mockMvc.perform(get("/api/user/profile").cookie(new Cookie("jwt", expiredJwtToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldRejectValidJwtWhenUserNotFoundInDatabase() throws Exception {
        String validJwtToken = "valid.jwt.token";

        when(jwtTokenProvider.validateToken(validJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validJwtToken)).thenReturn(999L);
        when(jwtTokenProvider.getEmailFromToken(validJwtToken))
                .thenReturn("nonexistent@example.com");
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/profile").cookie(new Cookie("jwt", validJwtToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldSkipJwtValidationForPublicEndpoints() throws Exception {
        String[] publicPaths = {
            "/oauth2/authorization/google",
            "/login/oauth2/code/google",
            "/api/public/test",
            "/error"
        };

        for (String path : publicPaths) {
            mockMvc.perform(get(path).cookie(new Cookie("jwt", "any.jwt.token")))
                    .andExpect(
                            result -> {
                                int status = result.getResponse().getStatus();
                                String location = result.getResponse().getHeader("Location");

                                if (status == 302 && location != null) {
                                    assertThat(location)
                                            .as(
                                                    "Public path %s should not redirect to OAuth2",
                                                    path)
                                            .doesNotContain("oauth2/authorization");
                                }
                            });
        }
    }

    @Test
    void shouldHandleMultipleCookiesAndExtractJwt() throws Exception {
        String validJwtToken = "valid.jwt.token";
        UserEntity testUser = createTestUser();

        when(jwtTokenProvider.validateToken(validJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validJwtToken)).thenReturn(123L);
        when(jwtTokenProvider.getEmailFromToken(validJwtToken)).thenReturn("test@example.com");
        when(userService.findById(123L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(
                        get("/api/user/profile")
                                .cookie(new Cookie("session", "sessionvalue"))
                                .cookie(new Cookie("jwt", validJwtToken))
                                .cookie(new Cookie("csrf", "csrfvalue")))
                .andExpect(
                        result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 404);

                            String location = result.getResponse().getHeader("Location");
                            if (location != null) {
                                assertThat(location).doesNotContain("oauth2/authorization");
                            }
                        });
    }

    @Test
    void shouldFallbackToOAuth2WhenNoJwtCookie() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldHandleJwtParsingExceptionGracefully() throws Exception {
        String malformedJwtToken = "malformed.jwt.token";

        when(jwtTokenProvider.validateToken(malformedJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(malformedJwtToken))
                .thenThrow(new RuntimeException("JWT parsing failed"));

        mockMvc.perform(get("/api/user/profile").cookie(new Cookie("jwt", malformedJwtToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldBeStateless() throws Exception {
        String validJwtToken = "valid.jwt.token";
        UserEntity testUser = createTestUser();

        when(jwtTokenProvider.validateToken(validJwtToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(validJwtToken)).thenReturn(123L);
        when(jwtTokenProvider.getEmailFromToken(validJwtToken)).thenReturn("test@example.com");
        when(userService.findById(123L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/user/profile").cookie(new Cookie("jwt", validJwtToken)))
                .andExpect(
                        result -> {
                            assertThat(result.getRequest().getSession(false)).isNull();
                        });
    }

    @Test
    void shouldUseOAuth2FailureHandler() throws Exception {
        assertThat(oAuth2AuthenticationFailureHandler).isNotNull();
    }

    @Test
    void shouldIgnoreCSRFForAllConfiguredPaths() throws Exception {
        String[] csrfExcludedPaths = {
            "/oauth2/test", "/login/oauth2/code/test", "/api/save-metric"
        };

        for (String path : csrfExcludedPaths) {
            mockMvc.perform(post(path).contentType("application/json").content("{}"))
                    .andExpect(
                            result -> {
                                int status = result.getResponse().getStatus();
                                assertThat(status).isNotEqualTo(403);
                            });
        }
    }

    @Test
    void shouldReturn401ForUnauthenticatedApiRequests() throws Exception {
        String[] protectedApiPaths = {
            "/api/user/profile", "/api/admin/users", "/api/logout", "/api/protected/resource"
        };

        for (String path : protectedApiPaths) {
            mockMvc.perform(get(path))
                    .andExpect(status().isUnauthorized())
                    .andExpect(
                            result -> {
                                String location = result.getResponse().getHeader("Location");
                                assertThat(location)
                                        .as("API endpoint %s should not redirect to OAuth2", path)
                                        .isNull();
                            });
        }
    }

    @Test
    void shouldReturn401ForUnauthenticatedApiPostRequests() throws Exception {
        String[] protectedApiPaths = {"/api/user/update", "/api/admin/create", "/api/data/save"};

        for (String path : protectedApiPaths) {
            mockMvc.perform(post(path).contentType("application/json").content("{}").with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(
                            result -> {
                                String location = result.getResponse().getHeader("Location");
                                assertThat(location)
                                        .as("API endpoint %s should not redirect to OAuth2", path)
                                        .isNull();
                            });
        }
    }

    @Test
    void shouldReturn401ForUnauthenticatedApiPutRequests() throws Exception {
        mockMvc.perform(
                        put("/api/user/123")
                                .contentType("application/json")
                                .content("{}")
                                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldReturn401ForUnauthenticatedApiDeleteRequests() throws Exception {
        mockMvc.perform(delete("/api/user/123").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldRedirectToOAuth2ForUnauthenticatedNonApiRequests() throws Exception {
        String[] nonApiProtectedPaths = {
            "/dashboard", "/profile", "/settings", "/protected/resource", "/admin/panel"
        };

        for (String path : nonApiProtectedPaths) {
            mockMvc.perform(get(path))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(header().string("Location", containsString("oauth2/authorization")));
        }
    }

    @Test
    void shouldReturn401ForApiLogoutWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/logout").with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldReturn401ForApiCsrfEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/csrf"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldStillAllowPublicApiEndpointsWithoutAuthentication() throws Exception {
        String[] publicApiPaths = {"/api/public/test", "/api/save-metric"};

        for (String path : publicApiPaths) {
            mockMvc.perform(get(path))
                    .andExpect(
                            result -> {
                                int status = result.getResponse().getStatus();
                                String location = result.getResponse().getHeader("Location");

                                assertThat(status).isNotEqualTo(401);
                                if (location != null) {
                                    assertThat(location).doesNotContain("oauth2/authorization");
                                }
                            });
        }
    }

    @Test
    void shouldReturn401ForDeepApiPaths() throws Exception {
        String[] deepApiPaths = {
            "/api/v1/users/123/profile",
            "/api/v2/admin/dashboard/stats",
            "/api/internal/metrics/performance"
        };

        for (String path : deepApiPaths) {
            mockMvc.perform(get(path))
                    .andExpect(status().isUnauthorized())
                    .andExpect(
                            result -> {
                                String location = result.getResponse().getHeader("Location");
                                assertThat(location).isNull();
                            });
        }
    }

    @Test
    void shouldReturn401ForApiPathsWithQueryParameters() throws Exception {
        mockMvc.perform(get("/api/user/search").param("query", "test").param("limit", "10"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        result -> {
                            String location = result.getResponse().getHeader("Location");
                            assertThat(location).isNull();
                        });
    }

    @Test
    void shouldHandleApiPathsWithSpecialCharactersGracefully() throws Exception {
        String[] specialApiPaths = {
            "/api/user/test@example.com",
            "/api/file/document%20name.pdf",
            "/api/search/term+with+spaces"
        };

        for (String path : specialApiPaths) {
            mockMvc.perform(get(path))
                    .andExpect(
                            result -> {
                                int status = result.getResponse().getStatus();
                                assertThat(status).isIn(400, 401);

                                String location = result.getResponse().getHeader("Location");
                                if (location != null) {
                                    assertThat(location).doesNotContain("oauth2/authorization");
                                }
                            });
        }
    }

    @Test
    void shouldRespectExceptionHandlingForDifferentHttpMethods() throws Exception {
        String apiPath = "/api/testshould401/endpoint";

        mockMvc.perform(get(apiPath)).andExpect(status().isUnauthorized());

        mockMvc.perform(post(apiPath).contentType("application/json").content("{}").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put(apiPath).contentType("application/json").content("{}").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete(apiPath).with(csrf())).andExpect(status().isUnauthorized());

        mockMvc.perform(patch(apiPath).contentType("application/json").content("{}").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleCaseInsensitiveApiPaths() throws Exception {
        mockMvc.perform(get("/API/user/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("oauth2/authorization")));

        mockMvc.perform(get("/Api/user/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("oauth2/authorization")));
    }

    @Test
    void shouldReturn401OnlyForExactApiPathPrefix() throws Exception {
        mockMvc.perform(get("/api/should401")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/apix/test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("oauth2/authorization")));

        mockMvc.perform(get("/xapi/test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("oauth2/authorization")));
    }

    @Test
    void shouldReturn401ForMinimalApiPath() throws Exception {
        mockMvc.perform(get("/api/")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api")).andExpect(status().isUnauthorized());
    }

    private UserEntity createTestUser() {
        UserEntity user = new UserEntity();
        user.setId(123L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setProfilePictureUrl("https://example.com/profile.jpg");
        return user;
    }
}
