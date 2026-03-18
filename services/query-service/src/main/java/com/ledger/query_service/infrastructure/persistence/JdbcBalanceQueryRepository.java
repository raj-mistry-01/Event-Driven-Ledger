package com.ledger.query_service.infrastructure.persistence;

import com.ledger.query_service.application.port.BalanceQueryRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcBalanceQueryRepository implements BalanceQueryRepository {

    private final JdbcClient jdbcClient;

    public JdbcBalanceQueryRepository(
            @Qualifier("balanceJdbcClient") JdbcClient jdbcClient
    ) {
        this.jdbcClient = jdbcClient;
    }


}
