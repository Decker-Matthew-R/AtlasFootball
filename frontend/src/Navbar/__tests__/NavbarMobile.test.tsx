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
  const renderMobileNavbar = () => {
    render(
      <BrowserRouter>
        <Navbar />;
      </BrowserRouter>,
    );
  };

  beforeEach(() => {
    mockNavigate.mockClear();
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 375,
    });
    Object.defineProperty(window, 'innerHeight', {
      writable: true,
      configurable: true,
      value: 812,
    });

    window.dispatchEvent(new Event('resize'));
  });

  it('mobile: should contain a site logo and company name  ', () => {
    renderMobileNavbar();

    const siteName = screen.getByLabelText('atlas-site-name-mobile');
    const siteLogo = screen.getByLabelText('atlas-logo-mobile');

    expect(siteName).toBeVisible();
    expect(siteLogo).toBeVisible();
  });

  it('mobile: should navigate to home if Atlas text is clicked', async () => {
    renderMobileNavbar();

    const siteName = screen.getByLabelText('atlas-site-name-mobile');

    userEvent.click(siteName);

    expect(mockNavigate).toHaveBeenCalledWith('/');
    expect(mockNavigate).toHaveBeenCalledTimes(1);
  });

  it('mobile: should navigate to home if Atlas Logo is clicked', async () => {
    renderMobileNavbar();

    const siteLogo = screen.getByLabelText('atlas-logo-mobile');

    userEvent.click(siteLogo);

    expect(mockNavigate).toHaveBeenCalledWith('/');
    expect(mockNavigate).toHaveBeenCalledTimes(1);
  });

  it.each([
    ['News', '/'],
    ['Matches', '/'],
  ])(
    'mobile: should display hamburger menu, %s menu item when hamburger menu is clicked, navigate to %s, and hide menu when user clicks away ',
    async (menuItem, expectedRoute) => {
      renderMobileNavbar();

      const mobileHamburgerNavigationMenu = screen.getByLabelText('navigation-links');
      expect(mobileHamburgerNavigationMenu).toBeVisible();

      userEvent.click(mobileHamburgerNavigationMenu);

      const profileMenu = screen.getByRole('menu');
      expect(profileMenu).toBeVisible();

      const menuOption1 = screen.getByRole('menuitem', { name: menuItem });
      expect(menuOption1).toBeVisible();

      userEvent.click(menuOption1);

      expect(mockNavigate).toHaveBeenCalledWith(expectedRoute);
      expect(mockNavigate).toHaveBeenCalledTimes(1);
      expect(menuOption1).not.toBeVisible();
    },
  );

  it.each([
    ['Profile', '/'],
    ['Logout', '/'],
  ])(
    'mobile: should display profile icon, %s menu item when profile icon is clicked, navigate to %s, and hide menu when user clicks away ',
    async (menuItem, expectedRoute) => {
      renderMobileNavbar();

      const profileIconMobile = screen.getByLabelText('Open Profile Settings');
      expect(profileIconMobile).toBeVisible();

      userEvent.click(profileIconMobile);

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
