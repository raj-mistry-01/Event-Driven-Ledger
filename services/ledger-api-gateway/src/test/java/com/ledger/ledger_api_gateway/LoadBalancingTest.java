package com.ledger.ledger_api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class LoadBalancingTest {

    @Autowired
    private WebClient.Builder builder;

    @Test
    void shouldDistributeRequestsAcrossInstances() {
        WebClient client = builder.build();

        Flux.range(1, 1000)
                .flatMap(i ->
                        client.get()
                                .uri("http://command-service/test/count")
                                .retrieve()
                                .bodyToMono(String.class),
                100   // concurrency level
                )
                .blockLast();
        Flux.range(1, 1000)
                .flatMap(i ->
                                client.get()
                                        .uri("http://query-service/test/count")
                                        .retrieve()
                                        .bodyToMono(String.class),
                        100   // concurrency level
                )
                .blockLast();
    }
}
