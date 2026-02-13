package com.ledger.command_service.domain.event;

import java.util.UUID;

public record WalletCreated(
        UUID walletId
) {}
