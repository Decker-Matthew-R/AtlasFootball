import { render, screen } from '@testing-library/react';

import {
  generateUpcomingFixture,
  generateKnownFixture,
} from '@/FixturesByLeague/__tests__/testHelpers/FixtureGenerator';
import { FixtureDto } from '@/FixturesByLeague/types/FixtureTypes';

import FixtureCard from '../FixtureCard';

const renderFixtureCard = (fixture: FixtureDto) => {
  return render(<FixtureCard fixture={fixture} />);
};

describe('FixtureCard', () => {
  it('renders fixture with correct team names', () => {
    const fixture = generateKnownFixture();

    renderFixtureCard(fixture);

    expect(screen.getByText('Liverpool')).toBeInTheDocument();
    expect(screen.getByText('Bournemouth')).toBeInTheDocument();
  });

  it('displays team logos with correct alt text', () => {
    const fixture = generateKnownFixture();

    renderFixtureCard(fixture);

    const homeTeamLogo = screen.getByAltText('Liverpool');
    const awayTeamLogo = screen.getByAltText('Bournemouth');

    expect(homeTeamLogo).toBeInTheDocument();
    expect(homeTeamLogo).toHaveAttribute(
      'src',
      'https://media.api-sports.io/football/teams/40.png',
    );
    expect(awayTeamLogo).toBeInTheDocument();
    expect(awayTeamLogo).toHaveAttribute(
      'src',
      'https://media.api-sports.io/football/teams/35.png',
    );
  });

  it('shows "VS" when goals are null', () => {
    const fixture = generateUpcomingFixture();

    renderFixtureCard(fixture);

    expect(screen.getByText('VS')).toBeInTheDocument();
  });

  it('displays formatted date and time', () => {
    process.env.TZ = 'UTC';
    const fixture = generateUpcomingFixture({
      customDate: '2025-08-15T19:00:00+00:00',
    });

    renderFixtureCard(fixture);

    expect(screen.getByText(/Aug 15/)).toBeInTheDocument();
    expect(screen.getByText(/7:00 PM/)).toBeInTheDocument();
  });

  it('displays venue information', () => {
    const fixture = generateKnownFixture();

    renderFixtureCard(fixture);

    expect(screen.getByText('Anfield, Liverpool')).toBeInTheDocument();
  });

  it('shows NS status chip with default styling', () => {
    const fixture = generateUpcomingFixture();

    renderFixtureCard(fixture);

    const statusChip = screen.getByText('NS');
    expect(statusChip).toBeInTheDocument();
  });

  it('does not show goal scores for upcoming matches', () => {
    const fixture = generateUpcomingFixture();

    renderFixtureCard(fixture);

    const goalElements = screen.queryAllByText(/^[0-9]$/);
    expect(goalElements).toHaveLength(0);
  });

  it('applies normal font weight to team names when no winner', () => {
    const fixture = generateUpcomingFixture();

    renderFixtureCard(fixture);

    const homeTeamText = screen.getByText(fixture.teams.home.name);
    const awayTeamText = screen.getByText(fixture.teams.away.name);

    expect(homeTeamText).toHaveStyle('font-weight: normal');
    expect(awayTeamText).toHaveStyle('font-weight: normal');
  });

  it('shows error color chip for live matches', () => {
    const liveFixture: FixtureDto = {
      ...generateUpcomingFixture(),
      fixture: {
        ...generateUpcomingFixture().fixture,
        status: {
          elapsed: 45,
          long: 'Match Live',
          short: 'LIVE',
        },
      },
      goals: {
        home: 1,
        away: 2,
      },
    };

    renderFixtureCard(liveFixture);

    const statusChip = screen.getByText('LIVE');
    expect(statusChip).toBeInTheDocument();
  });

  it('displays goal scores for live matches', () => {
    const liveFixture: FixtureDto = {
      ...generateUpcomingFixture(),
      fixture: {
        ...generateUpcomingFixture().fixture,
        status: {
          elapsed: 45,
          long: 'Match Live',
          short: 'LIVE',
        },
      },
      goals: {
        home: 2,
        away: 1,
      },
    };

    renderFixtureCard(liveFixture);

    expect(screen.getByText('2')).toBeInTheDocument();
    expect(screen.getByText('1')).toBeInTheDocument();
    expect(screen.queryByText('VS')).not.toBeInTheDocument();
  });

  it('shows success color chip for finished matches', () => {
    const finishedFixture: FixtureDto = {
      ...generateUpcomingFixture(),
      fixture: {
        ...generateUpcomingFixture().fixture,
        status: {
          elapsed: 90,
          long: 'Match Finished',
          short: 'FT',
        },
      },
      goals: {
        home: 3,
        away: 1,
      },
      teams: {
        home: {
          ...generateUpcomingFixture().teams.home,
          winner: true,
        },
        away: {
          ...generateUpcomingFixture().teams.away,
          winner: false,
        },
      },
    };

    renderFixtureCard(finishedFixture);

    const statusChip = screen.getByText('FT');
    expect(statusChip).toBeInTheDocument();
  });

  it('applies bold font weight to winning team', () => {
    const finishedFixture: FixtureDto = {
      ...generateUpcomingFixture(),
      fixture: {
        ...generateUpcomingFixture().fixture,
        status: {
          elapsed: 90,
          long: 'Match Finished',
          short: 'FT',
        },
      },
      goals: {
        home: 2,
        away: 0,
      },
      teams: {
        home: {
          ...generateUpcomingFixture().teams.home,
          winner: true,
        },
        away: {
          ...generateUpcomingFixture().teams.away,
          winner: false,
        },
      },
    };

    renderFixtureCard(finishedFixture);

    const homeTeamText = screen.getByText(finishedFixture.teams.home.name);
    const awayTeamText = screen.getByText(finishedFixture.teams.away.name);

    expect(homeTeamText).toHaveStyle('font-weight: 700');
    expect(awayTeamText).toHaveStyle('font-weight: normal');
  });

  it('applies bold font weight to away team when they win', () => {
    const awayWinFixture: FixtureDto = {
      ...generateUpcomingFixture(),
      fixture: {
        ...generateUpcomingFixture().fixture,
        status: {
          elapsed: 90,
          long: 'Match Finished',
          short: 'FT',
        },
      },
      goals: {
        home: 1,
        away: 3,
      },
      teams: {
        home: {
          ...generateUpcomingFixture().teams.home,
          winner: false,
        },
        away: {
          ...generateUpcomingFixture().teams.away,
          winner: true,
        },
      },
    };

    renderFixtureCard(awayWinFixture);

    const homeTeamText = screen.getByText(awayWinFixture.teams.home.name);
    const awayTeamText = screen.getByText(awayWinFixture.teams.away.name);

    expect(homeTeamText).toHaveStyle('font-weight: normal');
    expect(awayTeamText).toHaveStyle('font-weight: 700');
  });

  it('displays team logos with correct dimensions', () => {
    const fixture = generateUpcomingFixture();

    renderFixtureCard(fixture);

    const logos = screen.getAllByRole('img');
    logos.forEach((logo) => {
      expect(logo).toHaveStyle('width: 32px');
      expect(logo).toHaveStyle('height: 32px');
    });
  });

  describe('Date Formatting', () => {
    it('formats date correctly for different months', () => {
      const decemberFixture = generateUpcomingFixture({
        customDate: '2025-12-25T15:30:00+00:00',
      });

      renderFixtureCard(decemberFixture);

      expect(screen.getByText(/Dec 25/)).toBeInTheDocument();
      expect(screen.getByText(/3:30 PM/)).toBeInTheDocument();
    });

    it('formats time correctly for different hours', () => {
      const morningFixture = generateUpcomingFixture({
        customDate: '2025-08-15T09:00:00+00:00',
      });

      renderFixtureCard(morningFixture);

      expect(screen.getByText(/9:00 AM/)).toBeInTheDocument();
    });
  });

  it('provides proper alt text for team logos', () => {
    const fixture = generateUpcomingFixture({
      homeTeamId: 40,
      awayTeamId: 42,
    });

    renderFixtureCard(fixture);

    expect(screen.getByAltText('Liverpool')).toBeInTheDocument();
    expect(screen.getByAltText('Arsenal')).toBeInTheDocument();
  });

  it('handles teams with long names', () => {
    const longNameFixture = generateUpcomingFixture({
      homeTeamId: 47,
    });

    renderFixtureCard(longNameFixture);

    expect(screen.getByText('Tottenham')).toBeInTheDocument();
  });

  it('handles venues with long names', () => {
    const fixture = generateUpcomingFixture({
      venueId: 593,
    });

    renderFixtureCard(fixture);

    expect(screen.getByText(/Tottenham Hotspur Stadium/)).toBeInTheDocument();
  });
});
