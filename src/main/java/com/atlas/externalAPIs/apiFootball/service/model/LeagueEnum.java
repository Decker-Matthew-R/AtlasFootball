package com.atlas.externalAPIs.apiFootball.service.model;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum LeagueEnum {
    PREMIER_LEAGUE("39", "Premier League"),
    LA_LIGA("140", "La Liga"),
    BUNDESLIGA("78", "Bundesliga"),
    SERIE_A("135", "Serie A"),
    LIGUE_1("61", "Ligue 1");

    private final String id;
    private final String name;

    LeagueEnum(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static List<LeagueEnum> getTopFiveLeagues() {
        return Arrays.asList(values());
    }
}
