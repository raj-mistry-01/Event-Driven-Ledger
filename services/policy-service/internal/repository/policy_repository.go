package repository

import (
	"context"
	"errors"
	"fmt"

	"go.mongodb.org/mongo-driver/v2/bson"
	"go.mongodb.org/mongo-driver/v2/mongo"
)

var (
	ErrPolicyNotFound = errors.New("policy document not found")
)

const (
	MaxCreditAmountPolicy       = "MAX_CREDIT_AMOUNT"
	MaxDebitAmountPolicy        = "MAX_DEBIT_AMOUNT"
	MaxWalletTransactionsPolicy = "MAX_WALLET_TRANSACTIONS"
)

type AmountLimitPolicy struct {
	ID        string `bson:"_id"`
	Enabled   bool   `bson:"enabled"`
	MaxAmount string `bson:"maxAmount"`
}

type TransactionFrequencyPolicy struct {
	ID              string `bson:"_id"`
	Enabled         bool   `bson:"enabled"`
	MaxTransactions int64  `bson:"maxTransactions"`
	WindowSeconds   int64  `bson:"windowSeconds"`
}

type PolicyRepository struct {
	collection *mongo.Collection
}

func NewPolicyRepository(collection *mongo.Collection) *PolicyRepository {
	return &PolicyRepository{
		collection: collection,
	}
}

func (r *PolicyRepository) GetAmountLimit(
	ctx context.Context,
	policyID string,
) (AmountLimitPolicy, error) {
	var policy AmountLimitPolicy

	err := r.collection.FindOne(
		ctx,
		bson.D{{Key: "_id", Value: policyID}},
	).Decode(&policy)

	if err != nil {
		if errors.Is(err, mongo.ErrNoDocuments) {
			return AmountLimitPolicy{}, fmt.Errorf(
				"%w: %s",
				ErrPolicyNotFound,
				policyID,
			)
		}

		return AmountLimitPolicy{}, fmt.Errorf(
			"fetch amount policy %s: %w",
			policyID,
			err,
		)
	}
	return policy, nil
}

func (r *PolicyRepository) GetTransactionFrequencyPolicy(
	ctx context.Context,
) (TransactionFrequencyPolicy, error) {
	var policy TransactionFrequencyPolicy

	err := r.collection.FindOne(
		ctx,
		bson.D{
			{Key: "_id", Value: MaxWalletTransactionsPolicy},
		},
	).Decode(&policy)

	if err != nil {
		if errors.Is(err, mongo.ErrNoDocuments) {
			return TransactionFrequencyPolicy{}, fmt.Errorf(
				"%w: %s",
				ErrPolicyNotFound,
				MaxWalletTransactionsPolicy,
			)
		}

		return TransactionFrequencyPolicy{}, fmt.Errorf(
			"fetch transaction frequency policy: %w",
			err,
		)
	}

	return policy, nil
}
