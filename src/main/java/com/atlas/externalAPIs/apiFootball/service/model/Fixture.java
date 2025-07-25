package com.atlas.externalAPIs.apiFootball.service.model;

import com.atlas.externalAPIs.apiFootball.service.model.supportingTypes.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Fixture {
    private FixtureDetails fixture;
    private com.atlas.externalAPIs.apiFootball.service.model.supportingTypes.League league;
    private LeagueEnum leagueEnum;
    private Teams teams;
    private Goals goals;
    private Score score;
}
