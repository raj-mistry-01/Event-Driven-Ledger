package main

import (
	"context"
	"log"
	"net"
	"os"
	"time"

	policyv1 "policy-service/api/proto/policyv1"
	"policy-service/internal/config"
	"policy-service/internal/handler"

	"policy-service/internal/repository"
	policyservice "policy-service/internal/service"

	"github.com/joho/godotenv"
	"google.golang.org/grpc"
)

func main() {
	if err := godotenv.Load(); err != nil {
		log.Println(".env file not found; using environment variables")
	}

	// Connect to MongoDB.
	ctx, cancel := context.WithTimeout(
		context.Background(),
		10*time.Second,
	)
	defer cancel()

	mongoClient, err := config.ConnectMongo(ctx)
	if err != nil {
		log.Fatalf("MongoDB connection failed: %v", err)
	}
	defer func() {
		if err := mongoClient.Disconnect(context.Background()); err != nil {
			log.Printf("MongoDB disconnect error: %v", err)
		}
	}()

	log.Println("MongoDB connected successfully")

	db := mongoClient.Database("policy")
	collection := db.Collection("policies")

	// policyRepo := repository.NewPolicyRepository(collection)

	// creditPolicy, err := policyRepo.GetAmountLimit(
	// 	ctx,
	// 	repository.MaxCreditAmountPolicy,
	// )
	// if err != nil {
	// 	log.Fatalf("Failed to load credit policy: %v", err)
	// }

	// debitPolicy, err := policyRepo.GetAmountLimit(
	// 	ctx,
	// 	repository.MaxDebitAmountPolicy,
	// )
	// if err != nil {
	// 	log.Fatalf("Failed to load debit policy: %v", err)
	// }

	// frequencyPolicy, err := policyRepo.GetTransactionFrequencyPolicy(ctx)
	// if err != nil {
	// 	log.Fatalf("Failed to load frequency policy: %v", err)
	// }

	// log.Printf("Credit policy loaded: enabled=%t, maxAmount=%s",
	// 	creditPolicy.Enabled, creditPolicy.MaxAmount)

	// log.Printf("Debit policy loaded: enabled=%t, maxAmount=%s",
	// 	debitPolicy.Enabled, debitPolicy.MaxAmount)

	// log.Printf("Frequency policy loaded: enabled=%t, maxTransactions=%d, windowSeconds=%d",
	// 	frequencyPolicy.Enabled,
	// 	frequencyPolicy.MaxTransactions,
	// 	frequencyPolicy.WindowSeconds,
	// )

	policyRepo := repository.NewPolicyRepository(collection)
	policySvc := policyservice.NewPolicyService(policyRepo)

	// Configure gRPC port.
	port := os.Getenv("GRPC_PORT")
	if port == "" {
		port = "9090"
	}

	listener, err := net.Listen("tcp", ":"+port)
	if err != nil {
		log.Fatalf("Failed to listen: %v", err)
	}

	server := grpc.NewServer()

	policyv1.RegisterPolicyServiceServer(
		server,
		handler.NewPolicyHandler(policySvc),
	)

	log.Printf("Policy Service gRPC server listening on :%s", port)

	if err := server.Serve(listener); err != nil {
		log.Fatalf("gRPC server stopped: %v", err)
	}
}
