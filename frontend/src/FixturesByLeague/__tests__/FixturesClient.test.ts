import { renderHook } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';

import { API_ENDPOINTS } from '@/ApiEndpoints/API_ENDPOINTS';
import axiosInstance from '@/AxiosInstance/AxiosInstance';
import { useFixtures } from '@/FixturesByLeague/client/FixturesClient';
import { FixtureResponseDto, FixtureDto } from '@/FixturesByLeague/types/FixtureTypes';

vi.mock('@/AxiosInstance/AxiosInstance', () => ({
  default: {
    get: vi.fn(),
  },
}));

vi.mock('@/ApiEndpoints/API_ENDPOINTS', () => ({
  API_ENDPOINTS: {
    GET_UPCOMING_FIXTURES: 'api/fixtures/upcoming',
  },
}));

const mockedAxiosInstance = axiosInstance as unknown as {
  get: ReturnType<typeof vi.fn>;
};

const mockFixtureDto: FixtureDto = {
  fixture: {
    id: 1,
    date: '2024-01-01T15:00:00Z',
    timezone: 'UTC',
    venue: { id: 1, name: 'Stadium', city: 'City' },
    status: { long: 'Not Started', short: 'NS', elapsed: 0 },
  },
  league: {
    id: 1,
    name: 'Premier League',
    country: 'England',
    logo: 'logo-url',
    season: 2024,
    round: 'Regular Season - 1',
  },
  teams: {
    home: { id: 1, name: 'Team A', logo: 'logo-a', winner: null },
    away: { id: 2, name: 'Team B', logo: 'logo-b', winner: null },
  },
  goals: { home: null, away: null },
  score: {
    halftime: { home: null, away: null },
    fulltime: { home: null, away: null },
    extratime: null,
    penalty: null,
  },
};

describe('fetchFixtures', () => {
  it('should return fixtures when response has success status', async () => {
    const mockResponse: FixtureResponseDto = {
      results: 1,
      fixtures: [mockFixtureDto],
      status: 'success',
      message: 'Success',
    };

    mockedAxiosInstance.get.mockResolvedValue({ data: mockResponse });

    const { result } = renderHook(() => useFixtures());
    const fixtures = await result.current.fetchFixtures();

    expect(mockedAxiosInstance.get).toHaveBeenCalledWith(API_ENDPOINTS.GET_UPCOMING_FIXTURES);
    expect(fixtures).toEqual([mockFixtureDto]);
  });

  it('should return fixtures when response has fixtures but no success status', async () => {
    const mockResponse: FixtureResponseDto = {
      results: 1,
      fixtures: [mockFixtureDto],
      status: 'error',
      message: 'Some error',
    };

    mockedAxiosInstance.get.mockResolvedValue({ data: mockResponse });

    const { result } = renderHook(() => useFixtures());
    const fixtures = await result.current.fetchFixtures();

    expect(fixtures).toEqual([mockFixtureDto]);
  });

  it('should return empty array when fixtures is undefined but condition passes', async () => {
    const mockResponse: FixtureResponseDto = {
      results: 0,
      fixtures: undefined as unknown as FixtureDto[],
      status: 'success',
      message: 'Success',
    };

    mockedAxiosInstance.get.mockResolvedValue({ data: mockResponse });

    const { result } = renderHook(() => useFixtures());
    const fixtures = await result.current.fetchFixtures();

    expect(fixtures).toEqual([]);
  });

  it('should throw error with custom message when response has no fixtures and failed status', async () => {
    const mockResponse: FixtureResponseDto = {
      results: 0,
      fixtures: null as unknown as FixtureDto[],
      status: 'error',
      message: 'Custom error message',
    };

    mockedAxiosInstance.get.mockResolvedValue({ data: mockResponse });

    const { result } = renderHook(() => useFixtures());
    await expect(result.current.fetchFixtures()).rejects.toThrow('Custom error message');
  });

  it('should return empty array when response has empty fixtures array', async () => {
    const mockResponse: FixtureResponseDto = {
      results: 0,
      fixtures: [],
      status: 'error',
      message: 'Custom error message',
    };

    mockedAxiosInstance.get.mockResolvedValue({ data: mockResponse });

    const { result } = renderHook(() => useFixtures());
    const fixtures = await result.current.fetchFixtures();

    expect(fixtures).toEqual([]);
  });

  it('should throw default error message when response has no fixtures and no message', async () => {
    const mockResponse: FixtureResponseDto = {
      results: 0,
      fixtures: null as unknown as FixtureDto[],
      status: 'error',
      message: '',
    };

    mockedAxiosInstance.get.mockResolvedValue({ data: mockResponse });

    const { result } = renderHook(() => useFixtures());
    await expect(result.current.fetchFixtures()).rejects.toThrow('Failed to fetch fixtures');
  });

  it('should throw default error message when response has no fixtures and message is undefined', async () => {
    const mockResponse: FixtureResponseDto = {
      results: 0,
      fixtures: undefined as unknown as FixtureDto[],
      status: 'error',
      message: undefined as unknown as string,
    };

    mockedAxiosInstance.get.mockResolvedValue({ data: mockResponse });

    const { result } = renderHook(() => useFixtures());
    await expect(result.current.fetchFixtures()).rejects.toThrow('Failed to fetch fixtures');
  });

  it('should propagate axios errors', async () => {
    const axiosError = new Error('Network error');
    mockedAxiosInstance.get.mockRejectedValue(axiosError);

    const { result } = renderHook(() => useFixtures());
    await expect(result.current.fetchFixtures()).rejects.toThrow('Network error');
  });
});
