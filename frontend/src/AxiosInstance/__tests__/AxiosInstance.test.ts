import axios from 'axios';
import { vi } from 'vitest';

import { axiosInstance } from '@/AxiosInstance/AxiosInstance';

vi.mock('axios', () => ({
  default: {
    create: vi.fn(),
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
}));

const mockedAxios = vi.mocked(axios);

describe('axiosInstance', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(axios.create).mockReturnValue(mockedAxios);
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

    it('should create a new axios instance when called', () => {
      axiosInstance();

      expect(vi.mocked(axios.create)).toHaveBeenCalledTimes(1);
      expect(vi.mocked(axios.create)).toHaveBeenCalledWith();
    });

    it('should create a new instance on each call', () => {
      axiosInstance();
      axiosInstance();

      expect(vi.mocked(axios.create)).toHaveBeenCalledTimes(2);
    });
  });
});
