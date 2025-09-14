import { MetadataType } from '@/Metrics/model/MetadataType';
import { METRIC_EVENT_TYPE } from '@/Metrics/model/METRIC_EVENT_TYPE';

export type MetricEventType = {
  event: METRIC_EVENT_TYPE;
  eventMetadata: MetadataType;
  userId?: number;
};
