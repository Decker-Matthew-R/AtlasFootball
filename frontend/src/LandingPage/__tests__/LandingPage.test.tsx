import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, vi } from 'vitest';

import { UserProvider } from '@/GlobalContext/UserContext/UserContext';
import LandingPage from '@/LandingPage/LandingPage';
import * as cookieUtils from '@/utils/CookieUtils';

vi.mock('@/utils/CookieUtils');

const mockLocation = {
  href: '',
};

Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true,
});

describe('Landing Page', () => {
  beforeEach(() => {
    mockLocation.href = '';
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(null);
    vi.clearAllMocks();
  });

  const renderApp = () =>
    render(
      <UserProvider>
        <LandingPage />
      </UserProvider>,
    );

  it('Should contain a background image', async () => {
    renderApp();

    const backgroundBox = screen.getByTestId('landing-page-container');

    expect(backgroundBox).toHaveStyle({
      'background-image': 'url(/src/assets/landingPageGenericBackground.jpg)',
      'background-size': 'cover',
      'background-position': 'center',
      'background-repeat': 'no-repeat',
    });
  });
  it('should redirect to OAuth endpoint when login button is clicked', async () => {
    renderApp();

    const loginButton = screen.getByRole('button', { name: /login|sign in/i });
    expect(loginButton).toBeInTheDocument();

    userEvent.click(loginButton);

    expect(mockLocation.href).toBe('http://localhost:8080/oauth2/authorization/google');
  });

  it('should display login button when user is not authenticated', () => {
    renderApp();

    const loginButton = screen.getByRole('button', { name: 'Login' });
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
