import { render, screen } from '@testing-library/react';
import { describe, it } from 'vitest';
import Navbar from '../Navbar';
import userEvent from '@testing-library/user-event';

describe('Navbar', () => {
  const renderNavbar = () => render(<Navbar />);

  beforeEach(() => {
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
    renderNavbar();

    const siteName = screen.getByLabelText('atlas-site-name-mobile');
    const siteLogo = screen.getByLabelText('atlas-logo-mobile');

    expect(siteName).toBeVisible();
    expect(siteLogo).toBeVisible();
  });

  it.each(['News', 'Matches'])(
    'mobile: should display hamburger menu, %s menu item when hamburger menu is clicked, and hide menu when user clicks away ',
    async (menuItem) => {
      renderNavbar();

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
      renderNavbar();

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
