package com.ledger.query_service.infrastructure.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

import javax.sql.DataSource;

@Configuration
public class HistoryDataSourceConfig {

    @Bean
    @ConfigurationProperties("history.datasource")
    public DataSourceProperties historyDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource historyDataSource() {
        return historyDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate historyJdbcTemplate() {
        return new JdbcTemplate(historyDataSource());
    }

    @Bean
    public JdbcClient historyJdbcClient() {
        return JdbcClient.create(historyJdbcTemplate());
    }
}