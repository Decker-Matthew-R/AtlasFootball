import { FixtureDto, FixturesByLeague } from '@/FixturesByLeague/types/FixtureTypes';

export const groupFixturesByLeague = (fixtures: FixtureDto[]): FixturesByLeague => {
  return fixtures.reduce((acc: FixturesByLeague, fixture) => {
    const leagueId = fixture.league.id;

    if (!acc[leagueId]) {
      acc[leagueId] = {
        league: fixture.league,
        fixtures: [],
      };
    }

    acc[leagueId].fixtures.push(fixture);
    return acc;
  }, {});
};
