package handler

import (
	"context"
	"errors"

	policyv1 "policy-service/api/proto/policyv1"
	policyservice "policy-service/internal/service"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type PolicyHandler struct {
	policyv1.UnimplementedPolicyServiceServer
	service *policyservice.PolicyService
}

func NewPolicyHandler(
	service *policyservice.PolicyService,
) *PolicyHandler {
	return &PolicyHandler{service: service}
}

func (h *PolicyHandler) EvaluateCommand(
	ctx context.Context,
	req *policyv1.EvaluateCommandRequest,
) (*policyv1.EvaluateCommandResponse, error) {
	if req == nil || req.GetPolicyContext() == nil {
		return nil, status.Error(
			codes.InvalidArgument,
			"a policy context is required",
		)
	}

	var (
		allowed bool
		reasons []string
		err     error
	)

	switch request := req.GetPolicyContext().(type) {
	case *policyv1.EvaluateCommandRequest_AmountLimit:
		allowed, reasons, err = h.service.EvaluateAmount(
			ctx, request.AmountLimit,
		)

	case *policyv1.EvaluateCommandRequest_TransactionFrequency:
		allowed, reasons, err = h.service.EvaluateTransactionFrequency(
			ctx, request.TransactionFrequency,
		)

	default:
		return nil, status.Error(
			codes.InvalidArgument,
			"unsupported policy context",
		)
	}

	if err != nil {
		switch {
		case errors.Is(err, context.Canceled):
			return nil, status.Error(
				codes.Canceled,
				"policy evaluation canceled",
			)

		case errors.Is(err, context.DeadlineExceeded):
			return nil, status.Error(
				codes.DeadlineExceeded,
				"policy evaluation timed out",
			)

		case errors.Is(err, policyservice.ErrInvalidArgument):
			return nil, status.Error(
				codes.InvalidArgument,
				err.Error(),
			)

		case errors.Is(err, policyservice.ErrPolicyNotFound),
			errors.Is(err, policyservice.ErrInvalidPolicy):
			return nil, status.Error(
				codes.Internal,
				"policy configuration error",
			)

		default:
			return nil, status.Error(
				codes.Unavailable,
				"policy evaluation could not be completed",
			)
		}
	}

	return &policyv1.EvaluateCommandResponse{
		Allowed:       allowed,
		DenialReasons: reasons,
	}, nil
}

func (h *PolicyHandler) GetTransactionFrequencyPolicy(
	ctx context.Context,
	req *policyv1.GetTransactionFrequencyPolicyRequest,
) (*policyv1.GetTransactionFrequencyPolicyResponse, error) {

	policy, err := h.service.GetTransactionFrequencyPolicy(ctx)
	if err != nil {
		switch {
		case errors.Is(err, context.Canceled):
			return nil, status.Error(
				codes.Canceled,
				"request canceled",
			)

		case errors.Is(err, context.DeadlineExceeded):
			return nil, status.Error(
				codes.DeadlineExceeded,
				"request timed out",
			)

		case errors.Is(err, policyservice.ErrInvalidArgument):
			return nil, status.Error(
				codes.InvalidArgument,
				err.Error(),
			)

		case errors.Is(err, policyservice.ErrPolicyNotFound),
			errors.Is(err, policyservice.ErrInvalidPolicy):
			return nil, status.Error(
				codes.Internal,
				"policy configuration error",
			)

		default:
			return nil, status.Error(
				codes.Unavailable,
				"could not retrieve policy configuration",
			)
		}
	}

	return &policyv1.GetTransactionFrequencyPolicyResponse{
		Enabled:         policy.Enabled,
		MaxTransactions: policy.MaxTransactions,
		WindowSeconds:   policy.WindowSeconds,
	}, nil
}
