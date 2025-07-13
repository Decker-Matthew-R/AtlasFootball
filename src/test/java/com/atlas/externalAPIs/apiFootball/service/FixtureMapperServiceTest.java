package com.atlas.externalAPIs.apiFootball.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.externalAPIs.apiFootball.controller.model.FixtureDto;
import com.atlas.externalAPIs.apiFootball.controller.model.supportingTypes.*;
import com.atlas.externalAPIs.apiFootball.service.model.Fixture;
import com.atlas.externalAPIs.apiFootball.service.model.response.FixtureResponse;
import com.atlas.externalAPIs.apiFootball.service.model.supportingTypes.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FixtureMapperServiceTest {

    private FixtureMapperService mapperService;

    @BeforeEach
    void setUp() {
        mapperService = new FixtureMapperService();
    }

    @Test
    void mapToDto_ShouldReturnEmptyResponse_WhenInputIsNull() {
        FixtureResponseDto result = mapperService.mapToDto(null);

        assertThat(result).isNotNull();
        assertThat(result.getResults()).isEqualTo(0);
        assertThat(result.getFixtures()).isEmpty();
        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getMessage()).isEqualTo("No fixtures found");
    }

    @Test
    void mapToDto_ShouldMapCompleteFixtureResponse() {
        FixtureResponse input = createCompleteFixtureResponse();

        FixtureResponseDto result = mapperService.mapToDto(input);

        assertThat(result).isNotNull();
        assertThat(result.getResults()).isEqualTo(2);
        assertThat(result.getStatus()).isEqualTo("success");
        assertThat(result.getMessage()).isEqualTo("Fixtures retrieved successfully");
        assertThat(result.getFixtures()).hasSize(2);

        FixtureDto firstFixture = result.getFixtures().get(0);
        assertThat(firstFixture.getFixture().getId()).isEqualTo(1L);
        assertThat(firstFixture.getFixture().getDate()).isEqualTo("2024-01-15T15:00:00+00:00");
        assertThat(firstFixture.getTeams().getHome().getName()).isEqualTo("Manchester United");
        assertThat(firstFixture.getTeams().getAway().getName()).isEqualTo("Chelsea");
        assertThat(firstFixture.getLeague().getName()).isEqualTo("Premier League");
    }

    @Test
    void mapToDto_ShouldHandleEmptyFixturesList() {
        FixtureResponse input = new FixtureResponse();
        input.setResults(0);
        input.setResponse(Collections.emptyList());

        FixtureResponseDto result = mapperService.mapToDto(input);

        assertThat(result).isNotNull();
        assertThat(result.getResults()).isEqualTo(0);
        assertThat(result.getFixtures()).isEmpty();
        assertThat(result.getStatus()).isEqualTo("success");
    }

    @Test
    void mapToDto_ShouldHandleNullFixturesList() {
        FixtureResponse input = new FixtureResponse();
        input.setResults(0);
        input.setResponse(null);

        FixtureResponseDto result = mapperService.mapToDto(input);

        assertThat(result).isNotNull();
        assertThat(result.getResults()).isEqualTo(0);
        assertThat(result.getFixtures()).isNull();
    }

    @Test
    void mapFixtureToDto_ShouldHandleMinimalFixture() {
        Fixture fixture = createMinimalFixture();

        FixtureDto result = mapperService.mapFixtureToDto(fixture);

        assertThat(result).isNotNull();
        assertThat(result.getFixture()).isNotNull();
        assertThat(result.getFixture().getId()).isEqualTo(123L);
        assertThat(result.getTeams()).isNotNull();
        assertThat(result.getTeams().getHome().getName()).isEqualTo("Team A");
    }

    @Test
    void mapFixtureToDto_ShouldHandleNullSubObjects() {
        Fixture fixture = new Fixture();

        FixtureDto result = mapperService.mapFixtureToDto(fixture);

        assertThat(result).isNotNull();
        assertThat(result.getFixture()).isNull();
        assertThat(result.getTeams()).isNull();
        assertThat(result.getLeague()).isNull();
        assertThat(result.getGoals()).isNull();
        assertThat(result.getScore()).isNull();
    }

    @Test
    void mapFixtureDetailsToDto_ShouldMapAllFields() {
        FixtureDetails details = createCompleteFixtureDetails(123L);

        FixtureDetailsDto result = mapperService.mapFixtureDetailsToDto(details);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getDate()).isEqualTo("2024-01-15T15:00:00+00:00");
        assertThat(result.getTimezone()).isEqualTo("UTC");
        assertThat(result.getVenue()).isNotNull();
        assertThat(result.getVenue().getName()).isEqualTo("Old Trafford");
        assertThat(result.getStatus()).isNotNull();
        assertThat(result.getStatus().getShortStatus()).isEqualTo("NS");
    }

    @Test
    void mapTeamsToDto_ShouldMapBothTeams() {
        Teams teams = createCompleteTeams();

        TeamsDto result = mapperService.mapTeamsToDto(teams);

        assertThat(result).isNotNull();
        assertThat(result.getHome()).isNotNull();
        assertThat(result.getHome().getName()).isEqualTo("Manchester United");
        assertThat(result.getHome().getLogo()).isEqualTo("https://example.com/mu-logo.png");
        assertThat(result.getAway()).isNotNull();
        assertThat(result.getAway().getName()).isEqualTo("Chelsea");
        assertThat(result.getAway().getLogo()).isEqualTo("https://example.com/chelsea-logo.png");
    }

    @Test
    void mapLeagueToDto_ShouldMapAllFields() {
        League league = createCompleteLeague();

        LeagueDto result = mapperService.mapLeagueToDto(league);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(39L);
        assertThat(result.getName()).isEqualTo("Premier League");
        assertThat(result.getCountry()).isEqualTo("England");
        assertThat(result.getLogo()).isEqualTo("https://example.com/pl-logo.png");
        assertThat(result.getSeason()).isEqualTo(2024);
        assertThat(result.getRound()).isEqualTo("Regular Season - 20");
    }

    @Test
    void mapGoalsToDto_ShouldMapBothScores() {
        Goals goals = new Goals();
        goals.setHome(2);
        goals.setAway(1);

        GoalsDto result = mapperService.mapGoalsToDto(goals);

        assertThat(result).isNotNull();
        assertThat(result.getHome()).isEqualTo(2);
        assertThat(result.getAway()).isEqualTo(1);
    }

    @Test
    void mapScoreToDto_ShouldMapAllPeriods() {
        Score score = createCompleteScore();

        ScoreDto result = mapperService.mapScoreToDto(score);

        assertThat(result).isNotNull();
        assertThat(result.getHalftime()).isNotNull();
        assertThat(result.getHalftime().getHome()).isEqualTo(1);
        assertThat(result.getHalftime().getAway()).isEqualTo(0);
        assertThat(result.getFulltime()).isNotNull();
        assertThat(result.getFulltime().getHome()).isEqualTo(2);
        assertThat(result.getFulltime().getAway()).isEqualTo(1);
    }

    private FixtureResponse createCompleteFixtureResponse() {
        FixtureResponse response = new FixtureResponse();
        response.setResults(2);
        response.setResponse(
                Arrays.asList(
                        createCompleteFixture(1L, "Manchester United", "Chelsea"),
                        createCompleteFixture(2L, "Arsenal", "Liverpool")));
        return response;
    }

    private Fixture createCompleteFixture(Long id, String homeTeam, String awayTeam) {
        Fixture fixture = new Fixture();
        fixture.setFixture(createCompleteFixtureDetails(id));
        fixture.setTeams(createTeamsWithNames(homeTeam, awayTeam));
        fixture.setLeague(createCompleteLeague());
        fixture.setGoals(createGoals(2, 1));
        fixture.setScore(createCompleteScore());
        return fixture;
    }

    private Fixture createMinimalFixture() {
        Fixture fixture = new Fixture();

        FixtureDetails details = new FixtureDetails();
        details.setId(123L);
        fixture.setFixture(details);

        Teams teams = new Teams();
        Team home = new Team();
        home.setName("Team A");
        teams.setHome(home);
        fixture.setTeams(teams);

        return fixture;
    }

    private FixtureDetails createCompleteFixtureDetails(Long id) {
        FixtureDetails details = new FixtureDetails();
        details.setId(id);
        details.setDate("2024-01-15T15:00:00+00:00");
        details.setTimezone("UTC");

        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("Old Trafford");
        venue.setCity("Manchester");
        details.setVenue(venue);

        Status status = new Status();
        status.setShortStatus("NS");
        status.setLongStatus("Not Started");
        details.setStatus(status);

        return details;
    }

    @Test
    void mapFixtureDetailsToDto_ShouldHandleNullVenue() {
        FixtureDetails details = new FixtureDetails();
        details.setId(123L);
        details.setDate("2024-01-15T15:00:00+00:00");
        details.setTimezone("UTC");

        FixtureDetailsDto result = mapperService.mapFixtureDetailsToDto(details);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getVenue()).isNull();
    }

    @Test
    void mapFixtureDetailsToDto_ShouldHandleNullStatus() {
        FixtureDetails details = new FixtureDetails();
        details.setId(123L);
        details.setDate("2024-01-15T15:00:00+00:00");
        details.setTimezone("UTC");

        FixtureDetailsDto result = mapperService.mapFixtureDetailsToDto(details);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getStatus()).isNull();
    }

    @Test
    void mapTeamsToDto_ShouldHandleNullHomeTeam() {
        Teams teams = new Teams();
        Team away = new Team();
        away.setId(34L);
        away.setName("Chelsea");
        away.setLogo("https://example.com/chelsea-logo.png");
        teams.setAway(away);

        TeamsDto result = mapperService.mapTeamsToDto(teams);

        assertThat(result).isNotNull();
        assertThat(result.getHome()).isNull();
        assertThat(result.getAway()).isNotNull();
        assertThat(result.getAway().getName()).isEqualTo("Chelsea");
    }

    @Test
    void mapTeamsToDto_ShouldHandleNullAwayTeam() {
        Teams teams = new Teams();
        Team home = new Team();
        home.setId(33L);
        home.setName("Manchester United");
        home.setLogo("https://example.com/mu-logo.png");
        teams.setHome(home);

        TeamsDto result = mapperService.mapTeamsToDto(teams);

        assertThat(result).isNotNull();
        assertThat(result.getHome()).isNotNull();
        assertThat(result.getHome().getName()).isEqualTo("Manchester United");
        assertThat(result.getAway()).isNull();
    }

    @Test
    void mapTeamsToDto_ShouldHandleBothTeamsNull() {
        Teams teams = new Teams();

        TeamsDto result = mapperService.mapTeamsToDto(teams);

        assertThat(result).isNotNull();
        assertThat(result.getHome()).isNull();
        assertThat(result.getAway()).isNull();
    }

    @Test
    void mapScoreToDto_ShouldHandleNullHalftime() {
        Score score = new Score();
        score.setFulltime(createGoals(2, 1));

        ScoreDto result = mapperService.mapScoreToDto(score);

        assertThat(result).isNotNull();
        assertThat(result.getHalftime()).isNull();
        assertThat(result.getFulltime()).isNotNull();
        assertThat(result.getFulltime().getHome()).isEqualTo(2);
    }

    @Test
    void mapScoreToDto_ShouldHandleNullFulltime() {
        Score score = new Score();
        score.setHalftime(createGoals(1, 0));

        ScoreDto result = mapperService.mapScoreToDto(score);

        assertThat(result).isNotNull();
        assertThat(result.getHalftime()).isNotNull();
        assertThat(result.getHalftime().getHome()).isEqualTo(1);
        assertThat(result.getFulltime()).isNull();
    }

    @Test
    void mapScoreToDto_ShouldHandleNullExtratime() {
        Score score = new Score();
        score.setHalftime(createGoals(1, 1));
        score.setFulltime(createGoals(1, 1));

        ScoreDto result = mapperService.mapScoreToDto(score);

        assertThat(result).isNotNull();
        assertThat(result.getExtratime()).isNull();
        assertThat(result.getPenalty()).isNull();
    }

    @Test
    void mapScoreToDto_ShouldHandleNullPenalty() {
        Score score = new Score();
        score.setHalftime(createGoals(1, 1));
        score.setFulltime(createGoals(1, 1));
        score.setExtratime(createGoals(0, 0));

        ScoreDto result = mapperService.mapScoreToDto(score);

        assertThat(result).isNotNull();
        assertThat(result.getExtratime()).isNotNull();
        assertThat(result.getPenalty()).isNull();
    }

    @Test
    void mapScoreToDto_ShouldHandleAllNullPeriods() {
        Score score = new Score();

        ScoreDto result = mapperService.mapScoreToDto(score);

        assertThat(result).isNotNull();
        assertThat(result.getHalftime()).isNull();
        assertThat(result.getFulltime()).isNull();
        assertThat(result.getExtratime()).isNull();
        assertThat(result.getPenalty()).isNull();
    }

    private Teams createCompleteTeams() {
        return createTeamsWithNames("Manchester United", "Chelsea");
    }

    private Teams createTeamsWithNames(String homeName, String awayName) {
        Teams teams = new Teams();

        Team home = new Team();
        home.setId(33L);
        home.setName(homeName);
        home.setLogo("https://example.com/mu-logo.png");
        teams.setHome(home);

        Team away = new Team();
        away.setId(34L);
        away.setName(awayName);
        away.setLogo("https://example.com/chelsea-logo.png");
        teams.setAway(away);

        return teams;
    }

    private League createCompleteLeague() {
        League league = new League();
        league.setId(39L);
        league.setName("Premier League");
        league.setCountry("England");
        league.setLogo("https://example.com/pl-logo.png");
        league.setSeason(2024);
        league.setRound("Regular Season - 20");
        return league;
    }

    private Goals createGoals(Integer home, Integer away) {
        Goals goals = new Goals();
        goals.setHome(home);
        goals.setAway(away);
        return goals;
    }

    private Score createCompleteScore() {
        Score score = new Score();
        score.setHalftime(createGoals(1, 0));
        score.setFulltime(createGoals(2, 1));
        score.setExtratime(createGoals(0, 0));
        score.setPenalty(createGoals(0, 0));
        return score;
    }
}
