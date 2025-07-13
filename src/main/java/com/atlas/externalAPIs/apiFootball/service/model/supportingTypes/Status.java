package com.atlas.externalAPIs.apiFootball.service.model.supportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {
    @JsonProperty("long")
    private String longStatus;

    @JsonProperty("short")
    private String shortStatus;

    private Integer elapsed;
}
