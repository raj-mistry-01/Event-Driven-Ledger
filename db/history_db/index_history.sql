CREATE INDEX idx_wallet_history_wallet
ON wallet_history (wallet_id);

CREATE INDEX idx_wallet_history_created
ON wallet_history (wallet_id, created_at DESC);