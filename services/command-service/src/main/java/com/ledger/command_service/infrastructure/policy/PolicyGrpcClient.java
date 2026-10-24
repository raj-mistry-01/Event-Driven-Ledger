package com.ledger.command_service.infrastructure.policy;

import com.ledger.command_service.application.exception.PolicyGrpcException;
import com.ledger.policy.v1.EvaluateCommandRequest;
import com.ledger.policy.v1.EvaluateCommandResponse;
import com.ledger.policy.v1.GetTransactionFrequencyPolicyRequest;
import com.ledger.policy.v1.GetTransactionFrequencyPolicyResponse;
import com.ledger.policy.v1.PolicyServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PolicyGrpcClient {

    private static final long DEADLINE_SECONDS = 2;

    private final PolicyServiceGrpc.PolicyServiceBlockingStub stub;

    public PolicyGrpcClient(ManagedChannel policyGrpcChannel) {
        this.stub = PolicyServiceGrpc
                .newBlockingStub(policyGrpcChannel);
    }

    public GetTransactionFrequencyPolicyResponse
    getTransactionFrequencyPolicy() {

        try {
            return stub
                    .withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .getTransactionFrequencyPolicy(
                            GetTransactionFrequencyPolicyRequest
                                    .getDefaultInstance()
                    );

        } catch (StatusRuntimeException ex) {
            throw translateException(ex);
        }
    }

    public EvaluateCommandResponse evaluateCommand(
            EvaluateCommandRequest request) {

        try {
            return stub
                    .withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .evaluateCommand(request);

        } catch (StatusRuntimeException ex) {
            throw translateException(ex);
        }
    }

    private RuntimeException translateException(
            StatusRuntimeException ex) {

        Status.Code code = ex.getStatus().getCode();

        return switch (code) {
            case INVALID_ARGUMENT ->
                    new IllegalArgumentException(
                            "Invalid policy request", ex);

            case DEADLINE_EXCEEDED ->
                    new PolicyGrpcException(
                            "Policy service request timed out", ex);

            case UNAVAILABLE, CANCELLED ->
                    new PolicyGrpcException(
                            "Policy service is unavailable", ex);

            default ->
                    new PolicyGrpcException(
                            "Policy service request failed: " + code, ex);
        };
    }
}
