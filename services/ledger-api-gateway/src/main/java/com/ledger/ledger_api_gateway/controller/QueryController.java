package com.ledger.ledger_api_gateway.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/queries")
public class QueryController {

    private final WebClient webClient;

    @Value("${spring.cloud.discovery.client.simple.instances.query-service[0].uri}")
    private String queryServiceUrl;

    @Value("${query-service.graphql-path:/graphql}")
    private String graphqlPath;

    public QueryController(WebClient.Builder builder) {
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

    private Mono<ResponseEntity<String>> callWebclientGet(String uri) {
        return webClient.get()
                .uri(uri)
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

    @GetMapping("/currentInfo/{id}")
    public Mono<ResponseEntity<String>> getWalletById(@PathVariable String id) {
        return callWebclientGet("http://query-service/wallet/currentInfo/" + id);
    }

    @GetMapping("/transactionInfo/{transactionId}")
    public Mono<ResponseEntity<String>> getTransactionInfo(@PathVariable String transactionId) {
        return callWebclientGet("http://query-service/wallet/transactionInfo/" + transactionId);
    }

    private String buildTransactionsUri(String walletId, Integer limit, String cursor, Integer type) {
        StringBuilder uri = new StringBuilder("http://query-service/wallet/");
        uri.append(walletId).append("/transactions");
        uri.append("?limit=").append(limit);
        if (cursor != null) {
            uri.append("&cursor=").append(URLEncoder.encode(cursor, StandardCharsets.UTF_8));
        }
        if (type != null) {
            uri.append("&type=").append(type);
        }
        return uri.toString();
    }

    @GetMapping("/{walletId}/transactions")
    public Mono<ResponseEntity<String>> getTransactionsByWalletId(
            @PathVariable String walletId,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer type
    ) {
        String uri = buildTransactionsUri(walletId, limit, cursor, type);
        return callWebclientGet(uri);
    }

    @GetMapping("/{walletId}/summary")
    public Mono<ResponseEntity<String>> getWalletSummary(@PathVariable String walletId) {
        return callWebclientGet("http://query-service/wallet/" + walletId + "/summary");
    }

    @PostMapping(
            value = "/graphql",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<String>> graphql(
            @RequestBody String requestBody) {

        return callWebclient(
                queryServiceUrl + graphqlPath,
                requestBody
        );
    }
}
