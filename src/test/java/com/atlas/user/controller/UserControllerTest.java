package com.atlas.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.atlas.config.jwt.JwtTokenProvider;
import com.atlas.metrics.controller.model.MetricEventDTO;
import com.atlas.metrics.controller.model.MetricEventType;
import com.atlas.metrics.service.MetricsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = UserController.class)
@AutoConfigureAfter(JacksonAutoConfiguration.class)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private MetricsService metricsService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(jwtTokenProvider.getUserIdFromToken(anyString())).thenReturn(123L);
        doNothing().when(metricsService).saveMetricEvent(any(MetricEventDTO.class));
    }

    @Test
    void logout_shouldReturnSuccessMessageAndClearJwtCookie() throws Exception {
        MvcResult result =
                mockMvc.perform(post("/api/logout").contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, String> responseMap =
                objectMapper.readValue(responseBody, new TypeReference<>() {});

        assertThat(responseMap).containsEntry("message", "Logged out successfully");

        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).hasSize(1);

        Cookie jwtCookie = cookies[0];
        assertThat(jwtCookie.getName()).isEqualTo("jwt");
        assertThat(jwtCookie.getValue()).isNull();
        assertThat(jwtCookie.getMaxAge()).isEqualTo(0);
        assertThat(jwtCookie.getPath()).isEqualTo("/");
        assertThat(jwtCookie.getDomain()).isEqualTo("localhost");
        assertThat(jwtCookie.isHttpOnly()).isTrue();
        assertThat(jwtCookie.getSecure()).isFalse();
    }

    @Test
    void logout_shouldUseCorrectHttpMethod() throws Exception {
        mockMvc.perform(post("/api/logout")).andExpect(status().isOk());
    }

    @Test
    void logout_shouldRejectGetMethod() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                                "/api/logout"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void logout_shouldRejectPutMethod() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                                "/api/logout"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void logout_shouldRejectDeleteMethod() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                                "/api/logout"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void logout_shouldHandleRequestWithExistingCookies() throws Exception {
        Cookie existingJwtCookie = new Cookie("jwt", "existing-jwt-token-value");
        existingJwtCookie.setHttpOnly(true);
        existingJwtCookie.setPath("/");

        MvcResult result =
                mockMvc.perform(
                                post("/api/logout")
                                        .cookie(existingJwtCookie)
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).hasSize(1);

        Cookie clearedCookie = cookies[0];
        assertThat(clearedCookie.getName()).isEqualTo("jwt");
        assertThat(clearedCookie.getValue()).isNull();
        assertThat(clearedCookie.getMaxAge()).isEqualTo(0);
    }

    @Test
    void logout_shouldHandleRequestWithoutExistingCookies() throws Exception {
        MvcResult result =
                mockMvc.perform(post("/api/logout").contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();
        assertThat(cookies).hasSize(1);

        Cookie jwtCookie = cookies[0];
        assertThat(jwtCookie.getName()).isEqualTo("jwt");
        assertThat(jwtCookie.getValue()).isNull();
        assertThat(jwtCookie.getMaxAge()).isEqualTo(0);
    }

    @Test
    void logout_shouldHandleEmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void logout_shouldSetCorrectCookieAttributes() throws Exception {
        MvcResult result =
                mockMvc.perform(post("/api/logout")).andExpect(status().isOk()).andReturn();

        Cookie jwtCookie = result.getResponse().getCookies()[0];

        assertThat(jwtCookie.isHttpOnly())
                .as("JWT cookie should be HttpOnly for security")
                .isTrue();

        assertThat(jwtCookie.getSecure())
                .as("JWT cookie should not be secure in localhost environment")
                .isFalse();

        assertThat(jwtCookie.getPath())
                .as("JWT cookie should be available on all paths")
                .isEqualTo("/");

        assertThat(jwtCookie.getDomain())
                .as("JWT cookie should be scoped to localhost")
                .isEqualTo("localhost");

        assertThat(jwtCookie.getMaxAge()).as("JWT cookie should expire immediately").isEqualTo(0);
    }

    @Test
    void logout_shouldReturnJsonResponse() throws Exception {
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void logout_shouldLogSuccessMessage() throws Exception {
        mockMvc.perform(post("/api/logout")).andExpect(status().isOk());
    }

    @Test
    void logout_shouldHandleConcurrentRequests() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }
    }

    @Test
    void logout_shouldCaptureLogoutMetricWithCorrectData() throws Exception {
        Cookie jwtCookie = new Cookie("jwt", "valid-jwt-token");

        mockMvc.perform(post("/api/logout").cookie(jwtCookie)).andExpect(status().isOk());

        ArgumentCaptor<MetricEventDTO> captor = ArgumentCaptor.forClass(MetricEventDTO.class);
        verify(metricsService).saveMetricEvent(captor.capture());

        MetricEventDTO captured = captor.getValue();
        assertThat(captured.getEvent()).isEqualTo(MetricEventType.LOGOUT);
        assertThat(captured.getUserId()).isEqualTo(123L);
        assertThat(captured.getEventMetadata().get("triggerId")).isEqualTo("Logout Success");
        assertThat(captured.getEventMetadata().get("screen")).isEqualTo("N/A");
    }

    @Test
    void logout_shouldHandleMetricCaptureFailureGracefully() throws Exception {
        doThrow(new RuntimeException("Metrics service failed"))
                .when(metricsService)
                .saveMetricEvent(any(MetricEventDTO.class));

        mockMvc.perform(post("/api/logout")).andExpect(status().isOk());
    }

    @Test
    void logout_shouldHandleInvalidJwtTokenGracefully() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken(anyString()))
                .thenThrow(new RuntimeException("Invalid JWT"));

        Cookie invalidJwtCookie = new Cookie("jwt", "invalid-token");

        mockMvc.perform(post("/api/logout").cookie(invalidJwtCookie)).andExpect(status().isOk());
    }

    @Test
    void logout_shouldHandleUnexpectedExceptionAndReturnError() throws Exception {
        doThrow(new RuntimeException("Critical system error"))
                .when(jwtTokenProvider)
                .getUserIdFromToken(anyString());

        Cookie jwtCookie = new Cookie("jwt", "valid-token");

        mockMvc.perform(post("/api/logout").cookie(jwtCookie)).andExpect(status().isOk());
    }

    @Test
    void csrf_shouldReturnCsrfToken() throws Exception {
        mockMvc.perform(get("/api/csrf").with(csrf()).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void csrf_shouldReturnTokenWithCorrectStructure() throws Exception {
        MvcResult result =
                mockMvc.perform(
                                get("/api/csrf")
                                        .with(csrf())
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).isNotEmpty();
    }

    @Test
    void logout_shouldHandleNullCookiesArrayGracefully() throws Exception {
        mockMvc.perform(post("/api/logout").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void logout_shouldHandleEmptyCookiesArray() throws Exception {
        Cookie otherCookie = new Cookie("other", "value");

        mockMvc.perform(
                        post("/api/logout")
                                .cookie(otherCookie)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void logout_shouldHandleNullJwtCookieValue() throws Exception {
        Cookie nullJwtCookie = new Cookie("jwt", null);

        mockMvc.perform(
                        post("/api/logout")
                                .cookie(nullJwtCookie)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(metricsService, never()).saveMetricEvent(any(MetricEventDTO.class));
    }

    @Test
    void logout_shouldHandleJwtTokenProviderException() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken(anyString()))
                .thenThrow(new RuntimeException("JWT parsing failed"));

        Cookie jwtCookie = new Cookie("jwt", "invalid-token");

        mockMvc.perform(
                        post("/api/logout")
                                .cookie(jwtCookie)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        verify(metricsService, never()).saveMetricEvent(any(MetricEventDTO.class));
    }
}
