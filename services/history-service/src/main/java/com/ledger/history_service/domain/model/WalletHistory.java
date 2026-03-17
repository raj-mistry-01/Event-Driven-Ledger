package com.ledger.history_service.domain.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletHistory(

        Long historyId,
        UUID walletId,
        UUID eventId,
        int eventType,
        BigDecimal amount,
        UUID referenceEventId,
        int eventVersion,
        Instant createdAt

) {}
