package com.atlas.externalAPIs.apiFootball.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.atlas.externalAPIs.apiFootball.config.ApiFootballConfig;
import com.atlas.externalAPIs.apiFootball.service.model.ExceptionTypes.ApiFootballException;
import com.atlas.externalAPIs.apiFootball.service.model.Fixture;
import com.atlas.externalAPIs.apiFootball.service.model.LeagueEnum;
import com.atlas.externalAPIs.apiFootball.service.model.request.FixtureRequest;
import com.atlas.externalAPIs.apiFootball.service.model.response.FixtureResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ApiFootballServiceTest {

    @Mock private RestTemplate restTemplate;

    @Mock private ApiFootballConfig config;

    @InjectMocks private ApiFootballService apiFootballService;

    private static final String BASE_URL = "https://api-football-v1.p.rapidapi.com/v3";
    private static final String API_KEY = "test-api-key";
    private static final String API_HOST = "api-football-v1.p.rapidapi.com";

    @BeforeEach
    void setUp() {
        when(config.getBaseUrl()).thenReturn(BASE_URL);
    }

    @Test
    void getUpcomingFixturesForTopFiveLeagues_SuccessfulResponse_ReturnsAllFixtures() {
        List<Fixture> premierLeagueFixtures = createMockFixtures(30);
        List<Fixture> laLigaFixtures = createMockFixtures(30);
        List<Fixture> bundesligaFixtures = createMockFixtures(30);
        List<Fixture> serieAFixtures = createMockFixtures(30);
        List<Fixture> ligue1Fixtures = createMockFixtures(30);

        FixtureResponse premierLeagueResponse = createMockResponse(premierLeagueFixtures);
        FixtureResponse laLigaResponse = createMockResponse(laLigaFixtures);
        FixtureResponse bundesligaResponse = createMockResponse(bundesligaFixtures);
        FixtureResponse serieAResponse = createMockResponse(serieAFixtures);
        FixtureResponse ligue1Response = createMockResponse(ligue1Fixtures);

        when(restTemplate.exchange(
                        contains("league=39"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(premierLeagueResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                        contains("league=140"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(laLigaResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                        contains("league=78"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(bundesligaResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                        contains("league=135"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(serieAResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                        contains("league=61"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(ligue1Response, HttpStatus.OK));

        try (MockedStatic<LeagueEnum> leagueMock = mockStatic(LeagueEnum.class)) {
            leagueMock
                    .when(LeagueEnum::getTopFiveLeagues)
                    .thenReturn(
                            Arrays.asList(
                                    LeagueEnum.PREMIER_LEAGUE,
                                    LeagueEnum.LA_LIGA,
                                    LeagueEnum.BUNDESLIGA,
                                    LeagueEnum.SERIE_A,
                                    LeagueEnum.LIGUE_1));

            FixtureResponse result = apiFootballService.getUpcomingFixturesForTopFiveLeagues();

            assertNotNull(result);
            assertEquals(150, result.getResults());
            assertEquals(150, result.getResponse().size());

            verify(restTemplate, times(5))
                    .exchange(
                            anyString(),
                            eq(HttpMethod.GET),
                            any(HttpEntity.class),
                            eq(FixtureResponse.class));
        }
    }

    @Test
    void getUpcomingFixturesForTopFiveLeagues_OneLeagueFailsOthersSucceed_ReturnsPartialResults() {
        List<Fixture> premierLeagueFixtures = createMockFixtures(30);
        List<Fixture> laLigaFixtures = createMockFixtures(30);
        List<Fixture> bundesligaFixtures = createMockFixtures(30);
        List<Fixture> serieAFixtures = createMockFixtures(30);

        FixtureResponse premierLeagueResponse = createMockResponse(premierLeagueFixtures);
        FixtureResponse laLigaResponse = createMockResponse(laLigaFixtures);
        FixtureResponse bundesligaResponse = createMockResponse(bundesligaFixtures);
        FixtureResponse serieAResponse = createMockResponse(serieAFixtures);

        when(restTemplate.exchange(
                        contains("league=39"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(premierLeagueResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                        contains("league=140"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(laLigaResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                        contains("league=78"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(bundesligaResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                        contains("league=135"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(serieAResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                        contains("league=61"),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        try (MockedStatic<LeagueEnum> leagueMock = mockStatic(LeagueEnum.class)) {
            leagueMock
                    .when(LeagueEnum::getTopFiveLeagues)
                    .thenReturn(
                            Arrays.asList(
                                    LeagueEnum.PREMIER_LEAGUE,
                                    LeagueEnum.LA_LIGA,
                                    LeagueEnum.BUNDESLIGA,
                                    LeagueEnum.SERIE_A,
                                    LeagueEnum.LIGUE_1));

            FixtureResponse result = apiFootballService.getUpcomingFixturesForTopFiveLeagues();

            assertNotNull(result);
            assertEquals(120, result.getResults());
            assertEquals(120, result.getResponse().size());
        }
    }

    @Test
    void getUpcomingFixturesForTopFiveLeagues_AllLeaguesFail_ReturnsEmptyResponse() {
        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        try (MockedStatic<LeagueEnum> leagueMock = mockStatic(LeagueEnum.class)) {
            leagueMock
                    .when(LeagueEnum::getTopFiveLeagues)
                    .thenReturn(
                            Arrays.asList(
                                    LeagueEnum.PREMIER_LEAGUE,
                                    LeagueEnum.LA_LIGA,
                                    LeagueEnum.BUNDESLIGA,
                                    LeagueEnum.SERIE_A,
                                    LeagueEnum.LIGUE_1));

            FixtureResponse result = apiFootballService.getUpcomingFixturesForTopFiveLeagues();

            assertNotNull(result);
            assertEquals(0, result.getResults());
            assertEquals(0, result.getResponse().size());
        }
    }

    @Test
    void getUpcomingFixturesForTopFiveLeagues_NullResponseFromAPI_HandlesGracefully() {
        FixtureResponse nullResponse = new FixtureResponse();
        nullResponse.setResponse(null);

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(nullResponse, HttpStatus.OK));

        try (MockedStatic<LeagueEnum> leagueMock = mockStatic(LeagueEnum.class)) {
            leagueMock
                    .when(LeagueEnum::getTopFiveLeagues)
                    .thenReturn(
                            Arrays.asList(
                                    LeagueEnum.PREMIER_LEAGUE,
                                    LeagueEnum.LA_LIGA,
                                    LeagueEnum.BUNDESLIGA,
                                    LeagueEnum.SERIE_A,
                                    LeagueEnum.LIGUE_1));

            FixtureResponse result = apiFootballService.getUpcomingFixturesForTopFiveLeagues();

            assertNotNull(result);
            assertEquals(0, result.getResults());
            assertEquals(0, result.getResponse().size());
        }
    }

    @Test
    void buildUrl_AllParameters_BuildsCorrectUrl() {
        FixtureRequest request =
                FixtureRequest.builder().next("30").league("39").timezone("UTC").build();

        String result = apiFootballService.buildUrl(request);

        assertTrue(result.contains(BASE_URL + "/fixtures"));
        assertTrue(result.contains("next=30"));
        assertTrue(result.contains("league=39"));
        assertTrue(result.contains("timezone=UTC"));
    }

    @Test
    void buildUrl_OnlyNextParameter_BuildsCorrectUrl() {
        FixtureRequest request = FixtureRequest.builder().next("20").build();

        String result = apiFootballService.buildUrl(request);

        assertTrue(result.contains(BASE_URL + "/fixtures"));
        assertTrue(result.contains("next=20"));
        assertFalse(result.contains("league="));
        assertFalse(result.contains("timezone="));
    }

    @Test
    void buildUrl_OnlyLeagueParameter_BuildsCorrectUrl() {
        FixtureRequest request = FixtureRequest.builder().league("140").build();

        String result = apiFootballService.buildUrl(request);

        assertTrue(result.contains(BASE_URL + "/fixtures"));
        assertTrue(result.contains("league=140"));
        assertFalse(result.contains("next="));
        assertFalse(result.contains("timezone="));
    }

    @Test
    void buildUrl_OnlyTimezoneParameter_BuildsCorrectUrl() {
        FixtureRequest request = FixtureRequest.builder().timezone("Europe/London").build();

        String result = apiFootballService.buildUrl(request);

        assertTrue(result.contains(BASE_URL + "/fixtures"));
        assertTrue(result.contains("timezone=Europe/London"));
        assertFalse(result.contains("next="));
        assertFalse(result.contains("league="));
    }

    @Test
    void buildUrl_NoParameters_BuildsBaseUrl() {
        FixtureRequest request = FixtureRequest.builder().build();

        String result = apiFootballService.buildUrl(request);

        assertEquals(BASE_URL + "/fixtures", result);
    }

    @Test
    void callFixturesApi_SuccessfulResponse_ReturnsFixtureResponse() {
        when(config.getApiKey()).thenReturn(API_KEY);
        when(config.getApiHost()).thenReturn(API_HOST);
        FixtureRequest request =
                FixtureRequest.builder().next("30").league("39").timezone("UTC").build();

        List<Fixture> fixtures = createMockFixtures(30);
        FixtureResponse expectedResponse = createMockResponse(fixtures);

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        FixtureResponse result = apiFootballService.callFixturesApi(request);

        assertNotNull(result);
        assertEquals(30, result.getResults());
        assertEquals(30, result.getResponse().size());

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate)
                .exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        entityCaptor.capture(),
                        eq(FixtureResponse.class));

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertEquals(API_KEY, headers.getFirst("X-RapidAPI-Key"));
        assertEquals(API_HOST, headers.getFirst("X-RapidAPI-Host"));
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
    }

    @Test
    void callFixturesApi_ApiThrowsException_ThrowsApiFootballException() {
        FixtureRequest request =
                FixtureRequest.builder().next("30").league("39").timezone("UTC").build();

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Forbidden"));

        ApiFootballException exception =
                assertThrows(
                        ApiFootballException.class,
                        () -> {
                            apiFootballService.callFixturesApi(request);
                        });

        assertEquals("Failed to fetch fixtures", exception.getMessage());
        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof HttpClientErrorException);
    }

    @Test
    void callFixturesApi_NullResponseBody_ThrowsAssertionError() {
        FixtureRequest request =
                FixtureRequest.builder().next("30").league("39").timezone("UTC").build();

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThrows(
                AssertionError.class,
                () -> {
                    apiFootballService.callFixturesApi(request);
                });
    }

    @Test
    void getFixturesForLeague_ValidLeagueId_CallsApiWithCorrectParameters() {
        String leagueId = "39";
        List<Fixture> fixtures = createMockFixtures(30);
        FixtureResponse expectedResponse = createMockResponse(fixtures);

        when(restTemplate.exchange(
                        anyString(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        FixtureResponse result = apiFootballService.getFixturesForLeague(leagueId);

        assertNotNull(result);
        assertEquals(30, result.getResults());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(restTemplate)
                .exchange(
                        urlCaptor.capture(),
                        eq(HttpMethod.GET),
                        any(HttpEntity.class),
                        eq(FixtureResponse.class));

        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.contains("next=30"));
        assertTrue(capturedUrl.contains("league=" + leagueId));
        assertTrue(capturedUrl.contains("timezone=UTC"));
    }

    private List<Fixture> createMockFixtures(int count) {
        List<Fixture> fixtures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Fixture fixture = new Fixture();
            fixtures.add(fixture);
        }
        return fixtures;
    }

    private FixtureResponse createMockResponse(List<Fixture> fixtures) {
        FixtureResponse response = new FixtureResponse();
        response.setResponse(fixtures);
        response.setResults(fixtures.size());
        return response;
    }
}
