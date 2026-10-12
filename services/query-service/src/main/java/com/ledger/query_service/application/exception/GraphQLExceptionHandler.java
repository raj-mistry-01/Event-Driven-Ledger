package com.ledger.query_service.application.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GraphQLExceptionHandler {

    @GraphQlExceptionHandler
    public GraphQLError handleWalletNotFound(
            WalletNotFoundException ex,
            GraphqlErrorBuilder<?> errorBuilder) {

        return errorBuilder
                .errorType(ErrorType.NOT_FOUND)
                .message(ex.getMessage())
                .extensions(java.util.Map.of(
                        "code", "WALLET_NOT_FOUND"
                ))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleTransactionNotFound(
            TransactionNotFoundException ex,
            GraphqlErrorBuilder<?> errorBuilder) {

        return errorBuilder
                .errorType(ErrorType.NOT_FOUND)
                .message(ex.getMessage())
                .extensions(java.util.Map.of(
                        "code", "TRANSACTION_NOT_FOUND"
                ))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleInvalidRequest(
            InvalidRequestException ex,
            GraphqlErrorBuilder<?> errorBuilder) {

        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getMessage())
                .extensions(java.util.Map.of(
                        "code", "INVALID_REQUEST"
                ))
                .build();
    }

    @GraphQlExceptionHandler
    public GraphQLError handleGeneric(
            Exception ex,
            GraphqlErrorBuilder<?> errorBuilder) {

        return errorBuilder
                .errorType(ErrorType.INTERNAL_ERROR)
                .message("An unexpected error occurred.")
                .extensions(java.util.Map.of(
                        "code", "INTERNAL_SERVER_ERROR"
                ))
                .build();
    }
}