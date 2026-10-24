package service

import (
	"context"
	"errors"
	"fmt"
	"math/big"
	"time"

	policyv1 "policy-service/api/proto/policyv1"
	"policy-service/internal/repository"
)

type PolicyRepository interface {
	GetAmountLimit(
		ctx context.Context,
		policyID string,
	) (repository.AmountLimitPolicy, error)

	GetTransactionFrequencyPolicy(
		ctx context.Context,
	) (repository.TransactionFrequencyPolicy, error)
}

type PolicyService struct {
	repo PolicyRepository
}

func classifyRepositoryError(err error) error {
	if errors.Is(err, repository.ErrPolicyNotFound) {
		return fmt.Errorf("%w: %v", ErrPolicyNotFound, err)
	}

	return err
}

func NewPolicyService(repo PolicyRepository) *PolicyService {
	return &PolicyService{repo: repo}
}

func (s *PolicyService) EvaluateAmount(
	ctx context.Context,
	req *policyv1.AmountLimitRequest,
) (bool, []string, error) {
	if req == nil {
		return false, nil, fmt.Errorf("%w: amount-limit request is required", ErrInvalidArgument)
	}

	var policyID string

	switch req.GetTransactionType() {
	case policyv1.TransactionType_CREDIT:
		policyID = repository.MaxCreditAmountPolicy
	case policyv1.TransactionType_DEBIT:
		policyID = repository.MaxDebitAmountPolicy
	default:
		return false, nil, fmt.Errorf("%w: unsupported transaction type", ErrInvalidArgument)
	}

	amount, ok := new(big.Rat).SetString(req.GetAmount())
	if !ok || amount.Sign() <= 0 {
		return false, nil, fmt.Errorf("%w: amount must be a positive decimal", ErrInvalidArgument)
	}

	policy, err := s.repo.GetAmountLimit(ctx, policyID)
	if err != nil {
		return false, nil, classifyRepositoryError(err)
	}

	if !policy.Enabled {
		return true, nil, nil
	}

	maxAmount, ok := new(big.Rat).SetString(policy.MaxAmount)
	if !ok || maxAmount.Sign() <= 0 {
		return false, nil, fmt.Errorf("%w: invalid maxAmount in policy %s", ErrInvalidPolicy, policyID)
	}

	if amount.Cmp(maxAmount) > 0 {
		return false, []string{
			fmt.Sprintf(
				"Requested amount exceeds the maximum permitted for %s",
				req.GetTransactionType().String(),
			),
		}, nil
	}

	return true, nil, nil
}

func (s *PolicyService) EvaluateTransactionFrequency(
	ctx context.Context,
	req *policyv1.TransactionFrequencyRequest,
) (bool, []string, error) {
	if req == nil {
		return false, nil, fmt.Errorf("%w: transaction-frequency request is required", ErrInvalidArgument)
	}

	switch req.GetTransactionType() {
	case policyv1.TransactionType_CREDIT,
		policyv1.TransactionType_DEBIT:
	default:
		return false, nil, fmt.Errorf("%w: unsupported transaction type", ErrInvalidArgument)
	}

	if req.GetRecentTotalTxns() < 0 {
		return false, nil, fmt.Errorf("%w: recent transaction count cannot be negative", ErrInvalidArgument)
	}

	if _, err := time.Parse(time.RFC3339Nano, req.GetEvaluationTime()); err != nil {
		return false, nil, fmt.Errorf("%w: evaluation_time must be RFC3339: %v", ErrInvalidArgument, err)
	}

	policy, err := s.repo.GetTransactionFrequencyPolicy(ctx)
	if err != nil {
		return false, nil, classifyRepositoryError(err)
	}

	if !policy.Enabled {
		return true, nil, nil
	}

	if policy.MaxTransactions <= 0 || policy.WindowSeconds <= 0 {
		return false, nil, fmt.Errorf("%w: invalid transaction-frequency policy configuration", ErrInvalidPolicy)
	}

	// The supplied count represents prior qualifying transactions
	// inside the configured window, before the current request.
	if req.GetRecentTotalTxns() >= policy.MaxTransactions {
		return false, []string{
			"Maximum wallet transaction frequency exceeded",
		}, nil
	}

	return true, nil, nil
}

func (s *PolicyService) GetTransactionFrequencyPolicy(
	ctx context.Context,
) (repository.TransactionFrequencyPolicy, error) {
	policy, err := s.repo.GetTransactionFrequencyPolicy(ctx)
	if err != nil {
		return repository.TransactionFrequencyPolicy{}, classifyRepositoryError(err)
	}

	if policy.Enabled &&
		(policy.MaxTransactions <= 0 || policy.WindowSeconds <= 0) {
		return repository.TransactionFrequencyPolicy{},
			fmt.Errorf("%w: invalid transaction-frequency policy configuration", ErrInvalidPolicy)
	}

	return policy, nil
}
