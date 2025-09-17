package com.atlas.integration;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.atlas.externalAPIs.apiFootball.config.ApiFootballConfig;
import com.atlas.externalAPIs.apiFootball.service.model.Fixture;
import com.atlas.externalAPIs.apiFootball.service.model.response.FixtureResponse;
import com.atlas.externalAPIs.apiFootball.service.model.supportingTypes.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@TestPropertySource(properties = {"apis.football.baseUrl=http://localhost:8089/v3"})
class ApiFootballControllerIntegrationTest {

    @Autowired private WebApplicationContext webApplicationContext;

    @MockitoBean private RestTemplate restTemplate;

    @Autowired private ApiFootballConfig config;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        Mockito.reset(restTemplate);

        if (redisTemplate != null) {
            try {
                redisTemplate.getConnectionFactory().getConnection().flushAll();
                System.out.println("Redis cache cleared");
            } catch (Exception e) {
                System.out.println("Could not clear Redis cache: " + e.getMessage());
            }
        }

        clearRedisAlternative();
    }

    @Test
    void getUpcomingFixtures_ShouldReturnSuccessfulResponse_WhenApiReturnsValidData()
            throws Exception {
        // Given - Create unique responses for each of the 5 leagues
        FixtureResponse premierLeagueResponse =
                createLeagueFixtureResponse("Premier League", 39L, "England");
        FixtureResponse laLigaResponse = createLeagueFixtureResponse("La Liga", 140L, "Spain");
        FixtureResponse bundesligaResponse =
                createLeagueFixtureResponse("Bundesliga", 78L, "Germany");
        FixtureResponse serieAResponse = createLeagueFixtureResponse("Serie A", 135L, "Italy");
        FixtureResponse ligue1Response = createLeagueFixtureResponse("Ligue 1", 61L, "France");

        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(), eq(FixtureResponse.class)))
                .thenReturn(
                        new ResponseEntity<>(premierLeagueResponse, HttpStatus.OK),
                        new ResponseEntity<>(laLigaResponse, HttpStatus.OK),
                        new ResponseEntity<>(bundesligaResponse, HttpStatus.OK),
                        new ResponseEntity<>(serieAResponse, HttpStatus.OK),
                        new ResponseEntity<>(ligue1Response, HttpStatus.OK));

        mockMvc.perform(get("/api/fixtures/upcoming").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Fixtures retrieved successfully"))
                .andExpect(jsonPath("$.results").value(greaterThan(0)))
                .andExpect(jsonPath("$.fixtures").isArray())
                .andExpect(jsonPath("$.fixtures", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.fixtures[0].fixture.id").exists())
                .andExpect(jsonPath("$.fixtures[0].fixture.date").exists())
                .andExpect(jsonPath("$.fixtures[0].teams.home.name").exists())
                .andExpect(jsonPath("$.fixtures[0].teams.away.name").exists())
                .andExpect(jsonPath("$.fixtures[0].league.name").exists());
    }

    @Test
    void getUpcomingFixtures_ShouldReturnEmptyResponse_WhenAllExternalApiCallsFail()
            throws Exception {
        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(), eq(FixtureResponse.class)))
                .thenThrow(new RestClientException("API connection failed"));

        mockMvc.perform(get("/api/fixtures/upcoming").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Fixtures retrieved successfully"))
                .andExpect(jsonPath("$.results").value(0))
                .andExpect(jsonPath("$.fixtures").isArray())
                .andExpect(jsonPath("$.fixtures", hasSize(0)));

        verify(restTemplate, times(5))
                .exchange(anyString(), eq(HttpMethod.GET), any(), eq(FixtureResponse.class));
    }

    @Test
    void getUpcomingFixtures_ShouldHandlePartialFailures_WhenSomeLeagueCallsFail()
            throws Exception {
        FixtureResponse successResponse1 =
                createLeagueFixtureResponse("Premier League", 39L, "England");
        FixtureResponse successResponse2 = createLeagueFixtureResponse("La Liga", 140L, "Spain");
        FixtureResponse successResponse3 = createLeagueFixtureResponse("Serie A", 135L, "Italy");

        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(), eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(successResponse1, HttpStatus.OK))
                .thenThrow(new RestClientException("League API failed"))
                .thenReturn(new ResponseEntity<>(successResponse2, HttpStatus.OK))
                .thenThrow(new RestClientException("League API failed"))
                .thenReturn(new ResponseEntity<>(successResponse3, HttpStatus.OK));

        mockMvc.perform(get("/api/fixtures/upcoming").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Fixtures retrieved successfully"))
                .andExpect(jsonPath("$.results").value(greaterThan(0)))
                .andExpect(jsonPath("$.fixtures").isArray())
                .andExpect(jsonPath("$.fixtures", hasSize(greaterThan(0))));
    }

    @Test
    void getUpcomingFixtures_ShouldValidateResponseStructure_WhenApiReturnsCompleteData()
            throws Exception {
        FixtureResponse detailedResponse = createDetailedMockFixtureResponse();

        FixtureResponse otherLeagueResponse = createEmptyFixtureResponse();

        when(restTemplate.exchange(
                        anyString(), eq(HttpMethod.GET), any(), eq(FixtureResponse.class)))
                .thenReturn(
                        new ResponseEntity<>(detailedResponse, HttpStatus.OK),
                        new ResponseEntity<>(otherLeagueResponse, HttpStatus.OK),
                        new ResponseEntity<>(otherLeagueResponse, HttpStatus.OK),
                        new ResponseEntity<>(otherLeagueResponse, HttpStatus.OK),
                        new ResponseEntity<>(otherLeagueResponse, HttpStatus.OK));

        mockMvc.perform(get("/api/fixtures/upcoming"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fixtures").isArray())
                .andExpect(jsonPath("$.fixtures", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.fixtures[?(@.fixture.id == 12345)]").exists())
                .andExpect(jsonPath("$.fixtures[?(@.fixture.id == 12345)].fixture.date").exists())
                .andExpect(
                        jsonPath("$.fixtures[?(@.fixture.id == 12345)].fixture.venue.name")
                                .value("Test Stadium"))
                .andExpect(
                        jsonPath("$.fixtures[?(@.fixture.id == 12345)].fixture.venue.city")
                                .value("Test City"))
                .andExpect(
                        jsonPath("$.fixtures[?(@.fixture.id == 12345)].fixture.status.short")
                                .value("NS"))
                .andExpect(
                        jsonPath("$.fixtures[?(@.fixture.id == 12345)].fixture.status.long")
                                .value("Not Started"))
                .andExpect(
                        jsonPath("$.fixtures[?(@.fixture.id == 12345)].teams.home.name")
                                .value("Manchester United"))
                .andExpect(jsonPath("$.fixtures[?(@.fixture.id == 12345)].teams.home.id").value(33))
                .andExpect(
                        jsonPath("$.fixtures[?(@.fixture.id == 12345)].teams.away.name")
                                .value("Liverpool"))
                .andExpect(jsonPath("$.fixtures[?(@.fixture.id == 12345)].teams.away.id").value(40))
                .andExpect(
                        jsonPath("$.fixtures[?(@.fixture.id == 12345)].league.name")
                                .value("Premier League"))
                .andExpect(jsonPath("$.fixtures[?(@.fixture.id == 12345)].league.id").value(39))
                .andExpect(
                        jsonPath("$.fixtures[?(@.fixture.id == 12345)].league.country")
                                .value("England"))
                .andExpect(
                        jsonPath("$.fixtures[?(@.fixture.id == 12345)].league.season").value(2024))
                .andExpect(
                        jsonPath("$.fixtures[?(@.fixture.id == 12345)].league.round")
                                .value("Regular Season - 15"));
    }

    private FixtureResponse createLeagueFixtureResponse(
            String leagueName, Long leagueId, String country) {
        FixtureResponse response = new FixtureResponse();

        Fixture fixture1 =
                createMockFixture(
                        leagueId * 1000 + 1,
                        "Home Team " + leagueId + "A",
                        leagueId * 100 + 1,
                        "Away Team " + leagueId + "A",
                        leagueId * 100 + 2,
                        leagueName,
                        leagueId,
                        country);

        Fixture fixture2 =
                createMockFixture(
                        leagueId * 1000 + 2,
                        "Home Team " + leagueId + "B",
                        leagueId * 100 + 3,
                        "Away Team " + leagueId + "B",
                        leagueId * 100 + 4,
                        leagueName,
                        leagueId,
                        country);

        response.setResponse(Arrays.asList(fixture1, fixture2));
        response.setResults(2);

        return response;
    }

    private FixtureResponse createDetailedMockFixtureResponse() {
        FixtureResponse response = new FixtureResponse();

        Fixture fixture = createDetailedMockFixture();
        response.setResponse(Collections.singletonList(fixture));
        response.setResults(1);

        return response;
    }

    private FixtureResponse createEmptyFixtureResponse() {
        FixtureResponse response = new FixtureResponse();
        response.setResponse(Collections.emptyList());
        response.setResults(0);
        return response;
    }

    private Fixture createMockFixture(
            Long fixtureId,
            String homeName,
            Long homeId,
            String awayName,
            Long awayId,
            String leagueName,
            Long leagueId) {
        return createMockFixture(
                fixtureId, homeName, homeId, awayName, awayId, leagueName, leagueId, "England");
    }

    private Fixture createMockFixture(
            Long fixtureId,
            String homeName,
            Long homeId,
            String awayName,
            Long awayId,
            String leagueName,
            Long leagueId,
            String country) {
        Fixture fixture = new Fixture();

        FixtureDetails details = new FixtureDetails();
        details.setId(fixtureId);
        details.setDate(
                LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setTimezone("UTC");
        fixture.setFixture(details);

        Teams teams = new Teams();

        Team homeTeam = new Team();
        homeTeam.setId(homeId);
        homeTeam.setName(homeName);
        homeTeam.setLogo("https://example.com/home-logo.png");
        teams.setHome(homeTeam);

        Team awayTeam = new Team();
        awayTeam.setId(awayId);
        awayTeam.setName(awayName);
        awayTeam.setLogo("https://example.com/away-logo.png");
        teams.setAway(awayTeam);

        fixture.setTeams(teams);

        League league = new League();
        league.setId(leagueId);
        league.setName(leagueName);
        league.setCountry(country);
        league.setLogo("https://example.com/league-logo.png");
        league.setSeason(2024);
        league.setRound("Regular Season - 15");
        fixture.setLeague(league);

        return fixture;
    }

    private Fixture createDetailedMockFixture() {
        Fixture fixture = new Fixture();

        FixtureDetails details = new FixtureDetails();
        details.setId(12345L);
        details.setDate(
                LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        details.setTimezone("UTC");

        Venue venue = new Venue();
        venue.setId(556L);
        venue.setName("Test Stadium");
        venue.setCity("Test City");
        details.setVenue(venue);

        Status status = new Status();
        status.setShortStatus("NS");
        status.setLongStatus("Not Started");
        details.setStatus(status);

        fixture.setFixture(details);

        Teams teams = new Teams();

        Team homeTeam = new Team();
        homeTeam.setId(33L);
        homeTeam.setName("Manchester United");
        homeTeam.setLogo("https://example.com/mu-logo.png");
        homeTeam.setWinner(null);
        teams.setHome(homeTeam);

        Team awayTeam = new Team();
        awayTeam.setId(40L);
        awayTeam.setName("Liverpool");
        awayTeam.setLogo("https://example.com/lfc-logo.png");
        awayTeam.setWinner(null);
        teams.setAway(awayTeam);

        fixture.setTeams(teams);

        League league = new League();
        league.setId(39L);
        league.setName("Premier League");
        league.setCountry("England");
        league.setLogo("https://example.com/pl-logo.png");
        league.setSeason(2024);
        league.setRound("Regular Season - 15");
        fixture.setLeague(league);

        Goals goals = new Goals();
        goals.setHome(null);
        goals.setAway(null);
        fixture.setGoals(goals);

        Score score = new Score();
        Goals halftimeScore = new Goals();
        halftimeScore.setHome(null);
        halftimeScore.setAway(null);
        score.setHalftime(halftimeScore);

        Goals fulltimeScore = new Goals();
        fulltimeScore.setHome(null);
        fulltimeScore.setAway(null);
        score.setFulltime(fulltimeScore);

        fixture.setScore(score);

        return fixture;
    }

    private void clearRedisAlternative() {
        if (redisTemplate != null) {
            try {
                Set<String> keys = redisTemplate.keys("fixtures:*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    System.out.println("Cleared " + keys.size() + " Redis keys matching pattern");
                }
            } catch (Exception e) {
                System.out.println("Alternative Redis clearing failed: " + e.getMessage());
            }
        }
    }
}
