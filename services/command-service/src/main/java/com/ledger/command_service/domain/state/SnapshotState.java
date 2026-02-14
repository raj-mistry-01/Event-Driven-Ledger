package com.ledger.command_service.domain.state;

import com.ledger.command_service.domain.enums.WalletStatus;

import java.math.BigDecimal;

public record SnapshotState(
        BigDecimal balance,
        WalletStatus status
) {

    public static SnapshotState initial() {
        return new SnapshotState(BigDecimal.ZERO, WalletStatus.CREATED);
    }

    public SnapshotState withBalance(BigDecimal newBalance) {
        return new SnapshotState(newBalance, this.status);
    }

    public SnapshotState withStatus(WalletStatus newStatus) {
        return new SnapshotState(this.balance, newStatus);
    }
}
