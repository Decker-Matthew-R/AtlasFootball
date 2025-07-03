import { renderHook } from '@testing-library/react';
import { rest } from 'msw';
import { describe, expect, vi, beforeEach } from 'vitest';

import { API_ENDPOINTS } from '@/ApiEndpoints/API_ENDPOINTS';
import { User } from '@/GlobalContext/UserContext/types/user';
import { useUser } from '@/GlobalContext/UserContext/UserContext';
import { useMetrics } from '@/metrics/client/MetricsClient';
import { MetadataType } from '@/metrics/model/MetadataType';
import { METRIC_EVENT_TYPE } from '@/metrics/model/METRIC_EVENT_TYPE';
import { server } from '@/setupTests';

vi.mock('@/GlobalContext/UserContext/UserContext', () => ({
  useUser: vi.fn(),
}));

describe('Metrics Client', () => {
  const metadata: MetadataType = { triggerId: 'React Button', screen: 'Home' };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('when user is logged in', () => {
    beforeEach(() => {
      vi.mocked(useUser).mockReturnValue({
        user: {
          id: 123,
          email: 'test@example.com',
          name: 'Test User',
          firstName: 'Test',
          lastName: 'User',
          profilePicture: null,
        },
        isAuthenticated: true,
        isLoading: false,
        error: null,
        login: vi.fn(),
        logout: vi.fn(),
        updateUser: vi.fn(),
        clearError: vi.fn(),
      });
    });

    it('should post correct data to the metrics endpoint with userId', async () => {
      let requestBody;
      const mockHandler = vi.fn(async (req, res, ctx) => {
        requestBody = await req.json();
        return res(ctx.status(201), ctx.json({}));
      });

      server.use(
        rest.post(`http://localhost:8080${API_ENDPOINTS.RECORD_METRIC_EVENT}`, mockHandler),
      );

      const { result } = renderHook(() => useMetrics());

      const savePromise = result.current.saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, metadata);

      await expect(savePromise).resolves.toBeUndefined();
      expect(mockHandler).toHaveBeenCalledTimes(1);
      expect(requestBody).toEqual({
        event: METRIC_EVENT_TYPE.BUTTON_CLICK,
        eventMetadata: metadata,
        userId: 123,
      });
    });

    it('should throw error when request fails', async () => {
      server.use(
        rest.post(`http://localhost:8080${API_ENDPOINTS.RECORD_METRIC_EVENT}`, (req, res, ctx) =>
          res(ctx.status(500)),
        ),
      );

      const { result } = renderHook(() => useMetrics());

      await expect(
        result.current.saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, metadata),
      ).rejects.toThrow('Failed to capture metric event.');
    });

    it('should throw error when network request fails', async () => {
      server.use(
        rest.post(`http://localhost:8080${API_ENDPOINTS.RECORD_METRIC_EVENT}`, (req, res) =>
          res.networkError('Network error'),
        ),
      );

      const { result } = renderHook(() => useMetrics());

      await expect(
        result.current.saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, metadata),
      ).rejects.toThrow('Failed to capture metric event.');
    });
  });

  describe('when user is not logged in (anonymous)', () => {
    beforeEach(() => {
      vi.mocked(useUser).mockReturnValue({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
        login: vi.fn(),
        logout: vi.fn(),
        updateUser: vi.fn(),
        clearError: vi.fn(),
      });
    });

    it('should post correct data to the metrics endpoint without userId', async () => {
      let requestBody;
      const mockHandler = vi.fn(async (req, res, ctx) => {
        requestBody = await req.json();
        return res(ctx.status(201), ctx.json({}));
      });

      server.use(
        rest.post(`http://localhost:8080${API_ENDPOINTS.RECORD_METRIC_EVENT}`, mockHandler),
      );

      const { result } = renderHook(() => useMetrics());

      const savePromise = result.current.saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, metadata);

      await expect(savePromise).resolves.toBeUndefined();
      expect(mockHandler).toHaveBeenCalledTimes(1);
      expect(requestBody).toEqual({
        event: METRIC_EVENT_TYPE.BUTTON_CLICK,
        eventMetadata: metadata,
      });
      expect(requestBody).not.toHaveProperty('userId');
    });

    it('should throw error when request fails for anonymous user', async () => {
      server.use(
        rest.post(`http://localhost:8080${API_ENDPOINTS.RECORD_METRIC_EVENT}`, (req, res, ctx) =>
          res(ctx.status(500)),
        ),
      );

      const { result } = renderHook(() => useMetrics());

      await expect(
        result.current.saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, metadata),
      ).rejects.toThrow('Failed to capture metric event.');
    });

    it('should throw error when network request fails for anonymous user', async () => {
      server.use(
        rest.post(`http://localhost:8080${API_ENDPOINTS.RECORD_METRIC_EVENT}`, (req, res) =>
          res.networkError('Network error'),
        ),
      );

      const { result } = renderHook(() => useMetrics());

      await expect(
        result.current.saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, metadata),
      ).rejects.toThrow('Failed to capture metric event.');
    });
  });

  describe('edge cases', () => {
    it('should handle user context returning null', async () => {
      vi.mocked(useUser).mockReturnValue({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
        login: vi.fn(),
        logout: vi.fn(),
        updateUser: vi.fn(),
        clearError: vi.fn(),
      });

      let requestBody;
      const mockHandler = vi.fn(async (req, res, ctx) => {
        requestBody = await req.json();
        return res(ctx.status(201), ctx.json({}));
      });

      server.use(
        rest.post(`http://localhost:8080${API_ENDPOINTS.RECORD_METRIC_EVENT}`, mockHandler),
      );

      const { result } = renderHook(() => useMetrics());

      await result.current.saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, metadata);

      expect(requestBody).toEqual({
        event: METRIC_EVENT_TYPE.BUTTON_CLICK,
        eventMetadata: metadata,
      });
      expect(requestBody).not.toHaveProperty('userId');
    });

    it('should handle user with missing id', async () => {
      vi.mocked(useUser).mockReturnValue({
        user: {
          email: 'test@example.com',
          name: 'Test User',
          firstName: 'Test',
          lastName: 'User',
          profilePicture: null,
        } as User,
        isAuthenticated: true,
        isLoading: false,
        error: null,
        login: vi.fn(),
        logout: vi.fn(),
        updateUser: vi.fn(),
        clearError: vi.fn(),
      });

      let requestBody;
      const mockHandler = vi.fn(async (req, res, ctx) => {
        requestBody = await req.json();
        return res(ctx.status(201), ctx.json({}));
      });

      server.use(
        rest.post(`http://localhost:8080${API_ENDPOINTS.RECORD_METRIC_EVENT}`, mockHandler),
      );

      const { result } = renderHook(() => useMetrics());

      await result.current.saveMetricEvent(METRIC_EVENT_TYPE.BUTTON_CLICK, metadata);

      expect(requestBody).toEqual({
        event: METRIC_EVENT_TYPE.BUTTON_CLICK,
        eventMetadata: metadata,
      });
      expect(requestBody).not.toHaveProperty('userId');
    });
  });
});
