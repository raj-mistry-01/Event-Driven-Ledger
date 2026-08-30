package com.ledger.balance_service.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record WalletEvent(

        @JsonProperty("outboxId")
        UUID outboxId,

        @JsonProperty("eventId")
        UUID eventId,

        @JsonProperty("walletId")
        UUID walletId,

        @JsonProperty("eventType")
        int eventType,

        @JsonProperty("eventVersion")
        int eventVersion,

        @JsonProperty("eventPayload")
        JsonNode eventPayload,

        @JsonProperty("createdAt")
        Long createdAt
) {}