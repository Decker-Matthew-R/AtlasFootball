import { describe, it, expect } from 'vitest';

import { groupFixturesByLeague } from '@/FixturesByLeague/helper/fixtureHelpers';
import { FixtureDto, FixturesByLeague } from '@/FixturesByLeague/types/FixtureTypes';

describe('groupFixturesByLeague', () => {
  const createMockFixture = (
    fixtureId: number,
    leagueId: number,
    leagueName: string,
    homeTeam: string,
    awayTeam: string,
  ): FixtureDto => ({
    fixture: {
      id: fixtureId,
      date: '2024-01-01T15:00:00Z',
      timezone: 'UTC',
      venue: { id: 1, name: 'Stadium', city: 'City' },
      status: { long: 'Not Started', short: 'NS', elapsed: 0 },
    },
    league: {
      id: leagueId,
      name: leagueName,
      country: 'England',
      logo: `logo-${leagueId}`,
      season: 2024,
      round: 'Regular Season - 1',
    },
    teams: {
      home: { id: 1, name: homeTeam, logo: 'logo-home', winner: null },
      away: { id: 2, name: awayTeam, logo: 'logo-away', winner: null },
    },
    goals: { home: null, away: null },
    score: {
      halftime: { home: null, away: null },
      fulltime: { home: null, away: null },
      extratime: null,
      penalty: null,
    },
  });

  it('should return empty object for empty fixtures array', () => {
    const result = groupFixturesByLeague([]);

    expect(result).toEqual({});
  });

  it('should group single fixture correctly', () => {
    const fixture = createMockFixture(1, 100, 'Premier League', 'Team A', 'Team B');

    const result = groupFixturesByLeague([fixture]);

    const expected: FixturesByLeague = {
      100: {
        league: fixture.league,
        fixtures: [fixture],
      },
    };
    expect(result).toEqual(expected);
  });

  it('should group multiple fixtures from same league', () => {
    const fixture1 = createMockFixture(1, 100, 'Premier League', 'Team A', 'Team B');
    const fixture2 = createMockFixture(2, 100, 'Premier League', 'Team C', 'Team D');
    const fixture3 = createMockFixture(3, 100, 'Premier League', 'Team E', 'Team F');

    const result = groupFixturesByLeague([fixture1, fixture2, fixture3]);

    expect(result).toHaveProperty('100');
    expect(result[100].league).toEqual(fixture1.league);
    expect(result[100].fixtures).toHaveLength(3);
    expect(result[100].fixtures).toEqual([fixture1, fixture2, fixture3]);
  });

  it('should group fixtures from different leagues separately', () => {
    const premierLeagueFixture = createMockFixture(1, 100, 'Premier League', 'Arsenal', 'Chelsea');
    const laLigaFixture = createMockFixture(2, 200, 'La Liga', 'Barcelona', 'Real Madrid');
    const bundesligaFixture = createMockFixture(3, 300, 'Bundesliga', 'Bayern', 'Dortmund');

    const result = groupFixturesByLeague([premierLeagueFixture, laLigaFixture, bundesligaFixture]);

    expect(Object.keys(result)).toHaveLength(3);
    expect(result).toHaveProperty('100');
    expect(result).toHaveProperty('200');
    expect(result).toHaveProperty('300');

    expect(result[100].league.name).toBe('Premier League');
    expect(result[100].fixtures).toEqual([premierLeagueFixture]);

    expect(result[200].league.name).toBe('La Liga');
    expect(result[200].fixtures).toEqual([laLigaFixture]);

    expect(result[300].league.name).toBe('Bundesliga');
    expect(result[300].fixtures).toEqual([bundesligaFixture]);
  });

  it('should group mixed fixtures correctly with multiple leagues and multiple fixtures per league', () => {
    const plFixture1 = createMockFixture(1, 100, 'Premier League', 'Arsenal', 'Chelsea');
    const plFixture2 = createMockFixture(2, 100, 'Premier League', 'Liverpool', 'City');
    const laLigaFixture1 = createMockFixture(3, 200, 'La Liga', 'Barcelona', 'Real Madrid');
    const laLigaFixture2 = createMockFixture(4, 200, 'La Liga', 'Atletico', 'Sevilla');
    const bundesligaFixture = createMockFixture(5, 300, 'Bundesliga', 'Bayern', 'Dortmund');

    const fixtures = [plFixture1, laLigaFixture1, plFixture2, bundesligaFixture, laLigaFixture2];

    const result = groupFixturesByLeague(fixtures);

    expect(Object.keys(result)).toHaveLength(3);

    expect(result[100].fixtures).toHaveLength(2);
    expect(result[100].fixtures).toEqual([plFixture1, plFixture2]);

    expect(result[200].fixtures).toHaveLength(2);
    expect(result[200].fixtures).toEqual([laLigaFixture1, laLigaFixture2]);

    expect(result[300].fixtures).toHaveLength(1);
    expect(result[300].fixtures).toEqual([bundesligaFixture]);
  });

  it('should preserve fixture order within each league group', () => {
    const fixture1 = createMockFixture(1, 100, 'Premier League', 'First', 'Match');
    const fixture2 = createMockFixture(2, 100, 'Premier League', 'Second', 'Match');
    const fixture3 = createMockFixture(3, 100, 'Premier League', 'Third', 'Match');

    const result = groupFixturesByLeague([fixture1, fixture2, fixture3]);

    expect(result[100].fixtures[0]).toEqual(fixture1);
    expect(result[100].fixtures[1]).toEqual(fixture2);
    expect(result[100].fixtures[2]).toEqual(fixture3);
  });

  it('should use league data from first fixture encountered for each league', () => {
    const fixture1 = createMockFixture(1, 100, 'Premier League', 'Team A', 'Team B');
    const fixture2 = createMockFixture(2, 100, 'Different Name', 'Team C', 'Team D');

    const result = groupFixturesByLeague([fixture1, fixture2]);

    expect(result[100].league.name).toBe('Premier League');
    expect(result[100].fixtures).toHaveLength(2);
  });
});
