package com.ledger.command_service.domain.command;

import java.util.UUID;

public record SuspendWalletCommand(
        UUID walletId,
        String clientId,
        String clientRequestId
) {}
