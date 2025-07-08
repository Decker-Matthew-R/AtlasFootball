import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { describe, it, vi } from 'vitest';

import { UserProvider } from '@/GlobalContext/UserContext/UserContext';
import ProfilePage from '@/ProfilePage/ProfilePage';
import * as cookieUtils from '@/utils/CookieUtils';

vi.mock('@/utils/CookieUtils');

describe('Profile Page', () => {
  const renderProfilePage = () => {
    render(
      <UserProvider>
        <BrowserRouter>
          <ProfilePage />
        </BrowserRouter>
        ,
      </UserProvider>,
    );
  };
  it('should show profile page with users full name', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
      id: 1,
      email: 'test@example.com',
      name: 'John Doe',
      firstName: 'John',
      lastName: 'Doe',
      profilePicture: 'https://example.com/avatar.jpg',
    });

    renderProfilePage();

    const userName = screen.getByText('John Doe');
    expect(userName).toBeVisible();
  });

  it('should show profile page with when user has valid profilePicture URL', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
      id: 1,
      email: 'test@example.com',
      name: 'John Doe',
      firstName: 'John',
      lastName: 'Doe',
      profilePicture: 'https://example.com/avatar.jpg',
    });

    renderProfilePage();

    const avatar = screen.getByRole('img');

    expect(avatar).toHaveAttribute('src', 'https://example.com/avatar.jpg');
    expect(avatar).toHaveAttribute('alt', 'J');
  });

  it('should profile page with letter fallback if no profile picture is present', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
      id: 1,
      email: 'test@example.com',
      name: 'Jane Smith',
      firstName: 'Jane',
      lastName: 'Smith',
      profilePicture: null,
    });

    renderProfilePage();
    const avatar = screen.getByText('J');
    expect(avatar).toHaveTextContent('J');
  });

  it('should profile page with person icon when user has no name', () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue({
      id: 1,
      email: 'test@example.com',
      name: '',
      firstName: '',
      lastName: '',
      profilePicture: '',
    });

    renderProfilePage();

    const personIcon = screen.getByTestId('PersonIcon');
    expect(personIcon).toBeInTheDocument();

    const profileAvatar = screen.getByTestId('PersonIcon');
    expect(profileAvatar).toBeInTheDocument();
  });
});
