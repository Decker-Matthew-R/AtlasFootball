package com.atlas.externalAPIs.apiFootball.service.model.supportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Score {
    private Goals halftime;
    private Goals fulltime;
    private Goals extratime;
    private Goals penalty;
}
