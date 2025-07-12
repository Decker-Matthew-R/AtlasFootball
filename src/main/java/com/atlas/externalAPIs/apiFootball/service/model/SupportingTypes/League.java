package com.atlas.externalAPIs.apiFootball.service.model.SupportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class League {
    private Long id;
    private String name;
    private String country;
    private String logo;
    private String flag;
    private Integer season;
    private String round;
}
