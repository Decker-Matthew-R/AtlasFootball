package com.atlas.externalAPIs.apiFootball.service;

import com.atlas.externalAPIs.apiFootball.config.ApiFootballConfig;
import com.atlas.externalAPIs.apiFootball.service.model.ExceptionTypes.ApiFootballException;
import com.atlas.externalAPIs.apiFootball.service.model.request.FixtureRequest;
import com.atlas.externalAPIs.apiFootball.service.model.response.FixtureResponse;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
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
            // Log what we're about to send
            log.info("=== API CALL DEBUG ===");
            log.info("URL: {}", url);
            log.info("Headers: {}", headers);

            // First, try to get the raw response as String to see what we're getting
            ResponseEntity<String> rawResponse =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.info("HTTP Status: {}", rawResponse.getStatusCode());
            log.info("Response Headers: {}", rawResponse.getHeaders());
            log.info("Raw Response Body: {}", rawResponse.getBody());

            // Now try to parse it to your FixtureResponse class
            ResponseEntity<FixtureResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, FixtureResponse.class);
            log.info("Successfully parsed to FixtureResponse: {}", response.getBody());

            return response.getBody();
        } catch (HttpClientErrorException e) {
            // HTTP 4xx errors (401, 403, 404, etc.)
            log.error(
                    "HTTP Client Error: Status={}, Body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new ApiFootballException("HTTP error: " + e.getStatusCode(), e);
        } catch (HttpServerErrorException e) {
            // HTTP 5xx errors
            log.error(
                    "HTTP Server Error: Status={}, Body={}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new ApiFootballException("Server error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            // Network/timeout issues
            log.error("Network/Timeout Error: {}", e.getMessage());
            throw new ApiFootballException("Network error", e);
        } catch (Exception e) {
            // Parsing/serialization errors or other issues
            log.error("Unexpected error: {}", e.getMessage(), e);
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
