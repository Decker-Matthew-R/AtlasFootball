package com.atlas.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metrics.controller.model.MetricEventDTO;
import com.atlas.metrics.controller.model.MetricEventType;
import com.atlas.metrics.repository.MetricsRepository;
import com.atlas.metrics.repository.model.MetricEventEntity;
import com.atlas.testFactories.MetricEventTestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class MetricsIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired EntityManager entityManager;

    @Autowired MetricsRepository metricsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    @Transactional
    void resetDatabase() {
        entityManager
                .createNativeQuery("TRUNCATE TABLE metrics RESTART IDENTITY CASCADE;")
                .executeUpdate();
    }

    @Test
    @DisplayName("INT - when a user performs an action, a metric is recorded to the database")
    void whenAnActionIsPerformed_AMetricIsRecorded() throws Exception {
        JSONObject eventMetadata1 = new JSONObject();
        eventMetadata1.put("buttonId", "submit");
        eventMetadata1.put("screen", "login");

        JSONObject eventMetadata2 = new JSONObject();
        eventMetadata1.put("paymentType", "Card");
        eventMetadata1.put("screen", "checkout");

        MetricEventType eventType1 = MetricEventType.BUTTON_CLICK;
        MetricEventType eventType2 = MetricEventType.PAYMENT_SUBMITTED;

        MetricEventDTO incomingMetricEventDTO1 =
                MetricEventTestFactory.createMockMetricEventDTO(eventType1, eventMetadata1);

        MetricEventDTO incomingMetricEventDTO2 =
                MetricEventTestFactory.createMockMetricEventDTO(eventType2, eventMetadata2);

        String requestJson1 = objectMapper.writeValueAsString(incomingMetricEventDTO1);
        String requestJson2 = objectMapper.writeValueAsString(incomingMetricEventDTO2);

        mockMvc.perform(
                        post("/api/save-metric")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson1)
                                .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(
                        post("/api/save-metric")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson2)
                                .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<MetricEventEntity> actualSavedMetricEventEntity = metricsRepository.findAll();

        assertThat(actualSavedMetricEventEntity).hasSize(2);

        assertThat(actualSavedMetricEventEntity.get(0).getEvent()).isEqualTo(eventType1.toString());
        assertThat(actualSavedMetricEventEntity.get(0).getMetadata())
                .isEqualTo(eventMetadata1.toString());

        Timestamp now1 = new Timestamp(System.currentTimeMillis());
        long timeDifference1 =
                Math.abs(
                        actualSavedMetricEventEntity.get(0).getEventTime().getTime()
                                - now1.getTime());
        assertThat(timeDifference1).isLessThan(3000);

        assertThat(actualSavedMetricEventEntity.get(1).getEvent()).isEqualTo(eventType2.toString());
        assertThat(actualSavedMetricEventEntity.get(1).getMetadata())
                .isEqualTo(eventMetadata2.toString());

        Timestamp now2 = new Timestamp(System.currentTimeMillis());
        long timeDifference2 =
                Math.abs(
                        actualSavedMetricEventEntity.get(1).getEventTime().getTime()
                                - now2.getTime());
        assertThat(timeDifference2).isLessThan(3000);
    }
}
