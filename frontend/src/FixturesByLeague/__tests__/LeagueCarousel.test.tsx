import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';

import { generateUpcomingFixtures } from '@/FixturesByLeague/__tests__/testHelpers/FixtureGenerator';
import { FixturesByLeague } from '@/FixturesByLeague/types/FixtureTypes';

import LeagueCarousel from '../LeagueCarousel';

process.env.TZ = 'UTC';

const renderLeagueCarousel = (leagueId: number, leagueData: FixturesByLeague[number]) => {
  return render(
    <LeagueCarousel
      leagueId={leagueId}
      leagueData={leagueData}
    />,
  );
};

const mockScrollBy = vi.fn();
Object.defineProperty(HTMLElement.prototype, 'scrollBy', {
  configurable: true,
  value: mockScrollBy,
});

Object.defineProperty(HTMLElement.prototype, 'scrollLeft', {
  configurable: true,
  get() {
    return this._scrollLeft || 0;
  },
  set(val) {
    this._scrollLeft = val;
  },
});

Object.defineProperty(HTMLElement.prototype, 'scrollWidth', {
  configurable: true,
  get() {
    return this._scrollWidth || 1000;
  },
});

Object.defineProperty(HTMLElement.prototype, 'clientWidth', {
  configurable: true,
  get() {
    return this._clientWidth || 500;
  },
});

const createMockLeagueData = (fixtureCount: number = 3): FixturesByLeague[number] => {
  const fixtures = generateUpcomingFixtures(fixtureCount);
  return {
    league: fixtures[0].league,
    fixtures: fixtures,
  };
};

describe('LeagueCarousel', () => {
  beforeEach(() => {
    mockScrollBy.mockClear();
  });

  it('renders league header with logo, name, and details', () => {
    const leagueData = createMockLeagueData(3);

    renderLeagueCarousel(39, leagueData);

    expect(screen.getByText(leagueData.league.name)).toBeInTheDocument();
    expect(screen.getByText(new RegExp(leagueData.league.country))).toBeInTheDocument();
    expect(screen.getByText(new RegExp(leagueData.league.season.toString()))).toBeInTheDocument();
    expect(screen.getByText(new RegExp(leagueData.league.round))).toBeInTheDocument();

    const leagueLogo = screen.getByAltText(leagueData.league.name);
    expect(leagueLogo).toBeInTheDocument();
    expect(leagueLogo).toHaveAttribute('src', leagueData.league.logo);
  });

  it('renders all fixture cards', () => {
    const leagueData = createMockLeagueData(5);

    renderLeagueCarousel(39, leagueData);

    const fixtureCards = screen.getAllByText('VS');
    expect(fixtureCards).toHaveLength(5);

    leagueData.fixtures.forEach((fixture) => {
      const homeTeamElements = screen.getAllByText(fixture.teams.home.name);
      const awayTeamElements = screen.getAllByText(fixture.teams.away.name);

      expect(homeTeamElements.length).toBeGreaterThan(0);
      expect(awayTeamElements.length).toBeGreaterThan(0);
    });
  });

  it('renders left and right scroll buttons', () => {
    const leagueData = createMockLeagueData(3);

    renderLeagueCarousel(39, leagueData);

    const leftButton = screen.getByRole('button', { name: 'Scroll left' });
    const rightButton = screen.getByRole('button', { name: 'Scroll right' });

    expect(leftButton).toBeInTheDocument();
    expect(rightButton).toBeInTheDocument();
  });

  it('disables left scroll button initially', () => {
    const leagueData = createMockLeagueData(3);

    renderLeagueCarousel(39, leagueData);

    const leftButton = screen.getByRole('button', { name: 'Scroll left' });
    expect(leftButton).toBeDisabled();
  });

  it('calls scrollBy when right button is clicked', () => {
    const leagueData = createMockLeagueData(5);

    renderLeagueCarousel(39, leagueData);

    const rightButton = screen.getByRole('button', { name: 'Scroll right' });
    fireEvent.click(rightButton);

    expect(mockScrollBy).toHaveBeenCalledWith({
      left: 300,
      behavior: 'smooth',
    });
  });

  it('displays league logo with correct styling', () => {
    const leagueData = createMockLeagueData(3);

    renderLeagueCarousel(39, leagueData);

    const leagueLogo = screen.getByAltText(leagueData.league.name);
    expect(leagueLogo).toHaveStyle('max-width: 90%');
    expect(leagueLogo).toHaveStyle('max-height: 90%');
    expect(leagueLogo).toHaveStyle('object-fit: cover');
  });

  it('handles empty fixtures array', () => {
    const leagueData: FixturesByLeague[number] = {
      league: {
        id: 39,
        name: 'Premier League',
        country: 'England',
        logo: 'https://media.api-sports.io/football/leagues/39.png',
        season: 2025,
        round: 'Regular Season - 1',
      },
      fixtures: [],
    };

    renderLeagueCarousel(39, leagueData);

    expect(screen.getByText('Premier League')).toBeInTheDocument();

    expect(screen.getByRole('button', { name: 'Scroll left' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Scroll right' })).toBeInTheDocument();

    expect(screen.queryByText('VS')).not.toBeInTheDocument();

    const leftButton = screen.getByRole('button', { name: 'Scroll left' });
    const rightButton = screen.getByRole('button', { name: 'Scroll right' });

    expect(leftButton).toBeDisabled();
    expect(rightButton).toBeEnabled();
  });

  it('applies correct styling to scroll buttons', () => {
    const leagueData = createMockLeagueData(3);

    renderLeagueCarousel(39, leagueData);

    const leftButton = screen.getByRole('button', { name: 'Scroll left' });
    const rightButton = screen.getByRole('button', { name: 'Scroll right' });

    expect(leftButton).toHaveStyle('position: absolute');
    expect(rightButton).toHaveStyle('position: absolute');
  });

  it('renders league header typography with correct variants', () => {
    const leagueData = createMockLeagueData(3);

    renderLeagueCarousel(39, leagueData);

    const leagueName = screen.getByRole('heading', { level: 2 });
    expect(leagueName).toHaveTextContent(leagueData.league.name);
  });

  it('handles scroll buttons with proper event listeners', () => {
    const leagueData = createMockLeagueData(5);

    const { unmount } = renderLeagueCarousel(39, leagueData);

    expect(screen.getByText(leagueData.league.name)).toBeInTheDocument();

    expect(() => unmount()).not.toThrow();
  });
});
