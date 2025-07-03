import { MetadataType } from '@/metrics/model/MetadataType';
import { METRIC_EVENT_TYPE } from '@/metrics/model/METRIC_EVENT_TYPE';

export type MetricEventType = {
  event: METRIC_EVENT_TYPE;
  eventMetadata: MetadataType;
  userId?: number;
};
