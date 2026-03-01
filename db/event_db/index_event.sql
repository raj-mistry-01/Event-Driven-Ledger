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
