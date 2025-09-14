import { FixtureDto } from '@/FixturesByLeague/types/FixtureTypes';

interface TeamData {
  id: number;
  name: string;
  logo: string;
}

interface LeagueData {
  id: number;
  name: string;
  country: string;
  logo: string;
  season: number;
  round: string;
}

interface VenueData {
  id: number;
  name: string;
  city: string;
}

const SAMPLE_TEAMS: TeamData[] = [
  { id: 40, name: 'Liverpool', logo: 'https://media.api-sports.io/football/teams/40.png' },
  { id: 35, name: 'Bournemouth', logo: 'https://media.api-sports.io/football/teams/35.png' },
  { id: 66, name: 'Aston Villa', logo: 'https://media.api-sports.io/football/teams/66.png' },
  { id: 34, name: 'Newcastle', logo: 'https://media.api-sports.io/football/teams/34.png' },
  { id: 51, name: 'Brighton', logo: 'https://media.api-sports.io/football/teams/51.png' },
  { id: 36, name: 'Fulham', logo: 'https://media.api-sports.io/football/teams/36.png' },
  { id: 746, name: 'Sunderland', logo: 'https://media.api-sports.io/football/teams/746.png' },
  { id: 48, name: 'West Ham', logo: 'https://media.api-sports.io/football/teams/48.png' },
  { id: 47, name: 'Tottenham', logo: 'https://media.api-sports.io/football/teams/47.png' },
  { id: 44, name: 'Burnley', logo: 'https://media.api-sports.io/football/teams/44.png' },
  { id: 39, name: 'Wolves', logo: 'https://media.api-sports.io/football/teams/39.png' },
  { id: 50, name: 'Manchester City', logo: 'https://media.api-sports.io/football/teams/50.png' },
  { id: 65, name: 'Nottingham Forest', logo: 'https://media.api-sports.io/football/teams/65.png' },
  { id: 55, name: 'Brentford', logo: 'https://media.api-sports.io/football/teams/55.png' },
  { id: 49, name: 'Chelsea', logo: 'https://media.api-sports.io/football/teams/49.png' },
  { id: 52, name: 'Crystal Palace', logo: 'https://media.api-sports.io/football/teams/52.png' },
  { id: 33, name: 'Manchester United', logo: 'https://media.api-sports.io/football/teams/33.png' },
  { id: 42, name: 'Arsenal', logo: 'https://media.api-sports.io/football/teams/42.png' },
  { id: 63, name: 'Leeds', logo: 'https://media.api-sports.io/football/teams/63.png' },
  { id: 45, name: 'Everton', logo: 'https://media.api-sports.io/football/teams/45.png' },
];

const SAMPLE_LEAGUES: LeagueData[] = [
  {
    id: 39,
    name: 'Premier League',
    country: 'England',
    logo: 'https://media.api-sports.io/football/leagues/39.png',
    season: 2025,
    round: 'Regular Season - 1',
  },
  {
    id: 140,
    name: 'La Liga',
    country: 'Spain',
    logo: 'https://media.api-sports.io/football/leagues/140.png',
    season: 2025,
    round: 'Regular Season - 1',
  },
  {
    id: 78,
    name: 'Bundesliga',
    country: 'Germany',
    logo: 'https://media.api-sports.io/football/leagues/78.png',
    season: 2025,
    round: 'Regular Season - 1',
  },
];

const SAMPLE_VENUES: VenueData[] = [
  { id: 550, name: 'Anfield', city: 'Liverpool' },
  { id: 495, name: 'Villa Park', city: 'Birmingham' },
  { id: 508, name: 'American Express Stadium', city: 'Falmer, East Sussex' },
  { id: 589, name: 'Stadium of Light', city: 'Sunderland' },
  { id: 593, name: 'Tottenham Hotspur Stadium', city: 'London' },
  { id: 600, name: 'Molineux Stadium', city: 'Wolverhampton, West Midlands' },
  { id: 566, name: 'The City Ground', city: 'Nottingham, Nottinghamshire' },
  { id: 519, name: 'Stamford Bridge', city: 'London' },
  { id: 556, name: 'Old Trafford', city: 'Manchester' },
  { id: 546, name: 'Elland Road', city: 'Leeds, West Yorkshire' },
];

interface FixtureGeneratorOptions {
  homeTeamId?: number;
  awayTeamId?: number;
  leagueId?: number;
  venueId?: number;
  customDate?: string;
  fixtureId?: number;
  round?: string;
  deterministic?: boolean;
}

function getRandomElement<T>(array: T[]): T {
  return array[Math.floor(Math.random() * array.length)];
}

