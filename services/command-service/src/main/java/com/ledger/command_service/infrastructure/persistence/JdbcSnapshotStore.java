package com.ledger.command_service.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledger.command_service.application.port.SnapshotStore;
import com.ledger.command_service.domain.state.SnapshotState;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.UUID;


@Repository
public class JdbcSnapshotStore implements SnapshotStore {

    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;

    public JdbcSnapshotStore(JdbcClient jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<SnapshotWithVersion> load(UUID walletId) {
        try {
            return jdbc.sql("""
                    SELECT wallet_id,
                           snapshot_version,
                           snapshot_state
                    FROM snapshot
                    WHERE wallet_id = ?
                    """)
                    .param(walletId)
                    .query((rs, rowNum) -> {
                        try {
                            SnapshotState state = objectMapper.readValue(
                                    rs.getString("snapshot_state"),
                                    SnapshotState.class
                            );
                            return new SnapshotWithVersion(
                                    UUID.fromString(rs.getString("wallet_id")),
                                    rs.getInt("snapshot_version"),
                                    state
                            );
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to deserialize snapshot state", e);
                        }
                    })
                    .optional();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load snapshot", e);
        }
    }

    @Override
    public void upsert(UUID walletId, int version, SnapshotState state) {
        try {
//            System.out.println(version);
            String jsonState = objectMapper.writeValueAsString(state);
//            System.out.println(state);
//
//            System.out.println("snapshot triggered");

            jdbc.sql("""
                    INSERT INTO snapshot (
                        wallet_id,
                        snapshot_version,
                        snapshot_state,
                        snapshot_timestamp
                    )
                    VALUES (?, ?, ?::jsonb, now())
                    ON CONFLICT (wallet_id)
                    DO UPDATE SET
                        snapshot_version   = EXCLUDED.snapshot_version,
                        snapshot_state     = EXCLUDED.snapshot_state,
                        snapshot_timestamp = now()
                    """)
                    .params(walletId, version, jsonState)
                    .update();

        } catch (Exception e) {
            throw new RuntimeException("Failed to persist snapshot", e);
        }
    }
}

