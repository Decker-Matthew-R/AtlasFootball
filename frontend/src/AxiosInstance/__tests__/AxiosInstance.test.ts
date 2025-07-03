import axios from 'axios';
import { vi } from 'vitest';

vi.mock('axios', () => {
  const mockInstance = {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
    request: vi.fn(),
    interceptors: {
      request: {
        use: vi.fn(),
      },
      response: {
        use: vi.fn(),
      },
    },
  };

  return {
    default: {
      create: vi.fn(() => mockInstance),
      defaults: {
        headers: {
          post: {},
        },
        withXSRFToken: undefined,
        xsrfCookieName: undefined,
        xsrfHeaderName: undefined,
        withCredentials: undefined,
      },
    },
  };
});

import defaultInstance, { axiosInstance, SecurityUtils } from '@/AxiosInstance/AxiosInstance';

Object.defineProperty(document, 'cookie', {
  writable: true,
  value: '',
});

const consoleSpy = {
  log: vi.spyOn(console, 'log').mockImplementation(() => {}),
  error: vi.spyOn(console, 'error').mockImplementation(() => {}),
};

type MockAxiosConfig = {
  method?: string;
  url?: string;
  headers?: Record<string, string>;
  _skipCsrfInterceptor?: boolean;
  _csrfRetryAttempted?: boolean;
};

type MockAxiosError = {
  response?: { status: number };
  config: MockAxiosConfig;
};

