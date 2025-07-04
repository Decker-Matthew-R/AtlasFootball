import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';

/*
 * This is a singleton that will load on all the interceptors.
 *
 * Use this instead of directly importing axios.
 */

const XSRF_TOKEN_COOKIE_NAME = 'XSRF-TOKEN';
const XSRF_TOKEN_HEADER_NAME = 'X-XSRF-TOKEN';

let csrfFetchPromise: Promise<void> | null = null;

axios.defaults.headers.post['Content-Type'] = 'application/json;charset=utf-8';
axios.defaults.withXSRFToken = true;
axios.defaults.xsrfCookieName = XSRF_TOKEN_COOKIE_NAME;
axios.defaults.xsrfHeaderName = XSRF_TOKEN_HEADER_NAME;
axios.defaults.withCredentials = true;

function getCsrfToken(): string | null {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${XSRF_TOKEN_COOKIE_NAME}=`);
  if (parts.length === 2) {
    return decodeURIComponent(parts.pop()?.split(';').shift() || '');
  }
  return null;
}

export const axiosInstance = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    timeout: 10000,
  });

  const fetchCsrfToken = async (): Promise<void> => {
    if (csrfFetchPromise) {
      return csrfFetchPromise;
    }

    csrfFetchPromise = instance
      .get('/api/csrf', {
        _skipCsrfInterceptor: true,
      } as AxiosRequestConfig & { _skipCsrfInterceptor?: boolean })
      .then(() => {
        csrfFetchPromise = null;
      })
      .catch((error) => {
        csrfFetchPromise = null;
        throw error;
      });

    return csrfFetchPromise;
  };

  instance.interceptors.request.use(
    // @ts-expect-error axios patterns have been updated
    (config: AxiosRequestConfig & { _skipCsrfInterceptor?: boolean }) => {
      if (config._skipCsrfInterceptor) {
        return config;
      }

      const methodsRequiringCsrf = ['post', 'put', 'delete', 'patch'];
      if (methodsRequiringCsrf.includes(config.method?.toLowerCase() || '')) {
        const csrfToken = getCsrfToken();
        if (csrfToken && !config.headers?.[XSRF_TOKEN_HEADER_NAME]) {
          config.headers = config.headers || {};
          config.headers[XSRF_TOKEN_HEADER_NAME] = csrfToken;
        }
      }

      return config;
    },
    (error) => Promise.reject(error),
  );

  instance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      if (
        error.response?.status === 403 &&
        originalRequest &&
        !originalRequest._csrfRetryAttempted &&
        !originalRequest._skipCsrfInterceptor
      ) {
        originalRequest._csrfRetryAttempted = true;

        try {
          await fetchCsrfToken();

          const newCsrfToken = getCsrfToken();
          if (newCsrfToken) {
            originalRequest.headers = originalRequest.headers || {};
            originalRequest.headers[XSRF_TOKEN_HEADER_NAME] = newCsrfToken;

            return instance.request(originalRequest);
          }
        } catch (csrfError) {
          console.error('Failed to refresh CSRF token:', csrfError);
        }
      }

      return Promise.reject(error);
    },
  );

  return instance;
};

const defaultInstance = axiosInstance();

export const SecurityUtils = {
  async refreshCsrfToken(): Promise<boolean> {
    try {
      await defaultInstance.get('/api/csrf', {
        _skipCsrfInterceptor: true,
      } as AxiosRequestConfig & { _skipCsrfInterceptor?: boolean });
      return getCsrfToken() !== null;
    } catch (error) {
      console.error('Failed to refresh CSRF token:', error);
      return false;
    }
  },

  hasCsrfToken(): boolean {
    return getCsrfToken() !== null;
  },

  getCurrentCsrfToken(): string | null {
    return getCsrfToken();
  },
};

export default defaultInstance;
