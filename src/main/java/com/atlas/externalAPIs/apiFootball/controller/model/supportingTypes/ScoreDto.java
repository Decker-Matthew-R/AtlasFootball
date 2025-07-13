package com.atlas.externalAPIs.apiFootball.controller.model.supportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScoreDto {
    private GoalsDto halftime;
    private GoalsDto fulltime;
    private GoalsDto extratime;
    private GoalsDto penalty;
}
