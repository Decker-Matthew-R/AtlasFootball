package com.atlas.externalAPIs.apiFootball.service.model.response;

import com.atlas.externalAPIs.apiFootball.service.model.Fixture;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FixtureResponse {
    private String get;
    private Object parameters;
    private Object errors;
    private Integer results;
    private Object paging;
    private List<Fixture> response;
}
