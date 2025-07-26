package com.atlas.externalAPIs.apiFootball.controller;

import com.atlas.externalAPIs.apiFootball.controller.model.supportingTypes.FixtureResponseDto;
import com.atlas.externalAPIs.apiFootball.service.ApiFootballService;
import com.atlas.externalAPIs.apiFootball.service.FixtureMapperService;
import com.atlas.externalAPIs.apiFootball.service.model.ExceptionTypes.ApiFootballException;
import com.atlas.externalAPIs.apiFootball.service.model.response.FixtureResponse;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fixtures")
@RequiredArgsConstructor
@Slf4j
public class ApiFootballController {

    private final ApiFootballService apiFootballService;
    private final FixtureMapperService fixtureMapperService;

    @GetMapping("/upcoming")
    public ResponseEntity<FixtureResponseDto> getUpcomingFixtures() {
        try {

            FixtureResponse serviceResponse =
                    apiFootballService.getUpcomingFixturesForTopFiveLeagues();
            FixtureResponseDto responseDto = fixtureMapperService.mapToDto(serviceResponse);

            log.info("Successfully retrieved {} fixtures", responseDto.getResults());
            return ResponseEntity.ok(responseDto);

        } catch (ApiFootballException e) {
            log.error("API Football service error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(createErrorResponse("External service unavailable", 0));

        } catch (Exception e) {
            log.error("Unexpected error fetching fixtures: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Internal server error", 0));
        }
    }

    private FixtureResponseDto createErrorResponse(String message, int results) {
        FixtureResponseDto errorResponse = new FixtureResponseDto();
        errorResponse.setStatus("error");
        errorResponse.setMessage(message);
        errorResponse.setResults(results);
        errorResponse.setFixtures(Collections.emptyList());
        return errorResponse;
    }
}
