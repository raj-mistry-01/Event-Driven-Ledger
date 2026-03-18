package com.ledger.query_service.infrastructure.persistence;

import com.ledger.query_service.application.port.HistoryQueryRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JdbcHistoryQueryRepository implements HistoryQueryRepository {
    private final JdbcClient jdbcClient;

    public JdbcHistoryQueryRepository(
            @Qualifier("historyJdbcClient") JdbcClient jdbcClient
    ) {
        this.jdbcClient = jdbcClient;
    }

}

