import { API_ENDPOINTS } from '@/ApiEndpoints/API_ENDPOINTS';
import axiosInstance from '@/AxiosInstance/AxiosInstance';
import { useUser } from '@/GlobalContext/UserContext/UserContext';
import { MetadataType } from '@/Metrics/model/MetadataType';
import { METRIC_EVENT_TYPE } from '@/Metrics/model/METRIC_EVENT_TYPE';
import { MetricEventType } from '@/Metrics/model/MetricEventType';

export const useMetrics = () => {
  const { user } = useUser();

  const saveMetricEvent = async (
    event: METRIC_EVENT_TYPE,
    eventMetadata: MetadataType,
  ): Promise<void> => {
    try {
      const metricEvent: MetricEventType = {
        event,
        eventMetadata,
        ...(user && { userId: user.id }),
      };

      await axiosInstance.post(API_ENDPOINTS.RECORD_METRIC_EVENT, metricEvent);
    } catch {
      throw new Error('Failed to capture metric event.');
    }
  };

  return { saveMetricEvent };
};
