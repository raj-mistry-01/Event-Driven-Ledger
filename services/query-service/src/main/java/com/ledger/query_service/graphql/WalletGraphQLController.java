package com.ledger.query_service.graphql;

import com.ledger.query_service.application.dto.response.TransactionInformation;
import com.ledger.query_service.application.dto.response.WalletCurrentInfoResponse;
import com.ledger.query_service.application.dto.response.WalletSummary;
import com.ledger.query_service.application.dto.response.WalletTransactions;
import com.ledger.query_service.application.service.WalletQueryService;
import com.ledger.query_service.domain.enums.EventType;
import com.ledger.query_service.graphql.model.GraphQLWallet;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class WalletGraphQLController {

    private final WalletQueryService walletQueryService;

    public WalletGraphQLController(WalletQueryService walletQueryService) {
        this.walletQueryService = walletQueryService;
    }

    @QueryMapping
    public TransactionInformation transaction(@Argument UUID id) {
        return walletQueryService.getTransactionInfo(id);
    }

    @QueryMapping
    public GraphQLWallet wallet(@Argument UUID id) {

        WalletCurrentInfoResponse wallet =
                walletQueryService.getWalletCurrentInfo(id);

        return new GraphQLWallet(
                wallet.walletId(),
                wallet.currentBalance(),
                wallet.status(),
                wallet.lastUpdatedAt()
        );
    }

    @SchemaMapping(typeName = "Wallet", field = "transactions")
    public WalletTransactions transactions(
            GraphQLWallet wallet,
            @Argument Integer limit,
            @Argument String cursor,
            @Argument EventType type) {

        return walletQueryService.getWalletTransactions(
                wallet.walletId(),
                cursor,
                limit,
                type == null ? null : type.code()
        );
    }

    @SchemaMapping(typeName = "Wallet", field = "summary")
    public WalletSummary summary(GraphQLWallet wallet) {

        return walletQueryService.getWalletSummary(
                wallet.walletId()
        );
    }
}