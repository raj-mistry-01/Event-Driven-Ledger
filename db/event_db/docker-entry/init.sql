CREATE TABLE events (
    event_id           UUID PRIMARY KEY,
    wallet_id          UUID NOT NULL,
    event_type         INTEGER NOT NULL,
    event_payload      JSONB NULL,
    event_version      INTEGER NOT NULL,
    event_timestamp    TIMESTAMP NOT NULL DEFAULT NOW(),
    client_id          TEXT NOT NULL,
    client_request_id  TEXT NOT NULL
);

CREATE TABLE outbox (
    outbox_id      UUID PRIMARY KEY,
    event_id       UUID NOT NULL,
    wallet_id      UUID NOT NULL,
    event_version  INTEGER NOT NULL,
    event_type     INTEGER NOT NULL,
    event_payload  JSONB NULL,
    status         SMALLINT NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    published_at   TIMESTAMP NULL
);



CREATE TABLE snapshot (
    wallet_id           UUID PRIMARY KEY,
    snapshot_version    INTEGER NOT NULL,
    snapshot_state      JSONB NOT NULL,
    snapshot_timestamp  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE processed_commands (
    event_id           UUID NOT NULL,
    wallet_id 		   UUID NOT NULL,
    client_id          TEXT NOT NULL,
    client_request_id  TEXT NOT NULL,
    processed_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (client_id, client_request_id)
);

CREATE TABLE wallet_stream_head (
    wallet_id UUID PRIMARY KEY,
    last_version  INTEGER NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS reversed_transactions (
    original_transaction_id UUID PRIMARY KEY,
    reversal_event_id UUID NOT NULL UNIQUE,
    wallet_id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_original_transaction
        FOREIGN KEY (original_transaction_id)	
        REFERENCES events(event_id)
        ON DELETE RESTRICT
);

CREATE UNIQUE INDEX idx_events_wallet_version
ON events (wallet_id, event_version);

CREATE UNIQUE INDEX idx_events_idempotency
ON events (client_id, client_request_id);

CREATE INDEX idx_events_wallet
ON events (wallet_id);

CREATE INDEX idx_processed_commands_event
ON processed_commands (event_id);

CREATE INDEX idx_outbox_pending_order
ON outbox (status, wallet_id, event_version);

CREATE UNIQUE INDEX idx_events_event_id 
ON events(event_id);