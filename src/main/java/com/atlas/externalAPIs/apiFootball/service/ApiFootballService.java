package com.atlas.externalAPIs.apiFootball.service;

import com.atlas.externalAPIs.apiFootball.config.ApiFootballConfig;
import com.atlas.externalAPIs.apiFootball.service.model.ExceptionTypes.ApiFootballException;
import com.atlas.externalAPIs.apiFootball.service.model.Fixture;
import com.atlas.externalAPIs.apiFootball.service.model.LeagueEnum;
import com.atlas.externalAPIs.apiFootball.service.model.request.FixtureRequest;
import com.atlas.externalAPIs.apiFootball.service.model.response.FixtureResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class ApiFootballService {

    private final RestTemplate restTemplate;
    private final ApiFootballConfig config;

    public ApiFootballService(RestTemplate restTemplate, ApiFootballConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    @Cacheable(value = "football-fixtures", key = "'top-5-leagues-combined'")
    public FixtureResponse getUpcomingFixturesForTopFiveLeagues() {
        log.info("FOOTBALL FIXTURES API: Data stale - fetching top 5 league matches");

        List<LeagueEnum> topLeagueEnums = LeagueEnum.getTopFiveLeagues();

        List<CompletableFuture<FixtureResponse>> futures =
                topLeagueEnums.stream()
                        .map(
                                leagueEnum ->
                                        CompletableFuture.supplyAsync(
                                                () -> {
                                                    try {
                                                        return getFixturesForLeague(
                                                                leagueEnum.getId());
                                                    } catch (Exception e) {
                                                        log.warn(
                                                                "Failed to fetch fixtures for league {}: {}",
                                                                leagueEnum.getName(),
                                                                e.getMessage());
                                                        return createEmptyResponse();
                                                    }
                                                }))
                        .toList();

        List<FixtureResponse> responses = futures.stream().map(CompletableFuture::join).toList();

        List<Fixture> allFixtures = new ArrayList<>();
        for (FixtureResponse response : responses) {
            if (response.getResponse() != null) {
                allFixtures.addAll(response.getResponse());
            }
        }

        FixtureResponse combinedResponse = new FixtureResponse();
        combinedResponse.setResponse(allFixtures);
        combinedResponse.setResults(allFixtures.size());

        return combinedResponse;
    }

    String buildUrl(FixtureRequest request) {
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromUriString(config.getBaseUrl() + "/fixtures");

        if (request.getNext() != null) {
            builder.queryParam("next", request.getNext());
        }
        if (request.getLeague() != null) {
            builder.queryParam("league", request.getLeague());
        }
        if (request.getTimezone() != null) {
            builder.queryParam("timezone", request.getTimezone());
        }

        return builder.toUriString();
    }

    FixtureResponse callFixturesApi(FixtureRequest request) {
        String url = buildUrl(request);
        log.info("Calling API URL: {}", url);
        log.info("Raw API response about to be called");

        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<FixtureResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, FixtureResponse.class);

            log.info("Raw response status: {}", response.getStatusCode());
            assert response.getBody() != null;
            log.info("Raw response body type: {}", response.getBody().getClass().getSimpleName());

            return response.getBody();
        } catch (Exception e) {
            log.error(
                    "API call failed. Exception type: {}, Message: {}",
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e);
            throw new ApiFootballException("Failed to fetch fixtures", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", config.getApiKey());
        headers.set("X-RapidAPI-Host", config.getApiHost());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    FixtureResponse createEmptyResponse() {
        FixtureResponse empty = new FixtureResponse();
        empty.setResponse(new ArrayList<>());
        empty.setResults(0);
        return empty;
    }

    FixtureResponse getFixturesForLeague(String leagueId) {
        FixtureRequest request =
                FixtureRequest.builder()
                        .next(String.valueOf(30))
                        .league(leagueId)
                        .timezone("UTC")
                        .build();

        return callFixturesApi(request);
    }
}
