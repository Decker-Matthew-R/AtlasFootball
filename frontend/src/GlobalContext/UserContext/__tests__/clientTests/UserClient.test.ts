import { describe, it, expect, vi, beforeEach } from 'vitest';

import { API_ENDPOINTS } from '@/ApiEndpoints/API_ENDPOINTS';
import axiosInstance from '@/AxiosInstance/AxiosInstance';
import { logUserOut } from '@/GlobalContext/UserContext/client/UserClient';

vi.mock('@/AxiosInstance/AxiosInstance');

vi.mock('@/ApiEndpoints/API_ENDPOINTS', () => ({
  API_ENDPOINTS: {
    LOG_OUT_EVENT: '/logout',
  },
}));

describe('UserClient', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should successfully log user out when API call succeeds', async () => {
    const mockResponse = {
      status: 200,
      data: { message: 'Logged out successfully' },
    };
    vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

    await expect(logUserOut()).resolves.toBeUndefined();

    expect(axiosInstance.post).toHaveBeenCalledTimes(1);
    expect(axiosInstance.post).toHaveBeenCalledWith(API_ENDPOINTS.LOG_OUT_EVENT);
  });

  it('should call the correct API endpoint', async () => {
    const mockResponse = { status: 200, data: {} };
    vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

    await logUserOut();

    expect(axiosInstance.post).toHaveBeenCalledWith('/logout');
  });

  it('should throw error when API call fails with 4xx status', async () => {
    const mockError = {
      response: {
        status: 401,
        data: { error: 'Unauthorized' },
      },
    };
    vi.mocked(axiosInstance.post).mockRejectedValue(mockError);

    await expect(logUserOut()).rejects.toThrow('Failed to Log User Out');

    expect(axiosInstance.post).toHaveBeenCalledTimes(1);
  });

  it('should throw error when API call fails with 5xx status', async () => {
    const mockError = {
      response: {
        status: 500,
        data: { error: 'Internal Server Error' },
      },
    };
    vi.mocked(axiosInstance.post).mockRejectedValue(mockError);

    await expect(logUserOut()).rejects.toThrow('Failed to Log User Out');
  });

  it('should throw error when network request fails', async () => {
    const networkError = new Error('Network Error');
    vi.mocked(axiosInstance.post).mockRejectedValue(networkError);

    await expect(logUserOut()).rejects.toThrow('Failed to Log User Out');

    expect(axiosInstance.post).toHaveBeenCalledTimes(1);
  });

  it('should handle timeout errors', async () => {
    const timeoutError = {
      code: 'ECONNABORTED',
      message: 'timeout of 5000ms exceeded',
    };
    vi.mocked(axiosInstance.post).mockRejectedValue(timeoutError);

    await expect(logUserOut()).rejects.toThrow('Failed to Log User Out');
  });

  it('should not return any value on success', async () => {
    const mockResponse = { status: 200, data: {} };
    vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

    const result = await logUserOut();

    expect(result).toBeUndefined();
  });

  it('should handle different successful status codes', async () => {
    const successfulStatuses = [200, 201, 204];

    for (const status of successfulStatuses) {
      const mockResponse = { status, data: {} };
      vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

      await expect(logUserOut()).resolves.toBeUndefined();
    }

    expect(axiosInstance.post).toHaveBeenCalledTimes(successfulStatuses.length);
  });

  it('should handle response with different data structures', async () => {
    const mockResponse = {
      status: 200,
      data: {
        message: 'Logged out successfully',
        timestamp: '2024-01-01T00:00:00Z',
        userId: 123,
      },
    };
    vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

    await expect(logUserOut()).resolves.toBeUndefined();
  });

  it('should handle empty response data', async () => {
    const mockResponse = {
      status: 204,
      data: null,
    };
    vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

    await expect(logUserOut()).resolves.toBeUndefined();
  });

  it('should throw consistent error message regardless of error type', async () => {
    const errors = [
      new Error('Network Error'),
      { response: { status: 404 } },
      { response: { status: 500 } },
      { code: 'ECONNABORTED' },
      'String error',
    ];

    for (const error of errors) {
      vi.mocked(axiosInstance.post).mockRejectedValue(error);

      await expect(logUserOut()).rejects.toThrow('Failed to Log User Out');
    }
  });

  it('should call axiosInstance.post exactly once per invocation', async () => {
    const mockResponse = { status: 200, data: {} };
    vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

    await logUserOut();
    await logUserOut();
    await logUserOut();

    expect(axiosInstance.post).toHaveBeenCalledTimes(3);
  });

  it('should not catch or modify successful responses', async () => {
    const mockResponse = { status: 200, data: { success: true } };
    vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

    const startTime = Date.now();
    await logUserOut();
    const endTime = Date.now();

    expect(endTime - startTime).toBeLessThan(100);
    expect(axiosInstance.post).toHaveBeenCalledTimes(1);
  });

  it('should handle axios instance being undefined', async () => {
    vi.mocked(axiosInstance.post).mockImplementation(() => {
      throw new Error('axiosInstance is undefined');
    });

    await expect(logUserOut()).rejects.toThrow('Failed to Log User Out');
  });

  it('should handle malformed API_ENDPOINTS', async () => {
    const mockResponse = { status: 200, data: {} };
    vi.mocked(axiosInstance.post).mockResolvedValue(mockResponse);

    await expect(logUserOut()).resolves.toBeUndefined();

    expect(axiosInstance.post).toHaveBeenCalledWith(API_ENDPOINTS.LOG_OUT_EVENT);
  });
});
