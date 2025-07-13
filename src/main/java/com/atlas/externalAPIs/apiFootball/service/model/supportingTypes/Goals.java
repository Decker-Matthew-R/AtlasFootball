package com.atlas.externalAPIs.apiFootball.service.model.supportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Goals {
    private Integer home;
    private Integer away;
}
