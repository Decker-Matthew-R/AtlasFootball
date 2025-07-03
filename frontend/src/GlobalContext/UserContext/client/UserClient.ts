import { API_ENDPOINTS } from '@/ApiEndpoints/API_ENDPOINTS';
import axiosInstance from '@/AxiosInstance/AxiosInstance';

export const logUserOut = async (): Promise<void> => {
  try {
    await axiosInstance.post(API_ENDPOINTS.LOG_OUT_EVENT).then((response) => response.status);
  } catch {
    throw new Error('Failed to Log User Out');
  }
};
