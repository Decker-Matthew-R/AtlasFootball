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
  beforeEach(() => {
    vi.clearAllMocks();
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

  it('desktop: should navigate to home if Atlas text is clicked and record a metric', async () => {
    const metricEvent: MetricEventType = {
      event: METRIC_EVENT_TYPE.BUTTON_CLICK,
      eventMetadata: {
        triggerId: 'Atlas Name',
        screen: '/',
      },
    };

    renderDesktopNavbar();

    const siteName = screen.getByLabelText('atlas-site-name');

    userEvent.click(siteName);

    expect(mockMetricsClient).toHaveBeenCalledTimes(1);
    expect(mockMetricsClient).toHaveBeenCalledWith(metricEvent);

    expect(mockNavigate).toHaveBeenCalledWith('/');
    expect(mockNavigate).toHaveBeenCalledTimes(1);
  });

  it('desktop: should navigate to home if Atlas Logo is clicked and record a metric', async () => {
    const metricEvent: MetricEventType = {
      event: METRIC_EVENT_TYPE.BUTTON_CLICK,
      eventMetadata: {
        triggerId: 'Atlas Logo',
        screen: '/',
      },
    };

    renderDesktopNavbar();

    const siteLogo = screen.getByLabelText('atlas-logo');

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
    'desktop: should contain %s navigation buttons, navigate to %s, and record a metric when clicked',
    async (navigationButton, expectedRoute) => {
      const metricEvent: MetricEventType = {
        event: METRIC_EVENT_TYPE.BUTTON_CLICK,
        eventMetadata: {
          triggerId: navigationButton,
          screen: '/',
        },
      };

      renderDesktopNavbar();

      const navigationLink = screen.getByRole('button', { name: navigationButton });
      expect(navigationLink).toBeVisible();

      userEvent.click(navigationLink);

      expect(mockMetricsClient).toHaveBeenCalledTimes(1);
      expect(mockMetricsClient).toHaveBeenCalledWith(metricEvent);

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
      const metricEvent: MetricEventType = {
        event: METRIC_EVENT_TYPE.BUTTON_CLICK,
        eventMetadata: {
          triggerId: menuItem,
          screen: '/',
        },
      };

      renderDesktopNavbar();

      const profileIcon = screen.getByLabelText('Open Profile Settings');
      expect(profileIcon).toBeVisible();

      userEvent.click(profileIcon);

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
