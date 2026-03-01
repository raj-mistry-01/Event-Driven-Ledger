package com.ledger.command_service.infrastructure.persistence;

import com.ledger.command_service.application.port.ReversalTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class JdbcReversalTracker implements ReversalTracker {

    private static final Logger log = LoggerFactory.getLogger(JdbcReversalTracker.class);
    private final JdbcClient jdbc;

    public JdbcReversalTracker(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }


    @Override
    public void recordReversal(
            UUID originalTransactionId,
            UUID reversalEventId,
            UUID walletId
    ) {

        try {
            jdbc.sql("""
                    INSERT INTO reversed_transactions (
                        original_transaction_id,
                        reversal_event_id,
                        wallet_id,
                        created_at
                    )
                    VALUES (?, ?, ?, ?)
                    """)
                    .params(originalTransactionId, reversalEventId, walletId , Timestamp.from(Instant.now()))
                    .update();
        } catch (Exception e) {
            log.error("Failed to record reversal: {}", e.getMessage(), e);
            throw e;
        }
    }
}