package com.atlas.externalAPIs.apiFootball.controller.model.supportingTypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamsDto {
    private TeamDto home;
    private TeamDto away;
}
