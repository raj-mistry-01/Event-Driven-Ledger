package com.ledger.query_service.infrastructure.persistence;

import com.ledger.query_service.application.port.BalanceQueryRepository;
import com.ledger.query_service.application.dto.response.WalletCurrentInfoResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcBalanceQueryRepository implements BalanceQueryRepository {

    private final JdbcClient jdbcClient;

    public JdbcBalanceQueryRepository(
            @Qualifier("balanceJdbcClient") JdbcClient jdbcClient
    ) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<WalletCurrentInfoResponse> getWalletCurrentInfo(UUID walletId) {
        return jdbcClient
                .sql("""
            SELECT wallet_id, balance, status, updated_at
            FROM wallet
            WHERE wallet_id = :walletId
        """)
                .param("walletId", walletId)
                .query((rs, rowNum) -> new WalletCurrentInfoResponse(
                        rs.getObject("wallet_id", java.util.UUID.class),
                        rs.getBigDecimal("balance"),
                        rs.getString("status"),
                        rs.getString("updated_at")
                ))
                .optional();
    }
}
