package com.ledger.ledger_api_gateway.controller;

import org.springframework.http.MediaType;
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

    @PostMapping(path = "/wallet/create")
    public Mono<ResponseEntity<String>> createController(@RequestBody String requestBody) {
        return webClient.post()
                .uri("http://command-service/wallet/create")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .toEntity(String.class);
    }

    @PostMapping("/wallet/suspend")
    public Mono<ResponseEntity<String>> suspendController(@RequestBody String requestBody) {

        return webClient.post()
                .uri("http://command-service/wallet/suspend")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchangeToMono(response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .map(body ->
                                        ResponseEntity
                                                .status(response.statusCode())
                                                .headers(response.headers().asHttpHeaders())
                                                .body(body)
                                )
                );
    }

}
