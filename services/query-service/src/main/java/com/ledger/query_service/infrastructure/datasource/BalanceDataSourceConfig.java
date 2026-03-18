package com.ledger.query_service.infrastructure.datasource;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;

@Configuration
public class BalanceDataSourceConfig {

    @Bean
    @ConfigurationProperties("balance.datasource")
    public DataSourceProperties balanceDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource balanceDataSource() {
        return balanceDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate balanceJdbcTemplate() {
        return new JdbcTemplate(balanceDataSource());
    }

    @Bean
    public JdbcClient balanceJdbcClient() {
        return JdbcClient.create(balanceJdbcTemplate());
    }
}
