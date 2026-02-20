package com.ledger.command_service.infrastructure.persistence;

import com.ledger.command_service.application.port.ProcessedCommand;
import com.ledger.command_service.application.port.ProcessedCommandStore;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcProcessedCommandStore implements ProcessedCommandStore {

    private final JdbcClient jdbc;

    public JdbcProcessedCommandStore(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<ProcessedCommand> find(
            String clientId,
            String clientRequestId
    ) {
        try {
            return jdbc.sql("""
                    SELECT wallet_id, event_id
                    FROM processed_commands
                    WHERE client_id = ? AND client_request_id = ?
                    """)
                    .params(clientId, clientRequestId)
                    .query((rs, rowNum) ->
                            new ProcessedCommand(
                                    UUID.fromString(rs.getString("wallet_id")),
                                    UUID.fromString(rs.getString("event_id"))
                            )
                    )
                    .optional();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find processed command", e);
        }
    }

    @Override
    public void markProcessed(
            String clientId,
            String clientRequestId,
            UUID walletId,
            UUID eventId
    ) {
        try {
            jdbc.sql("""
                    INSERT INTO processed_commands (
                        client_id,
                        client_request_id,
                        wallet_id,
                        event_id,
                        processed_at
                    ) VALUES (?, ?, ?, ?, ?)
                    """)
                    .params(
                            clientId,
                            clientRequestId,
                            walletId,
                            eventId,
                            Timestamp.from(Instant.now())
                    )
                    .update();
        } catch (Exception e) {
            throw new RuntimeException("Failed to mark command as processed", e);
        }
    }
}
