package com.atlas.externalAPIs.apiFootball.controller.model.supportingTypes;

import com.atlas.externalAPIs.apiFootball.controller.model.FixtureDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FixtureResponseDto {
    private Integer results;
    private List<FixtureDto> fixtures;
    private String status;
    private String message;
}
