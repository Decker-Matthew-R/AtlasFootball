package com.atlas.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.atlas.config.oauthHandlers.OAuth2AuthenticationFailureHandler;
import com.atlas.config.oauthHandlers.OAuth2AuthenticationSuccessHandler;
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
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("oauth2/authorization")));
    }

    @Test
    void shouldRequireAuthenticationForAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("oauth2/authorization")));
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

        String[] protectedPaths = {
            "/api/user/profile", "/api/admin/dashboard", "/secure/data", "/private/info"
        };

        for (String path : protectedPaths) {
            mockMvc.perform(get(path))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(header().string("Location", containsString("oauth2/authorization")));
        }
    }
}
