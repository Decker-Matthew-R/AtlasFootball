import { render, screen } from '@testing-library/react';
import { describe, it, vi } from 'vitest';
import Navbar from '../Navbar';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';

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

    await userEvent.click(siteName);

    expect(mockNavigate).toHaveBeenCalledWith('/');
    expect(mockNavigate).toHaveBeenCalledTimes(1);
  });

  it('mobile: should navigate to home if Atlas Logo is clicked', async () => {
    renderMobileNavbar();

    const siteLogo = screen.getByLabelText('atlas-logo-mobile');

    await userEvent.click(siteLogo);

    expect(mockNavigate).toHaveBeenCalledWith('/');
    expect(mockNavigate).toHaveBeenCalledTimes(1);
  });

  it.each(['News', 'Matches'])(
    'mobile: should display hamburger menu, %s menu item when hamburger menu is clicked, and hide menu when user clicks away ',
    async (menuItem) => {
      renderMobileNavbar();

      const mobileHamburgerNavigationMenu = screen.getByLabelText('navigation-links');
      expect(mobileHamburgerNavigationMenu).toBeVisible();

      await userEvent.click(mobileHamburgerNavigationMenu);

      const profileMenu = screen.getByRole('menu');
      expect(profileMenu).toBeVisible();

      const menuOption1 = screen.getByRole('menuitem', { name: menuItem });
      expect(menuOption1).toBeVisible();

      await userEvent.click(menuOption1);

      expect(menuOption1).not.toBeVisible();
    },
  );

  it.each(['Profile', 'Logout'])(
    'mobile: should display profile icon, %s menu item when profile icon is clicked, and hide menu when user clicks away ',
    async (menuItem) => {
      renderMobileNavbar();

      const profileIconMobile = screen.getByLabelText('Open Profile Settings');
      expect(profileIconMobile).toBeVisible();

      await userEvent.click(profileIconMobile);

      const profileMenu = screen.getByRole('menu');
      expect(profileMenu).toBeVisible();

      const menuOption = screen.getByRole('menuitem', { name: menuItem });
      expect(menuOption).toBeVisible();

      await userEvent.click(menuOption);

      expect(menuOption).not.toBeVisible();
    },
  );
});
