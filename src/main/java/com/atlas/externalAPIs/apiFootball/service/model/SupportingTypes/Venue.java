package com.atlas.externalAPIs.apiFootball.service.model.SupportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Venue {
    private Long id;
    private String name;
    private String city;
}