function generateFutureDate(daysFromNow: number = 7): string {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString();
}

export function generateUpcomingFixture(options: FixtureGeneratorOptions = {}): FixtureDto {
  const { homeTeamId, awayTeamId, leagueId, venueId, customDate, fixtureId, round } = options;

  let homeTeam: TeamData, awayTeam: TeamData;

  if (homeTeamId && awayTeamId) {
    homeTeam = SAMPLE_TEAMS.find((t) => t.id === homeTeamId) || SAMPLE_TEAMS[0];
    awayTeam = SAMPLE_TEAMS.find((t) => t.id === awayTeamId) || SAMPLE_TEAMS[1];
  } else if (homeTeamId) {
    homeTeam = SAMPLE_TEAMS.find((t) => t.id === homeTeamId) || SAMPLE_TEAMS[0];
    awayTeam = getRandomElement(SAMPLE_TEAMS.filter((t) => t.id !== homeTeam.id));
  } else if (awayTeamId) {
    awayTeam = SAMPLE_TEAMS.find((t) => t.id === awayTeamId) || SAMPLE_TEAMS[1];
    homeTeam = getRandomElement(SAMPLE_TEAMS.filter((t) => t.id !== awayTeam.id));
  } else {
    homeTeam = getRandomElement(SAMPLE_TEAMS);
    awayTeam = getRandomElement(SAMPLE_TEAMS.filter((t) => t.id !== homeTeam.id));
  }

  const league = leagueId
    ? SAMPLE_LEAGUES.find((l) => l.id === leagueId) || SAMPLE_LEAGUES[0]
    : getRandomElement(SAMPLE_LEAGUES);

  const venue = venueId
    ? SAMPLE_VENUES.find((v) => v.id === venueId) || SAMPLE_VENUES[0]
    : getRandomElement(SAMPLE_VENUES);

  const generatedFixtureId = fixtureId || Math.floor(Math.random() * 9000000) + 1000000;
  const matchDate = customDate || generateFutureDate(Math.floor(Math.random() * 30) + 1);

  return {
    fixture: {
      id: generatedFixtureId,
      date: matchDate,
      timezone: 'UTC',
      venue: {
        id: venue.id,
        name: venue.name,
        city: venue.city,
      },
      status: {
        elapsed: null,
        long: 'Not Started',
        short: 'NS',
      },
    },
    league: {
      id: league.id,
      name: league.name,
      country: league.country,
      logo: league.logo,
      season: league.season,
      round: round || league.round,
    },
    teams: {
      home: {
        id: homeTeam.id,
        name: homeTeam.name,
        logo: homeTeam.logo,
        winner: null,
      },
      away: {
        id: awayTeam.id,
        name: awayTeam.name,
        logo: awayTeam.logo,
        winner: null,
      },
    },
    goals: {
      home: null,
      away: null,
    },
    score: {
      halftime: {
        home: null,
        away: null,
      },
      fulltime: {
        home: null,
        away: null,
      },
      extratime: {
        home: null,
        away: null,
      },
      penalty: {
        home: null,
        away: null,
      },
    },
  };
}

export function generateUpcomingFixtures(
  count: number,
  options: FixtureGeneratorOptions = {},
): FixtureDto[] {
  const fixtures: FixtureDto[] = [];
  const usedTeams = new Set<number>();

  const availableTeams = [...SAMPLE_TEAMS];

  if (!options.deterministic) {
    shuffleArray(availableTeams);
  }

  for (let i = 0; i < count; i++) {
    const fixtureOptions = {
      ...options,
      fixtureId: options.fixtureId ? options.fixtureId + i : undefined,
      customDate: options.customDate || generateFutureDate(i + 1),
    };

    const fixture = generateUniqueFixture(fixtureOptions, usedTeams, availableTeams);
    fixtures.push(fixture);
  }

  return fixtures;
}

export function generateDeterministicFixtures(
  count: number,
  options: FixtureGeneratorOptions = {},
): FixtureDto[] {
  return generateUpcomingFixtures(count, { ...options, deterministic: true });
}

function shuffleArray<T>(array: T[]): void {
  for (let i = array.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [array[i], array[j]] = [array[j], array[i]];
  }
}

