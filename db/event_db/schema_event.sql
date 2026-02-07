CREATE DATABASE event_db;

CREATE TABLE events (
    event_id           UUID PRIMARY KEY,
    wallet_id          UUID NOT NULL,
    event_type         INTEGER NOT NULL,
    event_payload      JSONB NOT NULL,
    event_version      INTEGER NOT NULL,
    event_timestamp    TIMESTAMP NOT NULL DEFAULT NOW(),
    client_id          TEXT NOT NULL,
    client_request_id  TEXT NOT NULL
);


CREATE TABLE snapshot (
    wallet_id           UUID PRIMARY KEY,
    snapshot_version    INTEGER NOT NULL,
    snapshot_state      JSONB NOT NULL,
    snapshot_timestamp  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE processed_commands (
    event_id           UUID NOT NULL,
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