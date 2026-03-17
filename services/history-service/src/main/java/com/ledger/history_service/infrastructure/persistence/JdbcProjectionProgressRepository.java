package com.ledger.history_service.infrastructure.persistence;

import com.ledger.history_service.application.port.ProjectionProgressRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class JdbcProjectionProgressRepository implements ProjectionProgressRepository {

    private final JdbcClient jdbc;

    public JdbcProjectionProgressRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int getLastProcessedVersion(UUID walletId) {

        Integer version = jdbc.sql("""
                SELECT last_processed_version
                FROM history_projection_progress
                WHERE wallet_id = ?
                """)
                .param(walletId)
                .query(Integer.class)
                .optional()
                .orElse(null);
        return version != null ? version : 0;
    }

    @Override
    public void updateLastProcessedVersion(UUID walletId, int version) {

        jdbc.sql("""
                INSERT INTO history_projection_progress (
                    wallet_id,
                    last_processed_version,
                    updated_at
                )
                VALUES (?, ?, ?)
                ON CONFLICT (wallet_id)
                DO UPDATE SET
                    last_processed_version = EXCLUDED.last_processed_version,
                    updated_at = EXCLUDED.updated_at
                """)
                .params(
                        walletId,
                        version,
                        Timestamp.from(Instant.now())
                )
                .update();
    }

}
