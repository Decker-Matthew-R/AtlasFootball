package com.atlas.externalAPIs.apiFootball.controller.model;

import com.atlas.externalAPIs.apiFootball.controller.model.supportingTypes.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FixtureDto {
    private FixtureDetailsDto fixture;
    private LeagueDto league;
    private TeamsDto teams;
    private GoalsDto goals;
    private ScoreDto score;
}
