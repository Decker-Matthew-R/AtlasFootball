package com.atlas.externalAPIs.apiFootball.service.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FixtureRequest {
    private String from;
    private String to;
    private String timezone;
    private String league;
    private String status;
    private String next;
}
