package com.atlas.testFactories;

import com.atlas.metrics.controller.model.MetricEventDTO;
import com.atlas.metrics.controller.model.MetricEventType;
import com.atlas.metrics.repository.model.MetricEventEntity;
import java.sql.Timestamp;
import java.util.Date;
import net.minidev.json.JSONObject;

public class MetricEventTestFactory {

    public static MetricEventDTO createMockMetricEventDTO(
            MetricEventType metricEventType, JSONObject eventMetadata) {
        return MetricEventDTO.builder().event(metricEventType).eventMetadata(eventMetadata).build();
    }

    public static MetricEventEntity convertMetricEventDTOTOMetricEventEntity(
            MetricEventDTO metricEventDTO) {
        return MetricEventEntity.builder()
                .id(null)
                .event(metricEventDTO.getEvent().toString())
                .eventTime(new Timestamp(new Date().getTime()))
                .metadata(metricEventDTO.getEventMetadata().toString())
                .build();
    }
}
