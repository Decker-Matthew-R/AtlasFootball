export interface VenueDto {
  id: number;
  name: string;
  city: string;
}

export interface TeamDto {
  id: number;
  name: string;
  logo: string;
  winner?: boolean;
}

export interface TeamsDto {
  home: TeamDto;
  away: TeamDto;
}

export interface LeagueDto {
  id: number;
  name: string;
  country: string;
  logo: string;
  season: number;
  round: string;
}

export interface GoalsDto {
  home: number;
  away: number;
}

export interface ScoreDto {
  halftime?: GoalsDto;
  fulltime?: GoalsDto;
  extratime?: GoalsDto;
  penalty?: GoalsDto;
}

export interface FixtureDetailsDto {
  id: number;
  date: string;
  timezone: string;
  venue?: VenueDto;
}

export interface FixtureDto {
  fixture: FixtureDetailsDto;
  teams: TeamsDto;
  league: LeagueDto;
  goals?: GoalsDto;
  score?: ScoreDto;
}

export interface FixtureResponseDto {
  status: string;
  message: string;
  results: number;
  fixtures: FixtureDto[];
}
