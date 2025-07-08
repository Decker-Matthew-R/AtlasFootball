import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, vi } from 'vitest';

import { UserProvider } from '@/GlobalContext/UserContext/UserContext';
import LandingPage from '@/LandingPage/LandingPage';
import * as metricsClient from '@/metrics/client/MetricsClient';
import { METRIC_EVENT_TYPE } from '@/metrics/model/METRIC_EVENT_TYPE';
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

vi.mock('@/metrics/client/MetricsClient', () => ({
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
  it('should redirect to OAuth endpoint when login button is clicked and record a metric', async () => {
    renderApp();

    const loginButton = screen.getByRole('button', { name: /login|sign in/i });
    expect(loginButton).toBeInTheDocument();

    userEvent.click(loginButton);

    expect(mockSaveMetricEvent).toHaveBeenCalledTimes(1);
    expect(mockSaveMetricEvent).toHaveBeenCalledWith(METRIC_EVENT_TYPE.BUTTON_CLICK, {
      triggerId: 'Login',
      screen: '/',
    });

    expect(mockLocation.href).toBe('http://localhost:8080/oauth2/authorization/google');
  });

  it('should display login button when user is not authenticated', () => {
    renderApp();

    const loginButton = screen.getByLabelText('login-button');
    expect(loginButton).toBeVisible();
  });

  it('should NOT display login button when user is authenticated', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
      id: 1,
      email: 'test@example.com',
      name: 'Awatif Decker',
      firstName: 'Awatif',
      lastName: 'Decker',
      profilePicture: '',
    });

    renderApp();

    expect(screen.queryByRole('button', { name: 'Login' })).not.toBeInTheDocument();
  });
});
