package com.atlas.metrics.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import com.atlas.metrics.controller.model.MetricEventDTO;
import com.atlas.metrics.controller.model.MetricEventType;
import com.atlas.metrics.repository.MetricsRepository;
import com.atlas.metrics.repository.model.MetricEventEntity;
import com.atlas.testFactories.MetricEventTestFactory;
import java.sql.Timestamp;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock MetricsRepository mockMetricsRepository;

    @InjectMocks MetricsService metricsService;

    @Test
    @DisplayName("when a metric request is received, the repository is called")
    void whenMetricEventDTOIsReceived_MetricRepositoryIsCalled_NoContentReturned() {

        JSONObject eventMetadata = new JSONObject();
        eventMetadata.put("buttonId", "submit");
        eventMetadata.put("screen", "login");

        MetricEventDTO metricEventDTO =
                MetricEventTestFactory.createMockMetricEventDTO(
                        MetricEventType.BUTTON_CLICK, eventMetadata);

        metricsService.saveMetricEvent(metricEventDTO);

        ArgumentCaptor<MetricEventEntity> captor = ArgumentCaptor.forClass(MetricEventEntity.class);
        verify(mockMetricsRepository).save(captor.capture());

        MetricEventEntity capturedEntity = captor.getValue();

        assertEquals(MetricEventType.BUTTON_CLICK.toString(), capturedEntity.getEvent());
        assertEquals(eventMetadata.toString(), capturedEntity.getMetadata());

        Timestamp now = new Timestamp(System.currentTimeMillis());
        assertTrue(
                Math.abs(capturedEntity.getEventTime().getTime() - now.getTime()) < 1000,
                "Timestamp should be within 1 second of the current time");
    }
}
