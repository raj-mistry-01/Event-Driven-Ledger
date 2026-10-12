package com.ledger.query_service.graphql;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.math.BigDecimal;

@Configuration
public class GraphQLScalarConfig {

    @Bean
    public RuntimeWiringConfigurer bigDecimalScalarConfigurer() {

        GraphQLScalarType bigDecimalScalar =
                GraphQLScalarType.newScalar()
                        .name("BigDecimal")
                        .description("Arbitrary precision decimal number")
                        .coercing(new Coercing<BigDecimal, String>() {

                            @Override
                            public String serialize(Object input) {
                                if (input instanceof BigDecimal value) {
                                    return value.toPlainString();
                                }

                                throw new IllegalArgumentException(
                                        "Expected BigDecimal but received: " + input
                                );
                            }

                            @Override
                            public BigDecimal parseValue(Object input) {
                                if (input instanceof String value) {
                                    return new BigDecimal(value);
                                }

                                if (input instanceof Number value) {
                                    return new BigDecimal(value.toString());
                                }

                                throw new IllegalArgumentException(
                                        "Expected a numeric value"
                                );
                            }

                            @Override
                            public BigDecimal parseLiteral(Object input) {
                                throw new UnsupportedOperationException(
                                        "Literal parsing not implemented"
                                );
                            }
                        })
                        .build();

        return wiringBuilder ->
                wiringBuilder.scalar(bigDecimalScalar);
    }
}