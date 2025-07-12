package com.atlas.externalAPIs.apiFootball.service.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FixtureRequest {
    private String from;
    private String to;
    private String timezone;
    private Integer league;
    private String status;
}
