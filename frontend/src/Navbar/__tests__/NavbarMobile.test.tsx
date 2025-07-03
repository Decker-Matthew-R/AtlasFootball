import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, vi } from 'vitest';

import { UserProvider } from '@/GlobalContext/UserContext/UserContext';
import * as metricsClient from '@/metrics/client/MetricsClient';
import { METRIC_EVENT_TYPE } from '@/metrics/model/METRIC_EVENT_TYPE';
import { MetricEventType } from '@/metrics/model/MetricEventType';
import { Navbar } from '@/Navbar/Navbar';
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
      <UserProvider>
        <BrowserRouter>
          <Navbar />
        </BrowserRouter>
      </UserProvider>,
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
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(null);
    vi.clearAllMocks();
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

  it.each([['Profile', '/']])(
    'desktop: should display profile icon, %s menu item when profile icon is clicked, the user navigates to %s and hide menu when user clicks away ',
    async (menuItem, expectedRoute) => {
      const metricEvent: MetricEventType = {
        event: METRIC_EVENT_TYPE.BUTTON_CLICK,
        eventMetadata: {
          triggerId: menuItem,
          screen: '/',
        },
      };

      vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
        id: 1,
        email: 'test@example.com',
        name: 'Jane Smith',
        firstName: 'Jane',
        lastName: 'Smith',
        profilePicture: null,
      });

      renderMobileNavbar();

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

  it('desktop: should display Logout Button in profile menu items', () => {
    const metricEvent: MetricEventType = {
      event: METRIC_EVENT_TYPE.BUTTON_CLICK,
      eventMetadata: {
        triggerId: 'Logout',
        screen: '/',
      },
    };

    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
      id: 1,
      email: 'test@example.com',
      name: 'Jane Smith',
      firstName: 'Jane',
      lastName: 'Smith',
      profilePicture: null,
    });

    renderMobileNavbar();

    const profileIcon = screen.getByLabelText('Open Profile Settings');
    expect(profileIcon).toBeVisible();

    userEvent.click(profileIcon);

    const profileMenu = screen.getByRole('menu');
    expect(profileMenu).toBeVisible();

    const menuOption = screen.getByRole('menuitem', { name: 'Logout' });
    expect(menuOption).toBeVisible();

    userEvent.click(menuOption);

    expect(mockMetricsClient).toHaveBeenCalledTimes(1);
    expect(mockMetricsClient).toHaveBeenCalledWith(metricEvent);

    expect(menuOption).not.toBeVisible();
  });
  it('should show profile picture when user has valid profilePicture URL', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
      id: 1,
      email: 'test@example.com',
      name: 'John Doe',
      firstName: 'John',
      lastName: 'Doe',
      profilePicture: 'https://example.com/avatar.jpg',
    });

    renderMobileNavbar();

    const avatar = screen.getByRole('img');

    expect(avatar).toHaveAttribute('src', 'https://example.com/avatar.jpg');
    expect(avatar).toHaveAttribute('alt', 'J');
  });

  it('should show first letter of name when user has no profile picture', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
      id: 1,
      email: 'test@example.com',
      name: 'Awatif Decker',
      firstName: 'Awatif',
      lastName: 'Decker',
      profilePicture: '',
    });

    renderMobileNavbar();

    const avatar = screen.getByText('A');

    expect(avatar).toHaveTextContent('A');

    const profileButton = screen.getByRole('button', { name: 'Open Profile Settings' });
    expect(profileButton).toBeInTheDocument();
  });

  it('should show first letter when user has null profile picture', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
      id: 1,
      email: 'test@example.com',
      name: 'Jane Smith',
      firstName: 'Jane',
      lastName: 'Smith',
      profilePicture: null,
    });

    renderMobileNavbar();

    const avatar = screen.getByText('J');

    expect(avatar).toHaveTextContent('J');

    const profileButton = screen.getByRole('button', { name: 'Open Profile Settings' });
    expect(profileButton).toBeInTheDocument();
  });

  it('should show person icon when user has no name', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
      id: 1,
      email: 'test@example.com',
      name: '',
      firstName: '',
      lastName: '',
      profilePicture: '',
    });

    renderMobileNavbar();

    const personIcon = screen.getByTestId('PersonIcon');
    expect(personIcon).toBeInTheDocument();

    const profileButton = screen.getByRole('button', { name: 'Open Profile Settings' });
    expect(profileButton).toBeInTheDocument();
  });

  it('should show person icon when user is not logged in', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(null);

    renderMobileNavbar();

    const personIcon = screen.getByTestId('PersonIcon');
    expect(personIcon).toBeInTheDocument();

    const profileButton = screen.getByLabelText('Open Profile Settings');
    expect(profileButton).toBeInTheDocument();
  });

  it('should display login button in the profile options menu when user is not logged on and should redirect to oauth endpoint on click', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(null);

    renderMobileNavbar();

    const profileIconMobile = screen.getByLabelText('Open Profile Settings');
    expect(profileIconMobile).toBeVisible();

    userEvent.click(profileIconMobile);

    const profileMenu = screen.getByRole('menu');
    expect(profileMenu).toBeVisible();

    const menuLoginButton = screen.getByRole('button', { name: 'Login' });
    expect(menuLoginButton).toBeVisible();

    userEvent.click(menuLoginButton);

    expect(mockLocation.href).toBe('http://localhost:8080/oauth2/authorization/google');
  });
});
