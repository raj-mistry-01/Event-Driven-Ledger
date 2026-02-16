package com.ledger.command_service.domain.command;

import java.util.UUID;

public record WalletLifeCycleCommand(
        UUID walletId,
        String clientId,
        String clientRequestId
) {}
