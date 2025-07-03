import { createContext, ReactNode, useContext, useEffect, useReducer } from 'react';

import PropTypes from 'prop-types';

import { User } from '@/GlobalContext/UserContext/types/user';
import { deleteCookie, parseUserInfoCookie } from '@/utils/CookieUtils';

const USER_ACTIONS = {
  SET_LOADING: 'SET_LOADING',
  LOGIN_SUCCESS: 'LOGIN_SUCCESS',
  LOGOUT: 'LOGOUT',
  UPDATE_USER: 'UPDATE_USER',
  SET_ERROR: 'SET_ERROR',
} as const;

interface SetLoadingAction {
  type: typeof USER_ACTIONS.SET_LOADING;
  payload: boolean;
}

interface LoginSuccessAction {
  type: typeof USER_ACTIONS.LOGIN_SUCCESS;
  payload: User;
}

interface LogoutAction {
  type: typeof USER_ACTIONS.LOGOUT;
}

interface UpdateUserAction {
  type: typeof USER_ACTIONS.UPDATE_USER;
  payload: Partial<User>;
}

interface SetErrorAction {
  type: typeof USER_ACTIONS.SET_ERROR;
  payload: string | null;
}

type UserAction =
  | SetLoadingAction
  | LoginSuccessAction
  | LogoutAction
  | UpdateUserAction
  | SetErrorAction;

interface UserState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

interface UserContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  login: () => boolean;
  logout: () => void;
  updateUser: (updates: Partial<User>) => void;
  clearError: () => void;
}

interface UserProviderProps {
  children: ReactNode;
}

const initialState: UserState = {
  user: null,
  isAuthenticated: false,
  isLoading: true,
  error: null,
};

const userReducer = (state: UserState, action: UserAction): UserState => {
  switch (action.type) {
    case USER_ACTIONS.SET_LOADING:
      return {
        ...state,
        isLoading: action.payload,
        error: null,
      };

    case USER_ACTIONS.LOGIN_SUCCESS:
      return {
        ...state,
        user: action.payload,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      };

    case USER_ACTIONS.LOGOUT:
      return {
        ...state,
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      };

    case USER_ACTIONS.UPDATE_USER:
      return {
        ...state,
        user: state.user ? { ...state.user, ...action.payload } : null,
        error: null,
      };

    case USER_ACTIONS.SET_ERROR:
      return {
        ...state,
        error: action.payload,
        isLoading: false,
      };

    default:
      return state;
  }
};

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider: React.FC<UserProviderProps> = ({ children }) => {
  const [state, dispatch] = useReducer(userReducer, initialState);

  useEffect(() => {
    const initializeUser = (): void => {
      try {
        const userData = parseUserInfoCookie();

        if (userData) {
          dispatch({
            type: USER_ACTIONS.LOGIN_SUCCESS,
            payload: userData,
          });
        } else {
          dispatch({
            type: USER_ACTIONS.SET_LOADING,
            payload: false,
          });
        }
      } catch (error) {
        console.error('Error initializing user:', error);
        dispatch({
          type: USER_ACTIONS.SET_ERROR,
          payload: 'Failed to load user data',
        });
      }
    };

    initializeUser();
  }, []);

  const login = (): boolean => {
    try {
      const userData = parseUserInfoCookie();

      if (userData) {
        dispatch({
          type: USER_ACTIONS.LOGIN_SUCCESS,
          payload: userData,
        });
        return true;
      } else {
        dispatch({
          type: USER_ACTIONS.SET_ERROR,
          payload: 'No user data found',
        });
        return false;
      }
    } catch (error) {
      console.error('Login error:', error);
      dispatch({
        type: USER_ACTIONS.SET_ERROR,
        payload: 'Login failed',
      });
      return false;
    }
  };

  const logout = (): void => {
    try {
      deleteCookie('jwt');
      deleteCookie('user_info');

      dispatch({ type: USER_ACTIONS.LOGOUT });

      window.location.href = '/';
    } catch (error) {
      console.error('Logout error:', error);
      dispatch({
        type: USER_ACTIONS.SET_ERROR,
        payload: 'Logout failed',
      });
    }
  };

  const updateUser = (updates: Partial<User>): void => {
    dispatch({
      type: USER_ACTIONS.UPDATE_USER,
      payload: updates,
    });
  };

  const clearError = (): void => {
    dispatch({
      type: USER_ACTIONS.SET_ERROR,
      payload: null,
    });
  };

  const value: UserContextType = {
    user: state.user,
    isAuthenticated: state.isAuthenticated,
    isLoading: state.isLoading,
    error: state.error,

    login,
    logout,
    updateUser,
    clearError,
  };

  return <UserContext.Provider value={value}>{children}</UserContext.Provider>;
};

UserProvider.propTypes = {
  children: PropTypes.node.isRequired,
};

export const useUser = (): UserContextType => {
  const context = useContext(UserContext);

  if (context === undefined) {
    throw new Error('useUser must be used within a UserProvider');
  }

  return context;
};
