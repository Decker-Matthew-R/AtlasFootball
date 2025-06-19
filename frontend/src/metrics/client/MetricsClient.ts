import { API_ENDPOINTS } from '@/ApiEndpoints/API_ENDPOINTS';
import axiosInstance from '@/AxiosInstance/AxiosInstance';
import { MetricEventType } from '@/metrics/model/MetricEventType';

export const saveMetricEvent = async (metricEvent: MetricEventType): Promise<void> => {
  try {
    await axiosInstance.post(API_ENDPOINTS.RECORD_METRIC_EVENT, metricEvent);
  } catch {
    throw new Error('Failed to capture metric event.');
  }
};