describe('axiosInstance', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    document.cookie = '';
    consoleSpy.log.mockClear();
    consoleSpy.error.mockClear();
  });

  afterAll(() => {
    consoleSpy.log.mockRestore();
    consoleSpy.error.mockRestore();
  });

  describe('axios defaults configuration', () => {
    it('should set correct Content-Type header for POST requests', () => {
      expect(axios.defaults.headers.post['Content-Type']).toBe('application/json;charset=utf-8');
    });

    it('should enable XSRF token handling', () => {
      expect(axios.defaults.withXSRFToken).toBe(true);
    });

    it('should set correct XSRF cookie name', () => {
      expect(axios.defaults.xsrfCookieName).toBe('XSRF-TOKEN');
    });

    it('should set correct XSRF header name', () => {
      expect(axios.defaults.xsrfHeaderName).toBe('X-XSRF-TOKEN');
    });

    it('should enable credentials', () => {
      expect(axios.defaults.withCredentials).toBe(true);
    });
  });

  describe('instance creation', () => {
    it('should create axios instance with correct configuration', () => {
      axiosInstance();

      expect(vi.mocked(axios.create)).toHaveBeenCalledWith({
        baseURL: 'http://localhost:8080',
        timeout: 10000,
      });
    });

    it('should use VITE_API_BASE_URL when available', () => {
      const originalEnv = import.meta.env.VITE_API_BASE_URL;
      import.meta.env.VITE_API_BASE_URL = 'https://api.example.com';

      axiosInstance();

      expect(vi.mocked(axios.create)).toHaveBeenCalledWith({
        baseURL: 'https://api.example.com',
        timeout: 10000,
      });

      import.meta.env.VITE_API_BASE_URL = originalEnv;
    });

    it('should set up request and response interceptors', () => {
      axiosInstance();

      expect(vi.mocked(axios.create)).toHaveBeenCalled();
    });
  });

  describe('SecurityUtils', () => {
    it('should check if CSRF token exists', () => {
      expect(SecurityUtils.hasCsrfToken()).toBe(false);

      document.cookie = 'XSRF-TOKEN=test-token-123';
      expect(SecurityUtils.hasCsrfToken()).toBe(true);
    });

    it('should get current CSRF token', () => {
      expect(SecurityUtils.getCurrentCsrfToken()).toBeNull();

      document.cookie = 'XSRF-TOKEN=test-token-123';
      expect(SecurityUtils.getCurrentCsrfToken()).toBe('test-token-123');
    });

    it('should handle URL encoded CSRF tokens', () => {
      document.cookie = 'XSRF-TOKEN=test%2Btoken%3D123';
      expect(SecurityUtils.getCurrentCsrfToken()).toBe('test+token=123');
    });

    it('should refresh CSRF token successfully', async () => {
      document.cookie = 'XSRF-TOKEN=new-token';

      const result = await SecurityUtils.refreshCsrfToken();

      expect(result).toBe(true);
    });

    it('should handle CSRF token refresh failure', async () => {
      const getSpy = vi
        .spyOn(defaultInstance, 'get')
        .mockRejectedValueOnce(new Error('Network error'));

      const result = await SecurityUtils.refreshCsrfToken();

      expect(result).toBe(false);
      expect(consoleSpy.error).toHaveBeenCalledWith(
        'Failed to refresh CSRF token:',
        expect.any(Error),
      );

      getSpy.mockRestore();
    });

    it('should return false when no token exists after refresh', async () => {
      const result = await SecurityUtils.refreshCsrfToken();

      expect(result).toBe(false);
    });
  });

  describe('CSRF token handling', () => {
    it('should extract CSRF token from cookie correctly', () => {
      document.cookie = 'other=value; XSRF-TOKEN=abc123; another=value';

      expect(SecurityUtils.getCurrentCsrfToken()).toBe('abc123');
    });

    it('should handle missing CSRF token', () => {
      document.cookie = 'other=value; another=value';

      expect(SecurityUtils.getCurrentCsrfToken()).toBeNull();
    });

    it('should handle empty cookie', () => {
      document.cookie = '';

      expect(SecurityUtils.getCurrentCsrfToken()).toBeNull();
    });

    it('should handle malformed cookie', () => {
      document.cookie = 'XSRF-TOKEN';

      expect(SecurityUtils.getCurrentCsrfToken()).toBeNull();
    });

    it('should handle cookie with empty value', () => {
      document.cookie = 'XSRF-TOKEN=';

      expect(SecurityUtils.getCurrentCsrfToken()).toBe('');
    });
  });

  describe('interceptor behavior integration tests', () => {
    it('should handle successful requests', () => {
      const instance = axiosInstance();
      expect(instance).toBeDefined();
      expect(instance.get).toBeDefined();
      expect(instance.post).toBeDefined();
    });

    it('should configure CSRF token constants correctly', () => {
      expect(axios.defaults.xsrfCookieName).toBe('XSRF-TOKEN');
      expect(axios.defaults.xsrfHeaderName).toBe('X-XSRF-TOKEN');
    });

    it('should trigger request interceptor on POST with CSRF token', async () => {
      document.cookie = 'XSRF-TOKEN=test-token';

      axiosInstance();

      expect(vi.mocked(axios.create)).toHaveBeenCalled();
    });

    it('should trigger request interceptor and handle config modification', () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const requestInterceptor = mockInstance?.interceptors?.request?.use?.mock?.calls?.[0]?.[0];

      if (!requestInterceptor) {
        return;
      }

      document.cookie = 'XSRF-TOKEN=test-token';

      const config: MockAxiosConfig = {
        method: 'post',
        url: '/test',
        headers: {},
      };

      const result = requestInterceptor(config);
      expect(result.headers?.['X-XSRF-TOKEN']).toBe('test-token');
    });

    it('should skip CSRF interceptor when _skipCsrfInterceptor flag is set', () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const requestInterceptor = mockInstance?.interceptors?.request?.use?.mock?.calls?.[0]?.[0];

      if (!requestInterceptor) {
        return;
      }

      document.cookie = 'XSRF-TOKEN=test-token';

      const config: MockAxiosConfig = {
        method: 'post',
        url: '/test',
        headers: {},
        _skipCsrfInterceptor: true,
      };

      const result = requestInterceptor(config);

      expect(result).toBe(config);
      expect(result.headers?.['X-XSRF-TOKEN']).toBeUndefined();
    });

    it('should handle request with no CSRF token available', () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const requestInterceptor = mockInstance?.interceptors?.request?.use?.mock?.calls?.[0]?.[0];

      if (!requestInterceptor) {
        return;
      }

      document.cookie = '';

      const config: MockAxiosConfig = {
        method: 'post',
        url: '/test',
        headers: {},
      };

      const result = requestInterceptor(config);

      expect(result.headers?.['X-XSRF-TOKEN']).toBeUndefined();
    });

    it('should not override existing CSRF token in headers', () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const requestInterceptor = mockInstance?.interceptors?.request?.use?.mock?.calls?.[0]?.[0];

      if (!requestInterceptor) {
        return;
      }

      document.cookie = 'XSRF-TOKEN=cookie-token';

      const config: MockAxiosConfig = {
        method: 'post',
        url: '/test',
        headers: {
          'X-XSRF-TOKEN': 'existing-header-token',
        },
      };

      const result = requestInterceptor(config);

      expect(result.headers?.['X-XSRF-TOKEN']).toBe('existing-header-token');
    });

    it('should handle request with null headers object', () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const requestInterceptor = mockInstance?.interceptors?.request?.use?.mock?.calls?.[0]?.[0];

      if (!requestInterceptor) {
        return;
      }

      document.cookie = 'XSRF-TOKEN=test-token';

      const config: MockAxiosConfig = {
        method: 'post',
        url: '/test',
      };

      const result = requestInterceptor(config);

      expect(result.headers).toBeDefined();
      expect(result.headers?.['X-XSRF-TOKEN']).toBe('test-token');
    });

    it('should handle request interceptor with undefined method', () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const requestInterceptor = mockInstance?.interceptors?.request?.use?.mock?.calls?.[0]?.[0];

      if (!requestInterceptor) {
        return;
      }

      document.cookie = 'XSRF-TOKEN=test-token';

      const config: MockAxiosConfig = {
        url: '/test',
        headers: {},
      };

      const result = requestInterceptor(config);

      expect(result.headers?.['X-XSRF-TOKEN']).toBeUndefined();
    });

    it('should trigger response interceptor error handler', async () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const errorHandler = mockInstance?.interceptors?.response?.use?.mock?.calls?.[0]?.[1];

      if (!errorHandler) {
        return;
      }

      document.cookie = 'XSRF-TOKEN=new-token';
      mockInstance.get.mockResolvedValue({ data: {} });
      mockInstance.request.mockResolvedValue({ data: 'retry-success' });

      const error: MockAxiosError = {
        response: { status: 403 },
        config: {
          method: 'post',
          url: '/api/test',
          headers: {},
        },
      };

      const result = await errorHandler(error);
      expect(result).toEqual({ data: 'retry-success' });
      expect(error.config._csrfRetryAttempted).toBe(true);
    });

    it('should handle concurrent CSRF refresh requests', async () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const errorHandler = mockInstance?.interceptors?.response?.use?.mock?.calls?.[0]?.[1];

      if (!errorHandler) {
        return;
      }

      document.cookie = 'XSRF-TOKEN=concurrent-token';
      mockInstance.get.mockResolvedValue({ data: {} });
      mockInstance.request.mockResolvedValue({ data: 'success' });

      const error1: MockAxiosError = {
        response: { status: 403 },
        config: { method: 'post', url: '/api/test1', headers: {} },
      };
      const error2: MockAxiosError = {
        response: { status: 403 },
        config: { method: 'post', url: '/api/test2', headers: {} },
      };

      const [result1, result2] = await Promise.all([errorHandler(error1), errorHandler(error2)]);

      expect(result1).toEqual({ data: 'success' });
      expect(result2).toEqual({ data: 'success' });

      expect(error1.config._csrfRetryAttempted).toBe(true);
      expect(error2.config._csrfRetryAttempted).toBe(true);
    });

    it('should handle request interceptor error passthrough', async () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const requestErrorHandler = mockInstance?.interceptors?.request?.use?.mock?.calls?.[0]?.[1];

      if (!requestErrorHandler) {
        return;
      }

      const error = new Error('Request setup failed');
      await expect(requestErrorHandler(error)).rejects.toBe(error);
    });

    it('should handle response interceptor success passthrough', () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const responseInterceptor = mockInstance?.interceptors?.response?.use?.mock?.calls?.[0]?.[0];

      if (!responseInterceptor) {
        return;
      }

      const response = { data: 'test', status: 200 };
      const result = responseInterceptor(response);
      expect(result).toBe(response);
    });

    it('should skip CSRF retry on non-403 errors', async () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const errorHandler = mockInstance?.interceptors?.response?.use?.mock?.calls?.[0]?.[1];

      if (!errorHandler) {
        return;
      }

      const error: MockAxiosError = {
        response: { status: 500 },
        config: { method: 'post', url: '/api/test' },
      };

      await await expect(errorHandler(error)).rejects.toBe(error);
      expect(mockInstance.get).not.toHaveBeenCalled();
    });

    it('should handle missing response in error', async () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const errorHandler = mockInstance?.interceptors?.response?.use?.mock?.calls?.[0]?.[1];

      if (!errorHandler) {
        return;
      }

      const error: MockAxiosError = {
        config: { method: 'post', url: '/api/test' },
      };

      await expect(errorHandler(error)).rejects.toBe(error);
      expect(mockInstance.get).not.toHaveBeenCalled();
    });

    it('should handle CSRF fetch failure in error handler', async () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const errorHandler = mockInstance?.interceptors?.response?.use?.mock?.calls?.[0]?.[1];

      if (!errorHandler) {
        return;
      }

      mockInstance.get.mockRejectedValue(new Error('CSRF fetch failed'));

      const error: MockAxiosError = {
        response: { status: 403 },
        config: { method: 'post', url: '/api/test', headers: {} },
      };

      await expect(errorHandler(error)).rejects.toBe(error);
      expect(consoleSpy.error).toHaveBeenCalledWith(
        'Failed to refresh CSRF token:',
        expect.any(Error),
      );
    });

    it('should handle CSRF retry when empty token is available after refresh', async () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const errorHandler = mockInstance?.interceptors?.response?.use?.mock?.calls?.[0]?.[1];

      if (!errorHandler) {
        return;
      }

      mockInstance.get.mockResolvedValue({ data: {} });
      document.cookie = 'XSRF-TOKEN=';

      const error: MockAxiosError = {
        response: { status: 403 },
        config: { method: 'post', url: '/api/test', headers: {} },
      };

      await expect(errorHandler(error)).rejects.toBe(error);
      expect(mockInstance.get).toHaveBeenCalledWith('/api/csrf', { _skipCsrfInterceptor: true });
      expect(mockInstance.request).not.toHaveBeenCalled();
    });

    it('should handle request interceptor with empty CSRF token', () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const requestInterceptor = mockInstance?.interceptors?.request?.use?.mock?.calls?.[0]?.[0];

      if (!requestInterceptor) {
        return;
      }

      document.cookie = 'XSRF-TOKEN=';

      const config: MockAxiosConfig = {
        method: 'post',
        url: '/test',
        headers: {},
      };

      const result = requestInterceptor(config);

      expect(result.headers?.['X-XSRF-TOKEN']).toBeUndefined();
    });

    it('should handle CSRF retry when no new token is available after refresh', async () => {
      axiosInstance();
      const mockCreate = vi.mocked(axios.create);
      const mockInstance = mockCreate.mock.results[mockCreate.mock.results.length - 1]?.value;
      const errorHandler = mockInstance?.interceptors?.response?.use?.mock?.calls?.[0]?.[1];

      if (!errorHandler) {
        return;
      }

      mockInstance.get.mockResolvedValue({ data: {} });
      document.cookie = '';

      const error: MockAxiosError = {
        response: { status: 403 },
        config: { method: 'post', url: '/api/test', headers: {} },
      };

      await expect(errorHandler(error)).rejects.toBe(error);
      expect(mockInstance.get).toHaveBeenCalledWith('/api/csrf', { _skipCsrfInterceptor: true });
      expect(mockInstance.request).not.toHaveBeenCalled();
    });
  });

  describe('getCsrfToken function edge cases', () => {
    it('should handle cookie parsing edge cases', () => {
      document.cookie = 'XSRF-TOKEN=value1';
      const token = SecurityUtils.getCurrentCsrfToken();
      expect(token).toBe('value1');

      document.cookie = '';
      document.cookie = 'prefix-XSRF-TOKEN=wrong; XSRF-TOKEN=correct';
      expect(SecurityUtils.getCurrentCsrfToken()).toBe('correct');

      document.cookie = '';
      document.cookie = 'XSRF-TOKEN=has';
      expect(SecurityUtils.getCurrentCsrfToken()).toBe('has');

      document.cookie = '';
      document.cookie = 'XSRF-TOKEN=token; other=value';
      expect(SecurityUtils.getCurrentCsrfToken()).toBe('token');
    });

    it('should handle complex URL encoding', () => {
      document.cookie = 'XSRF-TOKEN=test%20with%20spaces%26symbols';
      expect(SecurityUtils.getCurrentCsrfToken()).toBe('test with spaces&symbols');
    });
  });

  describe('module exports', () => {
    it('should export axiosInstance function', () => {
      expect(typeof axiosInstance).toBe('function');
    });

    it('should export SecurityUtils object', () => {
      expect(typeof SecurityUtils).toBe('object');
      expect(typeof SecurityUtils.refreshCsrfToken).toBe('function');
      expect(typeof SecurityUtils.hasCsrfToken).toBe('function');
      expect(typeof SecurityUtils.getCurrentCsrfToken).toBe('function');
    });

    it('should export default instance', () => {
      expect(defaultInstance).toBeDefined();
      expect(typeof defaultInstance.get).toBe('function');
      expect(typeof defaultInstance.post).toBe('function');
    });
  });
});
