export interface FixtureResponseDto {
  results: number;
  fixtures: FixtureDto[];
  status: string;
  message: string;
}

export interface FixtureDto {
  fixture: FixtureDetailsDto;
  league: LeagueDto;
  teams: TeamsDto;
  goals: GoalsDto;
  score: ScoreDto;
}

export interface FixtureDetailsDto {
  id: number;
  date: string;
  timezone: string;
  venue: VenueDto;
  status: StatusDto;
}

export interface VenueDto {
  id: number;
  name: string;
  city: string;
}

export interface StatusDto {
  long: string;
  short: string;
  elapsed: number | null;
}

export interface LeagueDto {
  id: number;
  name: string;
  country: string;
  logo: string;
  season: number;
  round: string;
}

export interface TeamsDto {
  home: TeamDto;
  away: TeamDto;
}

export interface TeamDto {
  id: number;
  name: string;
  logo: string;
  winner: boolean | null;
}

export interface GoalsDto {
  home: number | null;
  away: number | null;
}

export interface ScoreDto {
  halftime: GoalsDto;
  fulltime: GoalsDto;
  extratime: GoalsDto | null;
  penalty: GoalsDto | null;
}

export interface FixturesByLeague {
  [leagueId: number]: {
    league: LeagueDto;
    fixtures: FixtureDto[];
  };
}
