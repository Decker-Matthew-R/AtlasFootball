import React from 'react';

import { act, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { User } from '@/GlobalContext/UserContext/types/user';
import { UserProvider, useUser } from '@/GlobalContext/UserContext/UserContext';
import * as cookieUtils from '@/utils/CookieUtils';

vi.mock('@/utils/CookieUtils');

const mockLocation = {
  href: '',
};

Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true,
});

const TestComponent: React.FC = () => {
  const { user, isAuthenticated, isLoading, error, login, logout, updateUser, clearError } =
    useUser();

  return (
    <div>
      <div data-testid='user-data'>{user ? JSON.stringify(user) : 'null'}</div>
      <div data-testid='is-authenticated'>{isAuthenticated.toString()}</div>
      <div data-testid='is-loading'>{isLoading.toString()}</div>
      <div data-testid='error'>{error || 'null'}</div>
      <button
        data-testid='login-btn'
        onClick={login}
      >
        Login
      </button>
      <button
        data-testid='logout-btn'
        onClick={logout}
      >
        Logout
      </button>
      <button
        data-testid='update-btn'
        onClick={() => updateUser({ name: 'Updated Name' })}
      >
        Update User
      </button>
      <button
        data-testid='clear-error-btn'
        onClick={clearError}
      >
        Clear Error
      </button>
    </div>
  );
};

const renderWithProvider = () => {
  return render(
    <UserProvider>
      <TestComponent />
    </UserProvider>,
  );
};

