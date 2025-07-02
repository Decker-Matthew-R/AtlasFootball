import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, vi } from 'vitest';

import LandingPage from '@/LandingPage/LandingPage';

const mockLocation = {
  href: '',
};

Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true,
});

describe('Landing Page', () => {
  beforeEach(() => {
    // Reset location href before each test
    mockLocation.href = '';
    vi.clearAllMocks();
  });

  const renderApp = () => render(<LandingPage />);

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
});
