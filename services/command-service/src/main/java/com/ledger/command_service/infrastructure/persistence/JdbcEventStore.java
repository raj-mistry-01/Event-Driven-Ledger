package com.ledger.command_service.infrastructure.persistence;

import com.ledger.command_service.application.exception.OptimisticLockException;
import com.ledger.command_service.application.port.AggregateEvent;
import com.ledger.command_service.application.port.EventStore;
import com.ledger.command_service.application.port.StoredEvent;
import org.apache.catalina.util.ToStringUtil;
import org.postgresql.util.PGobject;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcEventStore implements EventStore {

    private final JdbcClient jdbc;

    public JdbcEventStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<StoredEvent> loadEvents(UUID walletId) {
        return jdbc.sql("""
                SELECT event_id,
                       wallet_id,
                       event_type,
                       event_payload,
                       event_version,
                       event_timestamp,
                       client_id,
                       client_request_id
                FROM events
                WHERE wallet_id = ?
                ORDER BY event_version ASC
                """)
                .param(walletId)
                .query((rs, rowNum) -> new StoredEvent(
                        UUID.fromString(rs.getString("event_id")),
                        UUID.fromString(rs.getString("wallet_id")),
                        rs.getInt("event_type"),
                        rs.getString("event_payload"),
                        rs.getInt("event_version"),
                        rs.getTimestamp("event_timestamp").toInstant(),
                        rs.getString("client_id"),
                        rs.getString("client_request_id")
                ))
                .list();
    }

    @Override
    public List<AggregateEvent> loadEventsForSnapshot(UUID walletId, int version) {
        return jdbc.sql("""
                SELECT event_type,
                       event_payload
                FROM events
                WHERE wallet_id = ?
                  AND event_version > ?
                ORDER BY event_version ASC
                """)
                .params(walletId, version)
                .query((rs, rowNum) -> new AggregateEvent(
                        rs.getInt("event_type"),
                        rs.getString("event_payload")
                ))
                .list();
    }

    @Override
    @Transactional()
    public void appendEvents(
            UUID walletId,
            int expectedVersion,
            List<StoredEvent> newEvents
    ) {

        int currentVersion = readCurrentVersion(walletId);
        if (currentVersion != expectedVersion) {
            throw new OptimisticLockException(
                    ("version mismatch")
            );
        }

        int nextVersion = currentVersion;

        for (StoredEvent event : newEvents) {
            nextVersion++;


            try {
                jdbc.sql("""
            INSERT INTO events (
                event_id,
                wallet_id,
                event_type,
                event_payload,
                event_version,
                event_timestamp,
                client_id,
                client_request_id
            ) VALUES (?, ?, ?, ?::jsonb, ?, ?, ?, ?)
            """)
                        .params(
                                event.eventId(),
                                walletId,
                                event.eventType(),
                                event.eventPayload(),
                                nextVersion,
                                Timestamp.from(event.eventTimestamp()),
                                event.clientId(),
                                event.clientRequestId()
                        )
                        .update();

            }
            catch (DuplicateKeyException e){
                System.out.println("dup");
                throw new OptimisticLockException("version mismatch");
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to persist event", e);
            }
        }
        upsertStreamHead(walletId, nextVersion);
    }


    @Override
    public int readCurrentVersion(UUID walletId) {
        Integer version = jdbc.sql("""
                SELECT last_version
                FROM wallet_stream_head
                WHERE wallet_id = ?
                """)
                .param(walletId)
                .query(Integer.class)
                .optional()
                .orElse(null);

        return version == null ? 0 : version;
    }


    @Override
    public Optional<StoredEvent> findByEventId(UUID eventId) {

        return jdbc.sql("""
            SELECT event_id,
                   wallet_id,
                   event_type,
                   event_payload,
                   event_version,
                   event_timestamp,
                   client_id,
                   client_request_id
            FROM events
            WHERE event_id = ?
            """)
                .param(eventId)
                .query((rs, rowNum) -> new StoredEvent(
                        UUID.fromString(rs.getString("event_id")),
                        UUID.fromString(rs.getString("wallet_id")),
                        rs.getInt("event_type"),
                        rs.getString("event_payload"),
                        rs.getInt("event_version"),
                        rs.getTimestamp("event_timestamp").toInstant(),
                        rs.getString("client_id"),
                        rs.getString("client_request_id")
                ))
                .optional();
    }


    private int upsertStreamHead(UUID walletId, int newVersion) {
        jdbc.sql("""
                INSERT INTO wallet_stream_head (wallet_id, last_version, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT (wallet_id)
                DO UPDATE SET
                    last_version = EXCLUDED.last_version,
                    updated_at = EXCLUDED.updated_at
                """)
                .params(walletId, newVersion, Timestamp.from(Instant.now()))
                .update();
        return newVersion;
    }

}

