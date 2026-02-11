package com.ledger.ledger_api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/commands")
public class CommandController {

    private final WebClient webClient;

    public CommandController(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @GetMapping("/wallets")
    public Mono<ResponseEntity<String>> testController() {
        return webClient.get()
                .uri("http://command-service/test/ping")
                .retrieve()
                .toEntity(String.class);
    }
}
