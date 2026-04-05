package com.ledger.query_service.infrastructure.persistence;

import com.ledger.query_service.application.dto.response.TransactionBaseInfo;
import com.ledger.query_service.application.dto.response.TransactionInformation;
import com.ledger.query_service.application.dto.response.WalletTransactions;
import com.ledger.query_service.application.dto.response.WalletHistorySummary;
import com.ledger.query_service.application.port.HistoryQueryRepository;
import com.ledger.query_service.domain.enums.EventType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.sql.Timestamp;

@RestController
public class JdbcHistoryQueryRepository implements HistoryQueryRepository {
    private final JdbcClient jdbcClient;

    public JdbcHistoryQueryRepository(
            @Qualifier("historyJdbcClient") JdbcClient jdbcClient
    ) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<TransactionInformation> getTransactionInfo(UUID transactionId) {
        try {
            return jdbcClient.sql("""
                    SELECT wallet_id, event_id, event_type, amount, reference_event_id, created_at 
                    FROM wallet_history
                    WHERE event_id = ?
                    """)
                    .param(transactionId)
                    .query((rs, rowNum) -> new TransactionInformation(
                            rs.getObject("event_id", UUID.class),
                            rs.getObject("wallet_id", UUID.class),
                            EventType.fromCode(rs.getInt("event_type")).name(),
                            rs.getBigDecimal("amount"),
                            rs.getObject("reference_event_id", UUID.class),
                            rs.getTimestamp("created_at").toInstant()
                    ))
                    .optional();
        } catch (Exception ex) {
            System.out.println("Failed to fetch transaction info: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public WalletTransactions getWalletTransactions(UUID walletId, String cursor, Integer limit, Integer type) {
        Instant cursorTime = null;
        UUID cursorId = null;

        if (cursor != null && !cursor.isBlank()) {
            String[] parts = cursor.split("_");
            cursorTime = Instant.parse(parts[0]);
            cursorId = UUID.fromString(parts[1]);
        }


        String baseSql = """
                    SELECT
                        event_id,
                        wallet_id,
                        event_type,
                        amount,
                        reference_event_id,
                        created_at
                    FROM wallet_history
                    WHERE wallet_id = :walletId
                """;

        StringBuilder sql = new StringBuilder(baseSql);

        if (type != null) {
            sql.append(" AND event_type = :type ");
        }

        if (cursorTime != null && cursorId != null) {
            sql.append("""
                        AND (
                            created_at < :cursorTime
                            OR (created_at = :cursorTime AND event_id < :cursorId)
                        )
                    """);
        }

        sql.append(" ORDER BY created_at DESC, event_id DESC LIMIT :limit");

        int safeLimit = (limit == null || limit <= 0) ? 10 : limit;
        var query = jdbcClient.sql(sql.toString())
                .param("walletId", walletId)
                .param("limit", safeLimit);

        if (type != null) {
            query = query.param("type", type);
        }

        if (cursorTime != null && cursorId != null) {
            query = query
                    .param("cursorTime", Timestamp.from(cursorTime))
                    .param("cursorId", cursorId);
        }


        List<TransactionBaseInfo> transactions;
        try {
            transactions = query
                    .query((rs, rowNum) -> new TransactionBaseInfo(
                            rs.getObject("event_id", UUID.class),
                            EventType.fromCode(rs.getInt("event_type")).name(),
                            rs.getBigDecimal("amount"),
                            rs.getObject("reference_event_id", UUID.class),
                            rs.getTimestamp("created_at").toInstant()
                    ))
                    .list();
        } catch (Exception ex) {
            System.out.println("Failed to fetch transactions: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }

        String nextCursor = null;


        if (!transactions.isEmpty()) {
            TransactionBaseInfo last = transactions.get(transactions.size() - 1);

            nextCursor = last.createdAt().toString() + "_" + last.transactionId();
        }

        return new WalletTransactions(
                walletId,
                transactions,
                nextCursor
        );
    }

    @Override
    public WalletHistorySummary getWalletHistorySummary(UUID walletId) {
        return jdbcClient.sql("""
                SELECT
                    COALESCE(SUM(CASE WHEN event_type = :credit THEN amount ELSE 0 END), 0) AS total_credits,
                    COALESCE(SUM(CASE WHEN event_type = :debit THEN amount ELSE 0 END), 0) AS total_debits,
                    COALESCE(SUM(CASE WHEN event_type = :creditReversal THEN amount ELSE 0 END), 0) AS total_credit_reversals,
                    COALESCE(SUM(CASE WHEN event_type = :debitReversal THEN amount ELSE 0 END), 0) AS total_debit_reversals,
                    COUNT(*) AS transaction_count,
                    MAX(created_at) AS last_transaction_at
                FROM wallet_history
                WHERE wallet_id = :walletId
                """)
                .param("walletId", walletId)
                .param("credit", EventType.WALLET_CREDITED.code())
                .param("debit", EventType.WALLET_DEBITED.code())
                .param("creditReversal", EventType.CREDIT_REVERSED.code())
                .param("debitReversal", EventType.DEBIT_REVERSED.code())
                .query((rs, rowNum) -> new WalletHistorySummary(
                        rs.getBigDecimal("total_credits"),
                        rs.getBigDecimal("total_debits"),
                        rs.getBigDecimal("total_credit_reversals"),
                        rs.getBigDecimal("total_debit_reversals"),
                        rs.getInt("transaction_count"),
                        rs.getTimestamp("last_transaction_at") == null
                                ? null
                                : rs.getTimestamp("last_transaction_at").toInstant()
                ))
                .single();
    }

}
