package com.ledger.command_service.infrastructure.persistence;

import com.ledger.command_service.application.port.OutboxEvent;
import com.ledger.command_service.application.port.OutboxStore;
import com.ledger.command_service.application.port.StoredEvent;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class JdbcOutboxStore implements OutboxStore {

    private final JdbcClient jdbc;

    public JdbcOutboxStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(List<StoredEvent> events) {
        for (StoredEvent event : events) {
            try {
                jdbc.sql("""
                                INSERT INTO outbox (
                                    outbox_id,
                                    event_id,
                                    event_type,
                                    event_payload,
                                    event_version,
                                    wallet_id,
                                    status,
                                    created_at,
                                    published_at
                                ) VALUES (?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?)
                                """)
                        .params(
                                UUID.randomUUID(),       // outbox_id
                                event.eventId(),                // event_id
                                event.eventType(),              // event_type
                                event.eventPayload(),           // event_payload (json)
                                event.eventVersion(),           // event_version
                                event.walletId(),               // wallet_id
                                0,                              // status = PENDING
                                Timestamp.from(Instant.now()),  // created_at
                                null                            // published_at
                        )
                        .update();
            } catch (Exception e) {
                throw new RuntimeException("Failed to save outbox event", e);
            }
        }
    }

    @Override
    public List<OutboxEvent> findPending(int batchSize) {
        return jdbc.sql("""
            SELECT
                outbox_id,
                event_id,
                event_type,
                event_payload,
                event_version,
                wallet_id,
                created_at
            FROM outbox
            WHERE status = 0
            ORDER BY created_at
            LIMIT ?
            """)
                .param(batchSize)
                .query((rs, rowNum) -> new OutboxEvent(
                        rs.getObject("outbox_id", UUID.class),
                        rs.getObject("event_id", UUID.class),
                        rs.getInt("event_type"),
                        rs.getString("event_payload"),
                        rs.getInt("event_version"),
                        rs.getObject("wallet_id", UUID.class),
                        rs.getTimestamp("created_at").toInstant()
                ))
                .list();
    }

    @Override
    public void markAsPublished(UUID outboxId) {
        jdbc.sql("""
            UPDATE outbox
            SET status = 1,
                published_at = ?
            WHERE outbox_id = ?
            """)
                .params(
                        Timestamp.from(Instant.now()),
                        outboxId
                )
                .update();
    }
}