function generateUniqueFixture(
  options: FixtureGeneratorOptions,
  usedTeams: Set<number>,
  availableTeams: TeamData[],
): FixtureDto {
  const { homeTeamId, awayTeamId, leagueId, venueId, customDate, fixtureId, round } = options;

  let homeTeam: TeamData, awayTeam: TeamData;

  if (homeTeamId && awayTeamId) {
    homeTeam = SAMPLE_TEAMS.find((t) => t.id === homeTeamId) || SAMPLE_TEAMS[0];
    awayTeam = SAMPLE_TEAMS.find((t) => t.id === awayTeamId) || SAMPLE_TEAMS[1];
  } else if (homeTeamId) {
    homeTeam = SAMPLE_TEAMS.find((t) => t.id === homeTeamId) || SAMPLE_TEAMS[0];
    awayTeam = getNextAvailableTeam(availableTeams, usedTeams, [homeTeam.id]);
  } else if (awayTeamId) {
    awayTeam = SAMPLE_TEAMS.find((t) => t.id === awayTeamId) || SAMPLE_TEAMS[1];
    homeTeam = getNextAvailableTeam(availableTeams, usedTeams, [awayTeam.id]);
  } else {
    homeTeam = getNextAvailableTeam(availableTeams, usedTeams, []);
    awayTeam = getNextAvailableTeam(availableTeams, usedTeams, [homeTeam.id]);
  }

  usedTeams.add(homeTeam.id);
  usedTeams.add(awayTeam.id);

  const league = leagueId
    ? SAMPLE_LEAGUES.find((l) => l.id === leagueId) || SAMPLE_LEAGUES[0]
    : getRandomElement(SAMPLE_LEAGUES);

  const venue = venueId
    ? SAMPLE_VENUES.find((v) => v.id === venueId) || SAMPLE_VENUES[0]
    : getRandomElement(SAMPLE_VENUES);

  const generatedFixtureId = fixtureId || Math.floor(Math.random() * 9000000) + 1000000;
  const matchDate = customDate || generateFutureDate(Math.floor(Math.random() * 30) + 1);

  return {
    fixture: {
      id: generatedFixtureId,
      date: matchDate,
      timezone: 'UTC',
      venue: {
        id: venue.id,
        name: venue.name,
        city: venue.city,
      },
      status: {
        elapsed: null,
        long: 'Not Started',
        short: 'NS',
      },
    },
    league: {
      id: league.id,
      name: league.name,
      country: league.country,
      logo: league.logo,
      season: league.season,
      round: round || league.round,
    },
    teams: {
      home: {
        id: homeTeam.id,
        name: homeTeam.name,
        logo: homeTeam.logo,
        winner: null,
      },
      away: {
        id: awayTeam.id,
        name: awayTeam.name,
        logo: awayTeam.logo,
        winner: null,
      },
    },
    goals: {
      home: null,
      away: null,
    },
    score: {
      halftime: {
        home: null,
        away: null,
      },
      fulltime: {
        home: null,
        away: null,
      },
      extratime: {
        home: null,
        away: null,
      },
      penalty: {
        home: null,
        away: null,
      },
    },
  };
}

function getNextAvailableTeam(
  availableTeams: TeamData[],
  usedTeams: Set<number>,
  excludeIds: number[],
): TeamData {
  // Find first team that hasn't been used and isn't in exclude list
  const team = availableTeams.find((t) => !usedTeams.has(t.id) && !excludeIds.includes(t.id));

  // If no available team found, reset and start over (shouldn't happen with 20 teams)
  if (!team) {
    console.warn('No available teams found, resetting used teams set');
    usedTeams.clear();
    return availableTeams.find((t) => !excludeIds.includes(t.id)) || availableTeams[0];
  }

  return team;
}

/**
 * Generates a specific fixture based on real data from your API response
 */
export function generateKnownFixture(): FixtureDto {
  return {
    fixture: {
      id: 1378969,
      date: '2025-08-15T19:00:00+00:00',
      timezone: 'UTC',
      venue: {
        id: 550,
        name: 'Anfield',
        city: 'Liverpool',
      },
      status: {
        elapsed: null,
        long: 'Not Started',
        short: 'NS',
      },
    },
    league: {
      id: 39,
      name: 'Premier League',
      country: 'England',
      logo: 'https://media.api-sports.io/football/leagues/39.png',
      season: 2025,
      round: 'Regular Season - 1',
    },
    teams: {
      home: {
        id: 40,
        name: 'Liverpool',
        logo: 'https://media.api-sports.io/football/teams/40.png',
        winner: null,
      },
      away: {
        id: 35,
        name: 'Bournemouth',
        logo: 'https://media.api-sports.io/football/teams/35.png',
        winner: null,
      },
    },
    goals: {
      home: null,
      away: null,
    },
    score: {
      halftime: {
        home: null,
        away: null,
      },
      fulltime: {
        home: null,
        away: null,
      },
      extratime: {
        home: null,
        away: null,
      },
      penalty: {
        home: null,
        away: null,
      },
    },
  };
}
