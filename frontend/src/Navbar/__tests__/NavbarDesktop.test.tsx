import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, vi } from 'vitest';

import { Navbar } from '@/Navbar/Navbar';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

describe('Navbar', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  });

  const renderDesktopNavbar = () => {
    render(
      <BrowserRouter>
        <Navbar />;
      </BrowserRouter>,
    );
  };

  it('desktop: should contain a site logo and company name', () => {
    renderDesktopNavbar();

    const siteName = screen.getByLabelText('atlas-site-name');
    const siteLogo = screen.getByLabelText('atlas-logo');

    expect(siteName).toBeVisible();
    expect(siteLogo).toBeVisible();
  });

  it('desktop: should navigate to home if Atlas text is clicked', async () => {
    renderDesktopNavbar();

    const siteName = screen.getByLabelText('atlas-site-name');

    userEvent.click(siteName);

    expect(mockNavigate).toHaveBeenCalledWith('/');
    expect(mockNavigate).toHaveBeenCalledTimes(1);
  });

  it('desktop: should navigate to home if Atlas Logo is clicked', async () => {
    renderDesktopNavbar();

    const siteLogo = screen.getByLabelText('atlas-logo');

    userEvent.click(siteLogo);

    expect(mockNavigate).toHaveBeenCalledWith('/');
    expect(mockNavigate).toHaveBeenCalledTimes(1);
  });

  it.each([
    ['News', '/'],
    ['Matches', '/'],
  ])(
    'desktop: should contain %s navigation buttons and navigate to %s when clicked',
    async (navigationButton, expectedRoute) => {
      renderDesktopNavbar();

      const navigationLink = screen.getByRole('button', { name: navigationButton });
      expect(navigationLink).toBeVisible();

      userEvent.click(navigationLink);

      expect(mockNavigate).toHaveBeenCalledWith(expectedRoute);
      expect(mockNavigate).toHaveBeenCalledTimes(1);
    },
  );

  it.each([
    ['Profile', '/'],
    ['Logout', '/'],
  ])(
    'desktop: should display profile icon, %s menu item when profile icon is clicked, the user navigates to %s and hide menu when user clicks away ',
    async (menuItem, expectedRoute) => {
      renderDesktopNavbar();

      const profileIcon = screen.getByLabelText('Open Profile Settings');
      expect(profileIcon).toBeVisible();

      userEvent.click(profileIcon);

      const profileMenu = screen.getByRole('menu');
      expect(profileMenu).toBeVisible();

      const menuOption = screen.getByRole('menuitem', { name: menuItem });
      expect(menuOption).toBeVisible();

      userEvent.click(menuOption);

      expect(mockNavigate).toHaveBeenCalledWith(expectedRoute);
      expect(mockNavigate).toHaveBeenCalledTimes(1);
      expect(menuOption).not.toBeVisible();
    },
  );
});
