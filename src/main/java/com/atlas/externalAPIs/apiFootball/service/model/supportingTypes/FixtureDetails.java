package com.atlas.externalAPIs.apiFootball.service.model.supportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FixtureDetails {
    private Long id;
    private String referee;
    private String timezone;
    private String date;
    private Long timestamp;
    private Periods periods;
    private Venue venue;
    private Status status;
}
