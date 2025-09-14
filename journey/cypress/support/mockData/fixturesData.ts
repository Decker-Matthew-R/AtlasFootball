// cypress/support/mockData/fixturesData.ts

export const mockFixturesResponse = {
  results: 16,
  status: "success",
  message: "Fixtures retrieved successfully",
  fixtures: [
    {
      fixture: {
        id: 1,
        date: '2024-01-15T15:00:00Z',
        timezone: 'UTC',
        venue: { id: 494, name: 'Emirates Stadium', city: 'London' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 39,
        name: 'Premier League',
        country: 'England',
        logo: 'https://media.api-sports.io/football/leagues/39.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 42, name: 'Arsenal', logo: 'https://media.api-sports.io/football/teams/42.png', winner: null },
        away: { id: 50, name: 'Manchester City', logo: 'https://media.api-sports.io/football/teams/50.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 2,
        date: '2024-01-15T17:30:00Z',
        timezone: 'UTC',
        venue: { id: 555, name: 'Anfield', city: 'Liverpool' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 39,
        name: 'Premier League',
        country: 'England',
        logo: 'https://media.api-sports.io/football/leagues/39.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 40, name: 'Liverpool', logo: 'https://media.api-sports.io/football/teams/40.png', winner: null },
        away: { id: 49, name: 'Chelsea', logo: 'https://media.api-sports.io/football/teams/49.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 3,
        date: '2024-01-15T20:00:00Z',
        timezone: 'UTC',
        venue: { id: 547, name: 'Old Trafford', city: 'Manchester' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 39,
        name: 'Premier League',
        country: 'England',
        logo: 'https://media.api-sports.io/football/leagues/39.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 33, name: 'Manchester United', logo: 'https://media.api-sports.io/football/teams/33.png', winner: null },
        away: { id: 47, name: 'Tottenham', logo: 'https://media.api-sports.io/football/teams/47.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 4,
        date: '2024-01-16T15:00:00Z',
        timezone: 'UTC',
        venue: { id: 508, name: 'Stamford Bridge', city: 'London' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 39,
        name: 'Premier League',
        country: 'England',
        logo: 'https://media.api-sports.io/football/leagues/39.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 49, name: 'Chelsea', logo: 'https://media.api-sports.io/football/teams/49.png', winner: null },
        away: { id: 66, name: 'Aston Villa', logo: 'https://media.api-sports.io/football/teams/66.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 5,
        date: '2024-01-16T17:30:00Z',
        timezone: 'UTC',
        venue: { id: 556, name: 'Etihad Stadium', city: 'Manchester' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 39,
        name: 'Premier League',
        country: 'England',
        logo: 'https://media.api-sports.io/football/leagues/39.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 50, name: 'Manchester City', logo: 'https://media.api-sports.io/football/teams/50.png', winner: null },
        away: { id: 35, name: 'Bournemouth', logo: 'https://media.api-sports.io/football/teams/35.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 6,
        date: '2024-01-17T15:00:00Z',
        timezone: 'UTC',
        venue: { id: 525, name: 'Goodison Park', city: 'Liverpool' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 39,
        name: 'Premier League',
        country: 'England',
        logo: 'https://media.api-sports.io/football/leagues/39.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 45, name: 'Everton', logo: 'https://media.api-sports.io/football/teams/45.png', winner: null },
        away: { id: 34, name: 'Newcastle', logo: 'https://media.api-sports.io/football/teams/34.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 7,
        date: '2024-01-17T17:30:00Z',
        timezone: 'UTC',
        venue: { id: 600, name: 'London Stadium', city: 'London' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 39,
        name: 'Premier League',
        country: 'England',
        logo: 'https://media.api-sports.io/football/leagues/39.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 48, name: 'West Ham', logo: 'https://media.api-sports.io/football/teams/48.png', winner: null },
        away: { id: 39, name: 'Wolves', logo: 'https://media.api-sports.io/football/teams/39.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 8,
        date: '2024-01-18T20:00:00Z',
        timezone: 'UTC',
        venue: { id: 580, name: 'Falmer Stadium', city: 'Brighton' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 39,
        name: 'Premier League',
        country: 'England',
        logo: 'https://media.api-sports.io/football/leagues/39.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 51, name: 'Brighton', logo: 'https://media.api-sports.io/football/teams/51.png', winner: null },
        away: { id: 42, name: 'Arsenal', logo: 'https://media.api-sports.io/football/teams/42.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },

    {
      fixture: {
        id: 9,
        date: '2024-01-15T20:00:00Z',
        timezone: 'UTC',
        venue: { id: 508, name: 'Camp Nou', city: 'Barcelona' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 140,
        name: 'La Liga',
        country: 'Spain',
        logo: 'https://media.api-sports.io/football/leagues/140.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 529, name: 'Barcelona', logo: 'https://media.api-sports.io/football/teams/529.png', winner: null },
        away: { id: 541, name: 'Real Madrid', logo: 'https://media.api-sports.io/football/teams/541.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 10,
        date: '2024-01-16T18:00:00Z',
        timezone: 'UTC',
        venue: { id: 738, name: 'Civitas Metropolitano', city: 'Madrid' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 140,
        name: 'La Liga',
        country: 'Spain',
        logo: 'https://media.api-sports.io/football/leagues/140.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 530, name: 'Atletico Madrid', logo: 'https://media.api-sports.io/football/teams/530.png', winner: null },
        away: { id: 536, name: 'Sevilla', logo: 'https://media.api-sports.io/football/teams/536.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 11,
        date: '2024-01-16T20:30:00Z',
        timezone: 'UTC',
        venue: { id: 756, name: 'Mestalla', city: 'Valencia' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 140,
        name: 'La Liga',
        country: 'Spain',
        logo: 'https://media.api-sports.io/football/leagues/140.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 532, name: 'Valencia', logo: 'https://media.api-sports.io/football/teams/532.png', winner: null },
        away: { id: 548, name: 'Real Sociedad', logo: 'https://media.api-sports.io/football/teams/548.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 12,
        date: '2024-01-17T16:00:00Z',
        timezone: 'UTC',
        venue: { id: 1496, name: 'San Mames', city: 'Bilbao' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 140,
        name: 'La Liga',
        country: 'Spain',
        logo: 'https://media.api-sports.io/football/leagues/140.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 531, name: 'Athletic Bilbao', logo: 'https://media.api-sports.io/football/teams/531.png', winner: null },
        away: { id: 533, name: 'Villarreal', logo: 'https://media.api-sports.io/football/teams/533.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 13,
        date: '2024-01-17T18:30:00Z',
        timezone: 'UTC',
        venue: { id: 1456, name: 'Estadio Benito Villamarin', city: 'Sevilla' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 140,
        name: 'La Liga',
        country: 'Spain',
        logo: 'https://media.api-sports.io/football/leagues/140.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 545, name: 'Real Betis', logo: 'https://media.api-sports.io/football/teams/545.png', winner: null },
        away: { id: 546, name: 'Getafe', logo: 'https://media.api-sports.io/football/teams/546.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 14,
        date: '2024-01-18T19:00:00Z',
        timezone: 'UTC',
        venue: { id: 1440, name: 'Estadio de la Ceramica', city: 'Villarreal' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 140,
        name: 'La Liga',
        country: 'Spain',
        logo: 'https://media.api-sports.io/football/leagues/140.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 533, name: 'Villarreal', logo: 'https://media.api-sports.io/football/teams/533.png', winner: null },
        away: { id: 715, name: 'Granada', logo: 'https://media.api-sports.io/football/teams/715.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 15,
        date: '2024-01-18T21:00:00Z',
        timezone: 'UTC',
        venue: { id: 1505, name: 'Santiago Bernabeu', city: 'Madrid' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 140,
        name: 'La Liga',
        country: 'Spain',
        logo: 'https://media.api-sports.io/football/leagues/140.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 541, name: 'Real Madrid', logo: 'https://media.api-sports.io/football/teams/541.png', winner: null },
        away: { id: 797, name: 'Las Palmas', logo: 'https://media.api-sports.io/football/teams/797.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    },
    {
      fixture: {
        id: 16,
        date: '2024-01-19T20:00:00Z',
        timezone: 'UTC',
        venue: { id: 1491, name: 'RCDE Stadium', city: 'Barcelona' },
        status: { long: 'Not Started', short: 'NS', elapsed: null }
      },
      league: {
        id: 140,
        name: 'La Liga',
        country: 'Spain',
        logo: 'https://media.api-sports.io/football/leagues/140.png',
        season: 2024,
        round: 'Regular Season - 20'
      },
      teams: {
        home: { id: 540, name: 'Espanyol', logo: 'https://media.api-sports.io/football/teams/540.png', winner: null },
        away: { id: 529, name: 'Barcelona', logo: 'https://media.api-sports.io/football/teams/529.png', winner: null }
      },
      goals: { home: null, away: null },
      score: { halftime: { home: null, away: null }, fulltime: { home: null, away: null }, extratime: null, penalty: null }
    }
  ]
};

export const emptyFixturesResponse = {
  results: 0,
  status: "success",
  message: "No fixtures found",
  fixtures: []
};

export const errorResponse = {
  error: 'Internal server error'
};
