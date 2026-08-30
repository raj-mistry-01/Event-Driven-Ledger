package com.ledger.history_service.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ledger.history_service.infrastructure.config.MicrosecondInstantDeserializer;

import java.time.Instant;
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
        @JsonDeserialize(using = MicrosecondInstantDeserializer.class)
        Instant createdAt

) {}