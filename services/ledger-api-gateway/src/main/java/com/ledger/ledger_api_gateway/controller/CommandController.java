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

    public Mono<ResponseEntity<String>> callWebclient(String path, String requestBody) {
        return webClient.post()
                .uri(path)
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

    @PostMapping(path = "/wallet/create")
    public Mono<ResponseEntity<String>> createController(@RequestBody String requestBody) {
        return callWebclient("http://command-service/wallet/create", requestBody);
    }

    @PostMapping("/wallet/activate")
    public Mono<ResponseEntity<String>> activateController(@RequestBody String requestBody) {
        return callWebclient("http://command-service/wallet/activate", requestBody);
    }

    @PostMapping("/wallet/suspend")
    public Mono<ResponseEntity<String>> suspendController(@RequestBody String requestBody) {
        return callWebclient("http://command-service/wallet/suspend", requestBody);
    }

    @PostMapping("/wallet/close")
    public Mono<ResponseEntity<String>> closeController(@RequestBody String requestBody) {
        return callWebclient("http://command-service/wallet/close", requestBody);
    }

    @PostMapping("/wallet/credit")
    public Mono<ResponseEntity<String>> creditController(@RequestBody String requestBody) {
        return callWebclient("http://command-service/wallet/credit", requestBody);
    }

    @PostMapping("/wallet/debit")
    public Mono<ResponseEntity<String>> debitController(@RequestBody String requestBody) {
        return callWebclient("http://command-service/wallet/debit", requestBody);
    }

    @PostMapping("/wallet/reverse")
    public Mono<ResponseEntity<String>> reverseController(@RequestBody String requestBody) {
        return callWebclient("http://command-service/wallet/reverse", requestBody);
    }
}
