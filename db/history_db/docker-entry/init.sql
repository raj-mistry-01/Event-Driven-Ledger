CREATE TABLE wallet_history (
    history_id         BIGSERIAL PRIMARY KEY,
    wallet_id          UUID NOT NULL,
    event_id 		   UUID NOT NULL,
    event_version      INTEGER NOT NULL,
    event_type         INTEGER NOT NULL,
    amount             NUMERIC(18,2) NULL,
    reference_event_id UUID NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE history_projection_progress (
    wallet_id UUID PRIMARY KEY,
    last_processed_version  INTEGER NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_history_wallet_version
ON wallet_history (wallet_id, event_version);

CREATE INDEX idx_wallet_history_wallet
ON wallet_history (wallet_id);

CREATE INDEX idx_wallet_history_created
ON wallet_history (wallet_id, created_at DESC);
