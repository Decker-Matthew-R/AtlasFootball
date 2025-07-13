import { API_ENDPOINTS } from '@/ApiEndpoints/API_ENDPOINTS';
import axiosInstance from '@/AxiosInstance/AxiosInstance';
import { FixtureResponseDto } from '@/LandingPage/model/FixtureTypes';

export const useFixtures = () => {
  const getUpcomingFixtures = async (): Promise<FixtureResponseDto> => {
    try {
      const response = await axiosInstance.get<FixtureResponseDto>(
        API_ENDPOINTS.GET_ALL_UPCOMING_FIXTURES,
      );

      return response.data;
    } catch (error) {
      console.error('Failed to fetch upcoming fixtures:', error);
      throw new Error('Failed to fetch upcoming fixtures.');
    }
  };

  return {
    getUpcomingFixtures,
  };
};
