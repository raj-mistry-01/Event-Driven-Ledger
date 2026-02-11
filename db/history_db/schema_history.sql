CREATE DATABASE history_db;

CREATE TABLE wallet_history (
    history_id         BIGSERIAL PRIMARY KEY,
    wallet_id          UUID NOT NULL,
    event_id 		   UUID NOT NULL,
    event_type         INTEGER NOT NULL,
    amount             NUMERIC(18,2) NOT NULL,
    reference_event_id UUID NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE history_projection_progress (
    wallet_id UUID PRIMARY KEY,
    last_version  INTEGER NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);