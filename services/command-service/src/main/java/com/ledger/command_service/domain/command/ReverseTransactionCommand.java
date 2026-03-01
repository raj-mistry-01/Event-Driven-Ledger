package com.ledger.command_service.domain.command;

import java.util.UUID;

public record ReverseTransactionCommand(
        UUID walletId,
        UUID originalTransactionId,
        String clientId,
        String clientRequestId
) {
}
