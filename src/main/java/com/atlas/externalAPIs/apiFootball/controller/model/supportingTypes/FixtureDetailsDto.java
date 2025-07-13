package com.atlas.externalAPIs.apiFootball.controller.model.supportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FixtureDetailsDto {
    private Long id;
    private String date;
    private String timezone;
    private VenueDto venue;
    private StatusDto status;
}
