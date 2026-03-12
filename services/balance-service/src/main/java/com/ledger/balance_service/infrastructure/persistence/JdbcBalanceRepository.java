package com.ledger.balance_service.infrastructure.persistence;

import com.ledger.balance_service.application.port.BalanceRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Repository
public class JdbcBalanceRepository implements BalanceRepository{

    private final JdbcClient jdbc;

    public JdbcBalanceRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void createWallet(UUID walletId) {

        jdbc.sql("""
                INSERT INTO wallet (
                    wallet_id,
                    balance,
                    status,
                    updated_at
                )
                VALUES (?, ?, ? , ?)
                ON CONFLICT (wallet_id) DO NOTHING
                """)
                .params(
                        walletId,
                        BigDecimal.ZERO,
                        0, // Assuming 0 is the default status for a new created wallet
                        Timestamp.from(Instant.now())
                )
                .update();
    }

    @Override
    public BigDecimal getBalance(UUID walletId) {

        BigDecimal balance = jdbc.sql("""
                SELECT balance
                FROM wallet
                WHERE wallet_id = ?
                """)
                .param(walletId)
                .query(BigDecimal.class)
                .optional()
                .orElse(null);

        if (balance == null) {
            throw new IllegalStateException("Wallet not found in projection: " + walletId);
        }

        return balance;
    }

    @Override
    public void updateBalance(UUID walletId, BigDecimal newBalance) {

        jdbc.sql("""
                UPDATE wallet
                SET balance = ?, updated_at = ?
                WHERE wallet_id = ?
                """)
                .params(
                        newBalance,
                        Timestamp.from(Instant.now()),
                        walletId
                )
                .update();
    }

    @Override
    public void updateStatus(UUID walletId, int status) {
        jdbc.sql("""
                UPDATE wallet
                SET status = ?, updated_at = ?
                WHERE wallet_id = ?
                """)
                .params(
                        status,
                        Timestamp.from(Instant.now()),
                        walletId
                )
                .update();
    }
}