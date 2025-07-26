import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { beforeEach, describe, it, vi } from 'vitest';

import * as fixturesClient from '@/FixturesByLeague/client/FixturesClient';
import * as fixtureHelpers from '@/FixturesByLeague/helper/fixtureHelpers';
import { UserProvider } from '@/GlobalContext/UserContext/UserContext';
import LandingPage from '@/LandingPage/LandingPage';
import * as metricsClient from '@/Metrics/client/MetricsClient';
import * as cookieUtils from '@/utils/CookieUtils';

const mockNavigate = vi.fn();
const currentRoute = '/';

vi.mock('@/utils/CookieUtils');

const mockLocation = {
  href: '',
};

Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true,
});

vi.mock('@/Metrics/client/MetricsClient', () => ({
  useMetrics: vi.fn(() => ({
    saveMetricEvent: vi.fn(),
  })),
}));

vi.mock('@/FixturesByLeague/client/FixturesClient', () => ({
  useFixtures: vi.fn(),
}));

vi.mock('@/FixturesByLeague/helper/fixtureHelpers', () => ({
  groupFixturesByLeague: vi.fn(),
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
    useLocation: vi.fn().mockImplementation(() => {
      return { pathname: currentRoute };
    }),
  };
});

describe('Landing Page', () => {
  const mockSaveMetricEvent = vi.fn();
  const mockFetchFixtures = vi.fn();
  const mockGroupFixturesByLeague = vi.fn();

  beforeEach(() => {
    mockLocation.href = '';
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(null);
    vi.clearAllMocks();
    vi.mocked(metricsClient.useMetrics).mockReturnValue({
      saveMetricEvent: mockSaveMetricEvent,
    });

    vi.mocked(fixturesClient.useFixtures).mockReturnValue({
      fetchFixtures: mockFetchFixtures,
    });

    vi.mocked(fixtureHelpers.groupFixturesByLeague).mockImplementation(mockGroupFixturesByLeague);
  });

  const renderApp = () => {
    render(
      <UserProvider>
        <BrowserRouter>
          <LandingPage />
        </BrowserRouter>
      </UserProvider>,
    );
  };

  it('Should contain a background image', async () => {
    mockFetchFixtures.mockResolvedValue([]);
    mockGroupFixturesByLeague.mockReturnValue({});

    renderApp();

    const backgroundBox = screen.getByTestId('landing-page-container');

    expect(backgroundBox).toHaveStyle({
      'background-image': 'url(/src/assets/landingPageGenericBackground.avif)',
      'background-size': 'cover',
      'background-position': 'center',
      'background-repeat': 'no-repeat',
    });
  });

  it('Should successfully load fixtures and call grouping helper', async () => {
    const mockFixtures = [
      { id: 1, leagueId: 1, homeTeam: 'Team A', awayTeam: 'Team B' },
      { id: 2, leagueId: 1, homeTeam: 'Team C', awayTeam: 'Team D' },
      { id: 3, leagueId: 2, homeTeam: 'Team E', awayTeam: 'Team F' },
    ];

    mockFetchFixtures.mockResolvedValue(mockFixtures);
    mockGroupFixturesByLeague.mockReturnValue({});

    renderApp();

    await waitFor(() => {
      expect(mockFetchFixtures).toHaveBeenCalledTimes(1);
    });

    expect(mockGroupFixturesByLeague).toHaveBeenCalledWith(mockFixtures);
  });

  it('Should handle error when loading fixtures fails', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    const mockError = new Error('Failed to fetch fixtures');

    mockFetchFixtures.mockRejectedValue(mockError);
    mockGroupFixturesByLeague.mockReturnValue({});

    renderApp();

    await waitFor(() => {
      expect(mockFetchFixtures).toHaveBeenCalledTimes(1);
    });

    expect(consoleSpy).toHaveBeenCalledWith('Failed to load fixtures:', mockError);
    expect(mockGroupFixturesByLeague).toHaveBeenCalledWith([]);

    consoleSpy.mockRestore();
  });

  it('Should display "No fixtures available" message when there are no fixtures', async () => {
    mockFetchFixtures.mockResolvedValue([]);
    mockGroupFixturesByLeague.mockReturnValue({});

    renderApp();

    await waitFor(() => {
      expect(mockFetchFixtures).toHaveBeenCalledTimes(1);
    });

    expect(screen.getByText('No fixtures available at the moment.')).toBeInTheDocument();
  });

  it('Should display "No fixtures available" message when fixtures load but grouping results in empty object', async () => {
    const mockFixtures = [{ id: 1, leagueId: null, homeTeam: 'Team A', awayTeam: 'Team B' }];

    mockFetchFixtures.mockResolvedValue(mockFixtures);
    mockGroupFixturesByLeague.mockReturnValue({}); // Empty object - no valid leagues

    renderApp();

    await waitFor(() => {
      expect(mockFetchFixtures).toHaveBeenCalledTimes(1);
    });

    expect(mockGroupFixturesByLeague).toHaveBeenCalledWith(mockFixtures);
    expect(screen.getByText('No fixtures available at the moment.')).toBeInTheDocument();
  });
});
