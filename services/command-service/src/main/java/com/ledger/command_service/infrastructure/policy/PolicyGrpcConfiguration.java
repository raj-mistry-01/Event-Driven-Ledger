package com.ledger.command_service.infrastructure.policy;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.AbstractStub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PolicyGrpcConfiguration {

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel policyGrpcChannel(
            @Value("${policy.grpc.host:localhost}") String host,
            @Value("${policy.grpc.port:9090}") int port) {

        return ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
    }
}
