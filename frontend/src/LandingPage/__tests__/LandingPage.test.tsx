import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, vi } from 'vitest';

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

  beforeEach(() => {
    mockLocation.href = '';
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(null);
    vi.clearAllMocks();
    vi.mocked(metricsClient.useMetrics).mockReturnValue({
      saveMetricEvent: mockSaveMetricEvent,
    });
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
    renderApp();

    const backgroundBox = screen.getByTestId('landing-page-container');

    expect(backgroundBox).toHaveStyle({
      'background-image': 'url(/src/assets/landingPageGenericBackground.avif)',
      'background-size': 'cover',
      'background-position': 'center',
      'background-repeat': 'no-repeat',
    });
  });
});
