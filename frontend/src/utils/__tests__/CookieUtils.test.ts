import { describe, it, expect, beforeEach, vi } from 'vitest';

import { User } from '@/GlobalContext/UserContext/types/user';
import { getCookie, deleteCookie, parseUserInfoCookie } from '@/utils/CookieUtils';

Object.defineProperty(document, 'cookie', {
  writable: true,
  value: '',
  configurable: true,
});

describe('CookieUtils', () => {
  beforeEach(() => {
    let cookieStore = '';
    Object.defineProperty(document, 'cookie', {
      get: () => cookieStore,
      set: (value) => {
        cookieStore = value;
      },
      configurable: true,
    });
    vi.clearAllMocks();
  });

  it('should return cookie value when cookie exists', () => {
    document.cookie = 'testCookie=testValue; path=/';

    const result = getCookie('testCookie');

    expect(result).toBe('testValue');
  });

  it('should return null when cookie does not exist', () => {
    document.cookie = 'otherCookie=otherValue; path=/';

    const result = getCookie('nonExistentCookie');

    expect(result).toBeNull();
  });

  it('should return correct value when multiple cookies exist', () => {
    document.cookie = 'cookie1=value1; cookie2=value2; cookie3=value3';

    const result = getCookie('cookie2');

    expect(result).toBe('value2');
  });

  it('should handle cookies with special characters in values', () => {
    document.cookie = 'specialCookie=value%20with%20spaces; path=/';

    const result = getCookie('specialCookie');

    expect(result).toBe('value%20with%20spaces');
  });

  it('should return empty string for cookie with empty value', () => {
    document.cookie = 'emptyCookie=; path=/';

    const result = getCookie('emptyCookie');

    expect(result).toBeNull();
  });

  it('should handle cookie name that is substring of another cookie', () => {
    document.cookie = 'user=john; user_info={"id":1}; path=/';

    const result = getCookie('user');

    expect(result).toBe('john');
  });

  it('should return null for undefined cookie parts', () => {
    document.cookie = 'invalidFormat';

    const result = getCookie('test');

    expect(result).toBeNull();
  });

  it('should set cookie expiration to past date with correct format', () => {
    const mockCookieSetter = vi.fn();
    Object.defineProperty(document, 'cookie', {
      set: mockCookieSetter,
      configurable: true,
    });

    deleteCookie('testCookie');

    expect(mockCookieSetter).toHaveBeenCalledWith(
      'testCookie=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=localhost;',
    );
  });

  it('should handle special characters in cookie name', () => {
    const mockCookieSetter = vi.fn();
    Object.defineProperty(document, 'cookie', {
      set: mockCookieSetter,
      configurable: true,
    });

    deleteCookie('special-cookie_123');

    expect(mockCookieSetter).toHaveBeenCalledWith(
      'special-cookie_123=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=localhost;',
    );
  });

  it('should parse valid user info cookie successfully', () => {
    const userData: User = {
      id: 123,
      email: 'test@example.com',
      name: 'Test User',
      firstName: 'Test',
      lastName: 'User',
      profilePicture: 'https://example.com/avatar.jpg',
    };

    const encodedData = encodeURIComponent(JSON.stringify(userData));
    document.cookie = `user_info=${encodedData}; path=/`;

    const result = parseUserInfoCookie();

    expect(result).toEqual(userData);
  });

  it('should return null when user_info cookie does not exist', () => {
    document.cookie = 'otherCookie=value; path=/';

    const result = parseUserInfoCookie();

    expect(result).toBeNull();
  });

  it('should return null and log error when cookie contains invalid JSON', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    document.cookie = 'user_info=invalidJson; path=/';

    const result = parseUserInfoCookie();

    expect(result).toBeNull();
    expect(consoleSpy).toHaveBeenCalledWith('Error parsing user info cookie:', expect.any(Error));

    consoleSpy.mockRestore();
  });

  it('should handle and replace plus signs in name fields', () => {
    const userData = {
      id: 456,
      email: 'user+test@example.com',
      name: 'José+María+García',
      firstName: 'José+María',
      lastName: 'García+López',
      profilePicture: 'https://example.com/profile.jpg',
    };

    const encodedData = encodeURIComponent(JSON.stringify(userData));
    document.cookie = `user_info=${encodedData}; path=/`;

    const result = parseUserInfoCookie();

    expect(result).toEqual({
      id: 456,
      email: 'user+test@example.com',
      name: 'José María García',
      firstName: 'José María',
      lastName: 'García López',
      profilePicture: 'https://example.com/profile.jpg',
    });
  });

  it('should handle user data with null profilePicture', () => {
    const userData: User = {
      id: 123,
      email: 'test@example.com',
      name: 'Test User',
      firstName: 'Test',
      lastName: 'User',
      profilePicture: null,
    };

    const encodedData = encodeURIComponent(JSON.stringify(userData));
    document.cookie = `user_info=${encodedData}; path=/`;

    const result = parseUserInfoCookie();

    expect(result).toEqual(userData);
  });

  it('should handle user data with empty string fields', () => {
    const userData = {
      id: 789,
      email: 'empty@example.com',
      name: '',
      firstName: '',
      lastName: '',
      profilePicture: '',
    };

    const encodedData = encodeURIComponent(JSON.stringify(userData));
    document.cookie = `user_info=${encodedData}; path=/`;

    const result = parseUserInfoCookie();

    expect(result).toEqual(userData);
  });

  it('should handle user data with only some name fields having plus signs', () => {
    const userData = {
      id: 999,
      email: 'partial@example.com',
      name: 'John+Doe',
      firstName: 'John',
      lastName: 'Doe+Jr',
      profilePicture: 'https://example.com/pic.jpg',
    };

    const encodedData = encodeURIComponent(JSON.stringify(userData));
    document.cookie = `user_info=${encodedData}; path=/`;

    const result = parseUserInfoCookie();

    expect(result).toEqual({
      id: 999,
      email: 'partial@example.com',
      name: 'John Doe',
      firstName: 'John',
      lastName: 'Doe Jr',
      profilePicture: 'https://example.com/pic.jpg',
    });
  });

  it('should handle malformed URL encoding gracefully', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    document.cookie = 'user_info=%invalid%encoding; path=/';

    const result = parseUserInfoCookie();

    expect(result).toBeNull();
    expect(consoleSpy).toHaveBeenCalledWith('Error parsing user info cookie:', expect.any(Error));

    consoleSpy.mockRestore();
  });

  it('should handle multiple plus signs in a single field', () => {
    const userData = {
      id: 111,
      email: 'test@example.com',
      name: 'Mary+Jane+Watson+Parker',
      firstName: 'Mary+Jane',
      lastName: 'Watson+Parker',
      profilePicture: null,
    };

    const encodedData = encodeURIComponent(JSON.stringify(userData));
    document.cookie = `user_info=${encodedData}; path=/`;

    const result = parseUserInfoCookie();

    expect(result).toEqual({
      id: 111,
      email: 'test@example.com',
      name: 'Mary Jane Watson Parker',
      firstName: 'Mary Jane',
      lastName: 'Watson Parker',
      profilePicture: null,
    });
  });

  it('should handle undefined name fields without errors', () => {
    const userData = {
      id: 222,
      email: 'test@example.com',
      profilePicture: 'https://example.com/pic.jpg',
    };

    const encodedData = encodeURIComponent(JSON.stringify(userData));
    document.cookie = `user_info=${encodedData}; path=/`;

    const result = parseUserInfoCookie();

    expect(result).toMatchObject({
      id: 222,
      email: 'test@example.com',
      profilePicture: 'https://example.com/pic.jpg',
    });
  });

  it('should handle decodeURIComponent errors', () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    const originalDecodeURIComponent = global.decodeURIComponent;
    global.decodeURIComponent = vi.fn().mockImplementation(() => {
      throw new Error('Decode error');
    });

    document.cookie = 'user_info=someValue; path=/';

    const result = parseUserInfoCookie();

    expect(result).toBeNull();
    expect(consoleSpy).toHaveBeenCalledWith('Error parsing user info cookie:', expect.any(Error));

    global.decodeURIComponent = originalDecodeURIComponent;
    consoleSpy.mockRestore();
  });
});
