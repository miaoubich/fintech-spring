-- Sequences ensure safe, fast, pre-allocated ID generation so your thread pool never bottlenecks when saving trades.
-- Partial Indexes (WHERE processed = false) ensure your background scheduler can poll thousands of rows per second 
-- with zero latency and minimal CPU load on PostgreSQL.
--If your table has:
--
--5 million processed rows
--20 unprocessed rows
--Without an index, Postgres scans 5 million rows every 5 seconds just to find 20.
--
--With the partial index, Postgres jumps straight to those 20 rows.
--
--That is a massive performance difference in production.



CREATE SEQUENCE IF NOT EXISTS outbox_events_seq
    START WITH 1
    INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGINT PRIMARY KEY DEFAULT nextval('outbox_events_seq'),

    event_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,

    payload JSONB NOT NULL,

    created_at TIMESTAMPTZ NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMPTZ,

    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_outbox_events_processed
    ON outbox_events (processed, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_events_unprocessed
    ON outbox_events (created_at)
    WHERE processed = false;

CREATE INDEX IF NOT EXISTS idx_outbox_events_aggregate
    ON outbox_events (aggregate_id);
    
-- --- Drop tables for cleanup (if needed)
DROP TABLE public.outbox_events;
DROP SEQUENCE outbox_events_seq;

ALTER TABLE outbox_events
ADD COLUMN retry_count INT DEFAULT 0 NOT NULL;

ALTER TABLE outbox_events
ADD COLUMN last_error TEXT NULL;

-- 1. Create dedicated sequence for DLQ table
CREATE SEQUENCE outbox_dead_letter_seq START WITH 1 INCREMENT BY 50;

-- 2. Create DLQ table with proper types and constraints
CREATE TABLE outbox_dead_letter (
    id BIGINT PRIMARY KEY DEFAULT nextval('outbox_dead_letter_seq'),
    event_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    retry_count INT NOT NULL,
    last_error TEXT
);

-- 3. Create index for fast investigation queries by aggregateId
CREATE INDEX idx_outbox_dlq_aggregate_id ON outbox_dead_letter(aggregate_id);
CREATE INDEX idx_outbox_dlq_created_at ON outbox_dead_letter(created_at DESC);