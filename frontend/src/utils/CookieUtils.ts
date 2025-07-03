import { User } from '@/GlobalContext/UserContext/types/user';

export const getCookie = (name: string): string | null => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop()?.split(';').shift() || null;
  }
  return null;
};

export const deleteCookie = (name: string): void => {
  document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; domain=localhost;`;
};

export const parseUserInfoCookie = (): User | null => {
  try {
    const userInfoCookie = getCookie('user_info');
    if (!userInfoCookie) {
      return null;
    }

    const decodedJson = decodeURIComponent(userInfoCookie);

    const userData: User = JSON.parse(decodedJson);

    if (userData.name) {
      userData.name = userData.name.replace(/\+/g, ' ');
    }

    if (userData.firstName) {
      userData.firstName = userData.firstName.replace(/\+/g, ' ');
    }

    if (userData.lastName) {
      userData.lastName = userData.lastName.replace(/\+/g, ' ');
    }

    return userData as User;
  } catch (error) {
    console.error('Error parsing user info cookie:', error);
    return null;
  }
};
