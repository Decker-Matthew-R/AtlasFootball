import { rest } from 'msw';
import { describe, expect, vi } from 'vitest';

import { API_ENDPOINTS } from '@/ApiEndpoints/API_ENDPOINTS';
import { saveMetricEvent } from '@/metrics/client/MetricsClient';
import { MetadataType } from '@/metrics/model/MetadataType';
import { METRIC_EVENT_TYPE } from '@/metrics/model/METRIC_EVENT_TYPE';
import { MetricEventType } from '@/metrics/model/MetricEventType';
import { server } from '@/setupTests';

describe('Metrics Client', () => {
  const metadata: MetadataType = { triggerId: 'React Button', screen: 'Home' };

  const metricEventType: MetricEventType = {
    event: METRIC_EVENT_TYPE.BUTTON_CLICK,
    eventMetadata: metadata,
  };

  it('should post correct data to the metrics endpoint', async () => {
    let requestBody;
    const mockHandler = vi.fn(async (req, res, ctx) => {
      requestBody = await req.json();
      return res(ctx.status(201), ctx.json({}));
    });

    server.use(rest.post(API_ENDPOINTS.RECORD_METRIC_EVENT, mockHandler));

    const result = saveMetricEvent(metricEventType);

    await expect(result).resolves.toBeUndefined();
    expect(mockHandler).toHaveBeenCalledTimes(1);
    expect(requestBody).toEqual(metricEventType);
  });

  it('should throw error when request fails', async () => {
    server.use(
      rest.post(API_ENDPOINTS.RECORD_METRIC_EVENT, (req, res, ctx) => res(ctx.status(500))),
    );

    await expect(saveMetricEvent(metricEventType)).rejects.toThrow(
      'Failed to capture metric event.',
    );
  });

  it('should throw error when network request fails', async () => {
    server.use(
      rest.post(API_ENDPOINTS.RECORD_METRIC_EVENT, (req, res) => res.networkError('Network error')),
    );

    await expect(saveMetricEvent(metricEventType)).rejects.toThrow(
      'Failed to capture metric event.',
    );
  });
});
