package com.ledger.history_service.infrastructure.persistence;

import com.ledger.history_service.application.port.HistoryRepository;
import com.ledger.history_service.domain.model.WalletHistory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class JdbcHistoryRepository implements HistoryRepository {

    private final JdbcClient jdbc;

    public JdbcHistoryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void insert(WalletHistory history) {
        try {
            jdbc.sql("""
            INSERT INTO wallet_history (
                wallet_id,
                event_id,
                event_type,
                amount,
                reference_event_id,
                event_version,
                created_at
            ) VALUES (
                :walletId,
                :eventId,
                :eventType,
                :amount,
                :referenceEventId,
                :eventVersion,
                :createdAt
            )
        """)
                    .param("walletId", history.walletId())
                    .param("eventId", history.eventId())
                    .param("eventType", history.eventType())
                    .param("amount", history.amount())
                    .param("referenceEventId", history.referenceEventId())
                    .param("eventVersion", history.eventVersion())
                    .param("createdAt", Timestamp.from(history.createdAt()))
                    .update();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to insert wallet history.", ex);
        }
    }
}
