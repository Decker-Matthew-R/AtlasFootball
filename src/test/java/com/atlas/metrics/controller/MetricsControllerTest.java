package com.atlas.metrics.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metrics.controller.model.MetricEventDTO;
import com.atlas.metrics.controller.model.MetricEventType;
import com.atlas.metrics.service.MetricsService;
import com.atlas.testFactories.MetricEventTestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = MetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = MetricsController.class)
class MetricsControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private MetricsService mockMetricsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("when a metric request is received, the service is called and 201 is returned")
    void whenMetricEventDTOIsReceived_MetricsServiceIsCalled_NoContentIsReturned()
            throws Exception {
        JSONObject eventMetadata = new JSONObject();
        eventMetadata.put("buttonId", "submit");
        eventMetadata.put("screen", "login");

        MetricEventDTO metricEventDTO =
                MetricEventTestFactory.createMockMetricEventDTO(
                        MetricEventType.BUTTON_CLICK, eventMetadata);

        String requestJson = objectMapper.writeValueAsString(metricEventDTO);

        mockMvc.perform(
                        post("/api/save-metric")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                                .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(mockMetricsService, times(1)).saveMetricEvent(metricEventDTO);
        verifyNoMoreInteractions(mockMetricsService);
    }

    @Test
    @DisplayName("when a metric request is received, the service is called and 500 is returned")
    void shouldReturn500WhenServiceThrowsException() throws Exception {

        JSONObject eventMetadata = new JSONObject();
        eventMetadata.put("buttonId", "submit");
        eventMetadata.put("screen", "login");

        MetricEventDTO metricEventDTO =
                MetricEventTestFactory.createMockMetricEventDTO(
                        MetricEventType.BUTTON_CLICK, eventMetadata);

        String requestJson = objectMapper.writeValueAsString(metricEventDTO);

        doThrow(new RuntimeException("Database error"))
                .when(mockMetricsService)
                .saveMetricEvent(any(MetricEventDTO.class));

        mockMvc.perform(
                        post("/api/save-metric")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(""));

        verify(mockMetricsService, times(1)).saveMetricEvent(any(MetricEventDTO.class));
    }

    @Test
    @DisplayName("Should return 400 when request body is invalid JSON")
    void shouldReturn400WhenRequestBodyIsInvalidJson() throws Exception {
        mockMvc.perform(
                        post("/api/save-metric")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{ invalid json }")
                                .with(csrf()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mockMetricsService);
    }

    @Test
    @DisplayName("Should save metric with userId when provided")
    void shouldSaveMetricWithUserId() throws Exception {
        String requestJson =
                """
        {
            "event": "BUTTON_CLICK",
            "eventMetadata": {"buttonId": "submit", "screen": "login"},
            "userId": 123
        }
        """;

        mockMvc.perform(
                        post("/api/save-metric")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                                .with(csrf()))
                .andExpect(status().isCreated());

        ArgumentCaptor<MetricEventDTO> captor = ArgumentCaptor.forClass(MetricEventDTO.class);
        verify(mockMetricsService).saveMetricEvent(captor.capture());

        MetricEventDTO captured = captor.getValue();
        assertEquals(123L, captured.getUserId());
    }

    @Test
    @DisplayName("Should save metric without userId when not provided")
    void shouldSaveMetricWithoutUserId() throws Exception {
        String requestJson =
                """
        {
            "event": "BUTTON_CLICK",
            "eventMetadata": {"buttonId": "submit", "screen": "login"}
        }
        """;

        mockMvc.perform(
                        post("/api/save-metric")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                                .with(csrf()))
                .andExpect(status().isCreated());

        ArgumentCaptor<MetricEventDTO> captor = ArgumentCaptor.forClass(MetricEventDTO.class);
        verify(mockMetricsService).saveMetricEvent(captor.capture());

        MetricEventDTO captured = captor.getValue();
        assertNull(captured.getUserId());
    }
}
