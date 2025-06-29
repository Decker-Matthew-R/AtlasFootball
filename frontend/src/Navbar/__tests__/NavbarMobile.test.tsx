import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, vi } from 'vitest';

import * as metricsClient from '@/metrics/client/MetricsClient';
import { METRIC_EVENT_TYPE } from '@/metrics/model/METRIC_EVENT_TYPE';
import { MetricEventType } from '@/metrics/model/MetricEventType';
import { Navbar } from '@/Navbar/Navbar';

const mockNavigate = vi.fn();
const currentRoute = '/';

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

const mockMetricsClient = vi
  .spyOn(metricsClient, 'saveMetricEvent')
  .mockImplementation(() => Promise.resolve());

describe('Navbar', () => {
  const renderMobileNavbar = () => {
    render(
      <BrowserRouter>
        <Navbar />;
      </BrowserRouter>,
    );
  };

  beforeEach(() => {
    vi.clearAllMocks();
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

  it('mobile: should navigate to home if Atlas text is clicked and record a metric', async () => {
    const metricEvent: MetricEventType = {
      event: METRIC_EVENT_TYPE.BUTTON_CLICK,
      eventMetadata: {
        triggerId: 'Atlas Name',
        screen: '/',
      },
    };

    renderMobileNavbar();

    const siteName = screen.getByLabelText('atlas-site-name-mobile');

    userEvent.click(siteName);

    expect(mockMetricsClient).toHaveBeenCalledTimes(1);
    expect(mockMetricsClient).toHaveBeenCalledWith(metricEvent);

    expect(mockNavigate).toHaveBeenCalledWith('/');
    expect(mockNavigate).toHaveBeenCalledTimes(1);
  });

  it('mobile: should navigate to home if Atlas Logo is clicked', async () => {
    const metricEvent: MetricEventType = {
      event: METRIC_EVENT_TYPE.BUTTON_CLICK,
      eventMetadata: {
        triggerId: 'Atlas Logo',
        screen: '/',
      },
    };

    renderMobileNavbar();

    const siteLogo = screen.getByLabelText('atlas-logo-mobile');

    userEvent.click(siteLogo);

    expect(mockMetricsClient).toHaveBeenCalledTimes(1);
    expect(mockMetricsClient).toHaveBeenCalledWith(metricEvent);

    expect(mockNavigate).toHaveBeenCalledWith('/');
    expect(mockNavigate).toHaveBeenCalledTimes(1);
  });

  it.each([
    ['News', '/'],
    ['Matches', '/'],
  ])(
    'mobile: should display hamburger menu, %s menu item when hamburger menu is clicked, navigate to %s, and hide menu when user clicks away ',
    async (menuItem, expectedRoute) => {
      const metricEvent: MetricEventType = {
        event: METRIC_EVENT_TYPE.BUTTON_CLICK,
        eventMetadata: {
          triggerId: menuItem,
          screen: '/',
        },
      };

      renderMobileNavbar();

      const mobileHamburgerNavigationMenu = screen.getByLabelText('navigation-links');
      expect(mobileHamburgerNavigationMenu).toBeVisible();

      userEvent.click(mobileHamburgerNavigationMenu);

      const profileMenu = screen.getByRole('menu');
      expect(profileMenu).toBeVisible();

      const menuOption1 = screen.getByRole('menuitem', { name: menuItem });
      expect(menuOption1).toBeVisible();

      userEvent.click(menuOption1);

      expect(mockMetricsClient).toHaveBeenCalledTimes(1);
      expect(mockMetricsClient).toHaveBeenCalledWith(metricEvent);

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
      const metricEvent: MetricEventType = {
        event: METRIC_EVENT_TYPE.BUTTON_CLICK,
        eventMetadata: {
          triggerId: menuItem,
          screen: '/',
        },
      };

      renderMobileNavbar();

      const profileIconMobile = screen.getByLabelText('Open Profile Settings');
      expect(profileIconMobile).toBeVisible();

      userEvent.click(profileIconMobile);

      const profileMenu = screen.getByRole('menu');
      expect(profileMenu).toBeVisible();

      const menuOption = screen.getByRole('menuitem', { name: menuItem });
      expect(menuOption).toBeVisible();

      userEvent.click(menuOption);

      expect(mockMetricsClient).toHaveBeenCalledTimes(1);
      expect(mockMetricsClient).toHaveBeenCalledWith(metricEvent);

      expect(mockNavigate).toHaveBeenCalledWith(expectedRoute);
      expect(mockNavigate).toHaveBeenCalledTimes(1);
      expect(menuOption).not.toBeVisible();
    },
  );
});
