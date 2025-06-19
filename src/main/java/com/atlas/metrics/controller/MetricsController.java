package com.atlas.metrics.controller;

import com.atlas.metrics.controller.model.MetricEventDTO;
import com.atlas.metrics.service.MetricsService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    @PostMapping("/api/save-metric")
    public ResponseEntity<Void> saveMetricEvent(@RequestBody MetricEventDTO metricEventDTO) {
        try {
            metricsService.saveMetricEvent(metricEventDTO);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
