CREATE TABLE wallet (
    wallet_id   UUID PRIMARY KEY,
    balance     NUMERIC(18,2) NOT NULL,
    status      INTEGER NOT NULL,
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);


CREATE TABLE balance_projection_progress (
    wallet_id UUID PRIMARY KEY,
    last_processed_version  INTEGER NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

