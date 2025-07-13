package com.atlas.externalAPIs.apiFootball.service.model.supportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Teams {
    private Team home;
    private Team away;
}
