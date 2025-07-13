package com.atlas.externalAPIs.apiFootball.service;

import com.atlas.externalAPIs.apiFootball.controller.model.FixtureDto;
import com.atlas.externalAPIs.apiFootball.controller.model.supportingTypes.*;
import com.atlas.externalAPIs.apiFootball.service.model.Fixture;
import com.atlas.externalAPIs.apiFootball.service.model.response.FixtureResponse;
import com.atlas.externalAPIs.apiFootball.service.model.supportingTypes.*;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FixtureMapperService {

    public FixtureResponseDto mapToDto(FixtureResponse response) {
        if (response == null) {
            return createEmptyResponse();
        }

        FixtureResponseDto dto = new FixtureResponseDto();
        dto.setResults(response.getResults());
        dto.setStatus("success");
        dto.setMessage("Fixtures retrieved successfully");

        if (response.getResponse() != null) {
            dto.setFixtures(
                    response.getResponse().stream()
                            .map(this::mapFixtureToDto)
                            .collect(Collectors.toList()));
        }

        return dto;
    }

    FixtureDto mapFixtureToDto(Fixture fixture) {
        FixtureDto dto = new FixtureDto();

        if (fixture.getFixture() != null) {
            dto.setFixture(mapFixtureDetailsToDto(fixture.getFixture()));
        }

        if (fixture.getTeams() != null) {
            dto.setTeams(mapTeamsToDto(fixture.getTeams()));
        }

        if (fixture.getLeague() != null) {
            dto.setLeague(mapLeagueToDto(fixture.getLeague()));
        }

        if (fixture.getGoals() != null) {
            dto.setGoals(mapGoalsToDto(fixture.getGoals()));
        }

        if (fixture.getScore() != null) {
            dto.setScore(mapScoreToDto(fixture.getScore()));
        }

        return dto;
    }

    FixtureDetailsDto mapFixtureDetailsToDto(FixtureDetails details) {
        FixtureDetailsDto dto = new FixtureDetailsDto();
        dto.setId(details.getId());
        dto.setDate(details.getDate());
        dto.setTimezone(details.getTimezone());

        if (details.getVenue() != null) {
            VenueDto venueDto = new VenueDto();
            venueDto.setId(details.getVenue().getId());
            venueDto.setName(details.getVenue().getName());
            venueDto.setCity(details.getVenue().getCity());
            dto.setVenue(venueDto);
        }

        if (details.getStatus() != null) {
            StatusDto statusDto = new StatusDto();
            statusDto.setShortStatus(details.getStatus().getShortStatus());
            statusDto.setLongStatus(details.getStatus().getLongStatus());
            dto.setStatus(statusDto);
        }

        return dto;
    }

    TeamsDto mapTeamsToDto(Teams teams) {
        TeamsDto dto = new TeamsDto();

        if (teams.getHome() != null) {
            TeamDto homeDto = new TeamDto();
            homeDto.setId(teams.getHome().getId());
            homeDto.setName(teams.getHome().getName());
            homeDto.setLogo(teams.getHome().getLogo());
            homeDto.setWinner(teams.getHome().getWinner());
            dto.setHome(homeDto);
        }

        if (teams.getAway() != null) {
            TeamDto awayDto = new TeamDto();
            awayDto.setId(teams.getAway().getId());
            awayDto.setName(teams.getAway().getName());
            awayDto.setLogo(teams.getAway().getLogo());
            awayDto.setWinner(teams.getAway().getWinner());
            dto.setAway(awayDto);
        }

        return dto;
    }

    LeagueDto mapLeagueToDto(League league) {
        LeagueDto dto = new LeagueDto();
        dto.setId(league.getId());
        dto.setName(league.getName());
        dto.setCountry(league.getCountry());
        dto.setLogo(league.getLogo());
        dto.setSeason(league.getSeason());
        dto.setRound(league.getRound());
        return dto;
    }

    GoalsDto mapGoalsToDto(Goals goals) {
        GoalsDto dto = new GoalsDto();
        dto.setHome(goals.getHome());
        dto.setAway(goals.getAway());
        return dto;
    }

    ScoreDto mapScoreToDto(Score score) {
        ScoreDto dto = new ScoreDto();

        if (score.getHalftime() != null) {
            dto.setHalftime(mapGoalsToDto(score.getHalftime()));
        }
        if (score.getFulltime() != null) {
            dto.setFulltime(mapGoalsToDto(score.getFulltime()));
        }
        if (score.getExtratime() != null) {
            dto.setExtratime(mapGoalsToDto(score.getExtratime()));
        }
        if (score.getPenalty() != null) {
            dto.setPenalty(mapGoalsToDto(score.getPenalty()));
        }

        return dto;
    }

    private FixtureResponseDto createEmptyResponse() {
        FixtureResponseDto dto = new FixtureResponseDto();
        dto.setResults(0);
        dto.setFixtures(Collections.emptyList());
        dto.setStatus("success");
        dto.setMessage("No fixtures found");
        return dto;
    }
}
