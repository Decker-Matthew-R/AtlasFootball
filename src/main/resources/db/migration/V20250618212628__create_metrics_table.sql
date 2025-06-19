CREATE TABLE if not exists metrics
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    event       VARCHAR(100) NOT NULL,
    event_time  timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata    JSONB NOT NULL,
    CONSTRAINT METRIC_EVENT_PK PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_metrics_event_time ON metrics(event_time);
CREATE INDEX IF NOT EXISTS idx_metrics_event ON metrics(event);
