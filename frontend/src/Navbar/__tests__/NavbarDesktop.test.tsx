import { render, screen } from '@testing-library/react';
import { describe, it } from 'vitest';
import Navbar from '../Navbar';
import userEvent from '@testing-library/user-event';

describe('Navbar', () => {
  const renderNavbar = () => render(<Navbar />);

  it('desktop: should contain a site logo and company name', () => {
    renderNavbar();

    const siteName = screen.getByLabelText('atlas-site-name');
    const siteLogo = screen.getByLabelText('atlas-logo');

    expect(siteName).toBeVisible();
    expect(siteLogo).toBeVisible();
  });

  it.each(['News', 'Matches'])(
    'desktop: should contain appropriate navigation buttons',
    (navigationButton) => {
      renderNavbar();

      const navigationLink = screen.getByRole('button', { name: navigationButton });
      expect(navigationLink).toBeVisible();
    },
  );

  it.each(['Profile', 'Logout'])(
    'desktop: should display profile icon, %s menu item when profile icon is clicked, and hide menu when user clicks away ',
    async (menuItem) => {
      renderNavbar();

      const profileIcon = screen.getByLabelText('Open Profile Settings');
      expect(profileIcon).toBeVisible();

      await userEvent.click(profileIcon);

      const profileMenu = screen.getByRole('menu');
      expect(profileMenu).toBeVisible();

      const menuOption = screen.getByRole('menuitem', { name: menuItem });
      expect(menuOption).toBeVisible();

      await userEvent.click(menuOption);

      expect(menuOption).not.toBeVisible();
    },
  );
});
