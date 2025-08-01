package com.atlas.externalAPIs.apiFootball.controller.model.supportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusDto {
    @JsonProperty("long")
    private String longStatus;

    @JsonProperty("short")
    private String shortStatus;

    private Integer elapsed;
}
