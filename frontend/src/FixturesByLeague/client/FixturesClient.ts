import { API_ENDPOINTS } from '@/ApiEndpoints/API_ENDPOINTS';
import axiosInstance from '@/AxiosInstance/AxiosInstance';
import { FixtureResponseDto, FixtureDto } from '@/FixturesByLeague/types/FixtureTypes';

export const useFixtures = () => {
  const fetchFixtures = async (): Promise<FixtureDto[]> => {
    const response = await axiosInstance.get<FixtureResponseDto>(
      API_ENDPOINTS.GET_UPCOMING_FIXTURES,
    );

    if (response.data.status === 'success' || response.data.fixtures) {
      return response.data.fixtures || [];
    } else {
      throw new Error(response.data.message || 'Failed to fetch fixtures');
    }
  };

  return { fetchFixtures };
};
