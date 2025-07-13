package com.atlas.externalAPIs.apiFootball.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.atlas.externalAPIs.apiFootball.config.ApiFootballConfig;
import com.atlas.externalAPIs.apiFootball.service.model.ExceptionTypes.ApiFootballException;
import com.atlas.externalAPIs.apiFootball.service.model.Fixture;
import com.atlas.externalAPIs.apiFootball.service.model.request.FixtureRequest;
import com.atlas.externalAPIs.apiFootball.service.model.response.FixtureResponse;
import com.atlas.externalAPIs.apiFootball.service.model.supportingTypes.FixtureDetails;
import com.atlas.externalAPIs.apiFootball.service.model.supportingTypes.League;
import com.atlas.externalAPIs.apiFootball.service.model.supportingTypes.Team;
import com.atlas.externalAPIs.apiFootball.service.model.supportingTypes.Teams;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ApiFootballServiceTest {

    @Mock private RestTemplate restTemplate;

    @Mock private ApiFootballConfig config;

    private ApiFootballService service;

    @BeforeEach
    void setUp() {
        service = new ApiFootballService(restTemplate, config);
    }

    @Test
    void getUpcomingFixtures_ShouldReturnFixtures_WhenApiCallSucceeds() {
        setupConfigMocks();
        FixtureResponse mockResponse = createMockFixtureResponse();
        ResponseEntity<FixtureResponse> responseEntity = ResponseEntity.ok(mockResponse);

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(responseEntity);

        FixtureResponse result = service.getUpcomingFixtures();

        assertThat(result).isNotNull();
        assertThat(result.getResults()).isEqualTo(2);
        assertThat(result.getResponse()).hasSize(2);
        assertThat(result.getResponse().get(0).getTeams().getHome().getName())
                .isEqualTo("Manchester United");
        assertThat(result.getResponse().get(1).getTeams().getAway().getName())
                .isEqualTo("Liverpool");
    }

    @Test
    void getUpcomingFixtures_ShouldThrowException_WhenApiCallFails() {
        setupConfigMocks();
        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenThrow(new RestClientException("API connection failed"));
        assertThatThrownBy(() -> service.getUpcomingFixtures())
                .isInstanceOf(ApiFootballException.class)
                .hasMessage("Failed to fetch fixtures")
                .hasCauseInstanceOf(RestClientException.class);
    }

    @Test
    void getUpcomingFixtures_ShouldCallCorrectUrl() {
        setupConfigMocks();
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        String expectedUrl =
                "https://api-football-v1.p.rapidapi.com/v3/fixtures"
                        + "?from="
                        + today.toString()
                        + "&to="
                        + nextWeek.toString()
                        + "&timezone=UTC";

        FixtureResponse mockResponse = createMockFixtureResponse();
        ResponseEntity<FixtureResponse> responseEntity = ResponseEntity.ok(mockResponse);

        when(restTemplate.exchange(
                        eq(expectedUrl),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(responseEntity);

        service.getUpcomingFixtures();
    }

    @Test
    void getUpcomingFixtures_ShouldIncludeCorrectHeaders() {
        setupConfigMocks();
        FixtureResponse mockResponse = createMockFixtureResponse();
        ResponseEntity<FixtureResponse> responseEntity = ResponseEntity.ok(mockResponse);

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        argThat(
                                entity -> {
                                    HttpHeaders headers = entity.getHeaders();
                                    return headers.get("X-RapidAPI-Key").contains("test-api-key")
                                            && headers.get("X-RapidAPI-Host")
                                                    .contains("api-football-v1.p.rapidapi.com")
                                            && headers.getContentType()
                                                    .equals(MediaType.APPLICATION_JSON);
                                }),
                        eq(FixtureResponse.class)))
                .thenReturn(responseEntity);

        service.getUpcomingFixtures();
    }

    @Test
    void getUpcomingFixtures_ShouldReturnNull_WhenResponseBodyIsNull() {
        setupConfigMocks();
        ResponseEntity<FixtureResponse> responseEntity = ResponseEntity.ok(null);

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(responseEntity);

        FixtureResponse result = service.getUpcomingFixtures();

        assertThat(result).isNull();
    }

    @Test
    void buildUrl_ShouldHandleAllNullParameters() {
        when(config.getBaseUrl()).thenReturn("https://api-football-v1.p.rapidapi.com/v3");

        FixtureRequest request =
                FixtureRequest.builder().from(null).to(null).timezone(null).build();

        String url = service.buildUrl(request);

        assertThat(url).isEqualTo("https://api-football-v1.p.rapidapi.com/v3/fixtures");
    }

    @Test
    void buildUrl_ShouldHandleNullFromParameter() {
        when(config.getBaseUrl()).thenReturn("https://api-football-v1.p.rapidapi.com/v3");

        FixtureRequest request =
                FixtureRequest.builder().from(null).to("2024-01-22").timezone("UTC").build();

        String url = service.buildUrl(request);

        assertThat(url)
                .isEqualTo(
                        "https://api-football-v1.p.rapidapi.com/v3/fixtures?to=2024-01-22&timezone=UTC");
        assertThat(url).doesNotContain("from=");
    }

    @Test
    void buildUrl_ShouldHandleNullToParameter() {
        when(config.getBaseUrl()).thenReturn("https://api-football-v1.p.rapidapi.com/v3");

        FixtureRequest request =
                FixtureRequest.builder().from("2024-01-15").to(null).timezone("UTC").build();

        String url = service.buildUrl(request);

        assertThat(url)
                .isEqualTo(
                        "https://api-football-v1.p.rapidapi.com/v3/fixtures?from=2024-01-15&timezone=UTC");
        assertThat(url).doesNotContain("to=");
    }

    @Test
    void buildUrl_ShouldHandleNullTimezoneParameter() {
        when(config.getBaseUrl()).thenReturn("https://api-football-v1.p.rapidapi.com/v3");

        FixtureRequest request =
                FixtureRequest.builder().from("2024-01-15").to("2024-01-22").timezone(null).build();

        String url = service.buildUrl(request);

        assertThat(url)
                .isEqualTo(
                        "https://api-football-v1.p.rapidapi.com/v3/fixtures?from=2024-01-15&to=2024-01-22");
        assertThat(url).doesNotContain("timezone=");
    }

    @Test
    void buildUrl_ShouldHandleAllParametersPresent() {
        when(config.getBaseUrl()).thenReturn("https://api-football-v1.p.rapidapi.com/v3");

        FixtureRequest request =
                FixtureRequest.builder()
                        .from("2024-01-15")
                        .to("2024-01-22")
                        .timezone("UTC")
                        .build();

        String url = service.buildUrl(request);

        assertThat(url)
                .isEqualTo(
                        "https://api-football-v1.p.rapidapi.com/v3/fixtures?from=2024-01-15&to=2024-01-22&timezone=UTC");
    }

    private void setupConfigMocks() {
        when(config.getBaseUrl()).thenReturn("https://api-football-v1.p.rapidapi.com/v3");
        when(config.getApiKey()).thenReturn("test-api-key");
        when(config.getApiHost()).thenReturn("api-football-v1.p.rapidapi.com");
    }

    private FixtureResponse createMockFixtureResponse() {
        FixtureResponse response = new FixtureResponse();
        response.setResults(2);
        response.setResponse(createMockFixtures());
        return response;
    }

    private List<Fixture> createMockFixtures() {
        Fixture fixture1 =
                createMockFixture(
                        1L,
                        "2024-01-15T15:00:00+00:00",
                        "Manchester United",
                        "Chelsea",
                        "Premier League");

        Fixture fixture2 =
                createMockFixture(
                        2L, "2024-01-16T17:30:00+00:00", "Arsenal", "Liverpool", "Premier League");

        return Arrays.asList(fixture1, fixture2);
    }

    private Fixture createMockFixture(
            Long id, String date, String homeTeam, String awayTeam, String leagueName) {
        Fixture fixture = new Fixture();

        FixtureDetails fixtureDetails = new FixtureDetails();
        fixtureDetails.setId(id);
        fixtureDetails.setDate(date);
        fixture.setFixture(fixtureDetails);

        Teams teams = new Teams();
        Team home = new Team();
        home.setName(homeTeam);
        Team away = new Team();
        away.setName(awayTeam);
        teams.setHome(home);
        teams.setAway(away);
        fixture.setTeams(teams);

        League league = new League();
        league.setName(leagueName);
        fixture.setLeague(league);

        return fixture;
    }
}
