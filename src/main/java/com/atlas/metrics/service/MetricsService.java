package com.atlas.metrics.service;

import com.atlas.metrics.controller.model.MetricEventDTO;
import com.atlas.metrics.repository.MetricsRepository;
import com.atlas.metrics.repository.model.MetricEventEntity;
import java.sql.Timestamp;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MetricsService {

    private final MetricsRepository metricsRepository;

    public void saveMetricEvent(MetricEventDTO metricEventDTO) {
        MetricEventEntity metricEventEntity =
                metricEventDTOToMetricEventEntityConversion(metricEventDTO);
        metricsRepository.save(metricEventEntity);
    }

    private MetricEventEntity metricEventDTOToMetricEventEntityConversion(
            MetricEventDTO metricEventDTO) {
        return MetricEventEntity.builder()
                .event(metricEventDTO.getEvent().name())
                .eventTime(Timestamp.from(Instant.now()))
                .metadata(metricEventDTO.getEventMetadata().toString())
                .build();
    }
}