describe('UserContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockLocation.href = '';

    vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should initialize with user data when valid cookie exists', async () => {
    const userData: User = {
      id: 123,
      email: 'test@example.com',
      name: 'Test User',
      firstName: 'Test',
      lastName: 'User',
      profilePicture: 'https://example.com/avatar.jpg',
    };

    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(userData);

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-loading')).toHaveTextContent('false');
    });

    expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true');
    expect(screen.getByTestId('user-data')).toHaveTextContent(JSON.stringify(userData));
    expect(screen.getByTestId('error')).toHaveTextContent('null');
  });

  it('should handle initialization error', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    vi.mocked(cookieUtils.parseUserInfoCookie).mockImplementation(() => {
      throw new Error('Cookie parsing failed');
    });

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('error')).toHaveTextContent('Failed to load user data');
    });

    expect(screen.getByTestId('is-loading')).toHaveTextContent('false');
    expect(screen.getByTestId('is-authenticated')).toHaveTextContent('false');
    expect(consoleSpy).toHaveBeenCalledWith('Error initializing user:', expect.any(Error));
  });

  it('should set loading false when no user data found', async () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(null);

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-loading')).toHaveTextContent('false');
    });

    expect(screen.getByTestId('is-authenticated')).toHaveTextContent('false');
    expect(screen.getByTestId('user-data')).toHaveTextContent('null');
    expect(screen.getByTestId('error')).toHaveTextContent('null');
  });

  it('should login successfully with valid user data', async () => {
    const userData: User = {
      id: 123,
      email: 'test@example.com',
      name: 'Test User',
      firstName: 'Test',
      lastName: 'User',
      profilePicture: null,
    };

    vi.mocked(cookieUtils.parseUserInfoCookie)
      .mockReturnValueOnce(null)
      .mockReturnValueOnce(userData);

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-loading')).toHaveTextContent('false');
    });

    act(() => {
      screen.getByTestId('login-btn').click();
    });

    expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true');
    expect(screen.getByTestId('user-data')).toHaveTextContent(JSON.stringify(userData));
    expect(screen.getByTestId('error')).toHaveTextContent('null');
  });

  it('should handle login failure when no user data found', async () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValueOnce(null).mockReturnValueOnce(null);

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-loading')).toHaveTextContent('false');
    });

    act(() => {
      screen.getByTestId('login-btn').click();
    });

    expect(screen.getByTestId('is-authenticated')).toHaveTextContent('false');
    expect(screen.getByTestId('error')).toHaveTextContent('No user data found');
  });

  it('should handle login error', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    vi.mocked(cookieUtils.parseUserInfoCookie)
      .mockReturnValueOnce(null)
      .mockImplementationOnce(() => {
        throw new Error('Login failed');
      });

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-loading')).toHaveTextContent('false');
    });

    act(() => {
      screen.getByTestId('login-btn').click();
    });

    expect(screen.getByTestId('error')).toHaveTextContent('Login failed');
    expect(consoleSpy).toHaveBeenCalledWith('Login error:', expect.any(Error));
  });

  it('should return true on successful login', async () => {
    const userData: User = {
      id: 123,
      email: 'test@example.com',
      name: 'Test User',
      firstName: 'Test',
      lastName: 'User',
      profilePicture: null,
    };

    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(userData);

    let loginResult: boolean | undefined;

    const TestLoginResult: React.FC = () => {
      const { login } = useUser();
      return (
        <button
          data-testid='test-login-result'
          onClick={() => {
            loginResult = login();
          }}
        >
          Test Login
        </button>
      );
    };

    render(
      <UserProvider>
        <TestLoginResult />
      </UserProvider>,
    );

    act(() => {
      screen.getByTestId('test-login-result').click();
    });

    expect(loginResult).toBe(true);
  });

  it('should return false on login failure', async () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(null);

    let loginResult: boolean | undefined;

    const TestLoginResult: React.FC = () => {
      const { login } = useUser();
      return (
        <button
          data-testid='test-login-result'
          onClick={() => {
            loginResult = login();
          }}
        >
          Test Login
        </button>
      );
    };

    render(
      <UserProvider>
        <TestLoginResult />
      </UserProvider>,
    );

    await waitFor(() => {
      act(() => {
        screen.getByTestId('test-login-result').click();
      });
    });

    expect(loginResult).toBe(false);
  });

  it('should logout successfully', async () => {
    const userData: User = {
      id: 123,
      name: 'Test User',
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      profilePicture: null,
    };

    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(userData);
    vi.mocked(cookieUtils.deleteCookie).mockImplementation(() => {});

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true');
    });

    act(() => {
      screen.getByTestId('logout-btn').click();
    });

    expect(cookieUtils.deleteCookie).toHaveBeenCalledWith('jwt');
    expect(cookieUtils.deleteCookie).toHaveBeenCalledWith('user_info');
    expect(screen.getByTestId('is-authenticated')).toHaveTextContent('false');
    expect(screen.getByTestId('user-data')).toHaveTextContent('null');
    expect(mockLocation.href).toBe('/');
  });

  it('should handle logout error', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    const userData: User = {
      id: 123,
      name: 'Test User',
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      profilePicture: null,
    };

    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(userData);
    vi.mocked(cookieUtils.deleteCookie).mockImplementation(() => {
      throw new Error('Delete cookie failed');
    });

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true');
    });

    act(() => {
      screen.getByTestId('logout-btn').click();
    });

    expect(screen.getByTestId('error')).toHaveTextContent('Logout failed');
    expect(consoleSpy).toHaveBeenCalledWith('Logout error:', expect.any(Error));
  });

  it('should update user data when user exists', async () => {
    const userData: User = {
      id: 123,
      name: 'Test User',
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
      profilePicture: null,
    };

    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(userData);

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true');
    });

    act(() => {
      screen.getByTestId('update-btn').click();
    });

    const updatedData = JSON.parse(screen.getByTestId('user-data').textContent || '{}');
    expect(updatedData.name).toBe('Updated Name');
    expect(updatedData.email).toBe('test@example.com');
  });

  it('should handle updateUser when no user exists', async () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(null);

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-loading')).toHaveTextContent('false');
    });

    act(() => {
      screen.getByTestId('update-btn').click();
    });

    expect(screen.getByTestId('user-data')).toHaveTextContent('null');
    expect(screen.getByTestId('is-authenticated')).toHaveTextContent('false');
  });

  it('should clear error state', async () => {
    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValueOnce(null).mockReturnValueOnce(null);

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-loading')).toHaveTextContent('false');
    });

    act(() => {
      screen.getByTestId('login-btn').click();
    });

    expect(screen.getByTestId('error')).toHaveTextContent('No user data found');

    act(() => {
      screen.getByTestId('clear-error-btn').click();
    });

    expect(screen.getByTestId('error')).toHaveTextContent('null');
  });

  it('should throw error when used outside provider', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    expect(() => {
      render(<TestComponent />);
    }).toThrow('useUser must be used within a UserProvider');

    consoleSpy.mockRestore();
  });

  it('should handle user with minimal data', async () => {
    const minimalUser: User = {
      id: 1,
      email: 'minimal@example.com',
      name: '',
      firstName: '',
      lastName: '',
      profilePicture: null,
    };

    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(minimalUser);

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true');
    });

    expect(screen.getByTestId('user-data')).toHaveTextContent(JSON.stringify(minimalUser));
  });

  it('should maintain state integrity across multiple operations', async () => {
    const userData: User = {
      id: 123,
      email: 'test@example.com',
      name: 'Test User',
      firstName: 'Test',
      lastName: 'User',
      profilePicture: null,
    };

    vi.mocked(cookieUtils.parseUserInfoCookie).mockReturnValue(userData);

    renderWithProvider();

    await waitFor(() => {
      expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true');
    });

    act(() => {
      screen.getByTestId('update-btn').click();
    });

    const updatedData = JSON.parse(screen.getByTestId('user-data').textContent || '{}');
    expect(updatedData.name).toBe('Updated Name');

    expect(screen.getByTestId('is-authenticated')).toHaveTextContent('true');
    expect(screen.getByTestId('error')).toHaveTextContent('null');
  });
});
