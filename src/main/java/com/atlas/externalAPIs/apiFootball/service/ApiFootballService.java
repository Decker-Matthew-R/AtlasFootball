package com.atlas.externalAPIs.apiFootball.service;

import com.atlas.externalAPIs.apiFootball.config.ApiFootballConfig;
import com.atlas.externalAPIs.apiFootball.service.model.ExceptionTypes.ApiFootballException;
import com.atlas.externalAPIs.apiFootball.service.model.request.FixtureRequest;
import com.atlas.externalAPIs.apiFootball.service.model.response.FixtureResponse;
import java.time.LocalDate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ApiFootballService {

    private final RestTemplate restTemplate;
    private final ApiFootballConfig config;

    public ApiFootballService(RestTemplate restTemplate, ApiFootballConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public FixtureResponse getUpcomingFixtures() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        FixtureRequest request =
                FixtureRequest.builder()
                        .from(today.toString())
                        .to(nextWeek.toString())
                        .timezone("UTC")
                        .build();

        return callFixturesApi(request);
    }

    private FixtureResponse callFixturesApi(FixtureRequest request) {
        String url = buildUrl(request);
        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<FixtureResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, FixtureResponse.class);
            return response.getBody();
        } catch (Exception e) {
            throw new ApiFootballException("Failed to fetch fixtures", e);
        }
    }

    String buildUrl(FixtureRequest request) {
        UriComponentsBuilder builder =
                UriComponentsBuilder.fromUriString(config.getBaseUrl() + "/fixtures");

        if (request.getFrom() != null) {
            builder.queryParam("from", request.getFrom());
        }
        if (request.getTo() != null) {
            builder.queryParam("to", request.getTo());
        }
        if (request.getTimezone() != null) {
            builder.queryParam("timezone", request.getTimezone());
        }

        return builder.toUriString();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", config.getApiKey());
        headers.set("X-RapidAPI-Host", config.getApiHost());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
