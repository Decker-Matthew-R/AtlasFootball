package com.atlas.metrics.controller.model;

import lombok.*;
import net.minidev.json.JSONObject;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetricEventDTO {
    MetricEventType event;
    JSONObject eventMetadata;
    Long userId;
}
