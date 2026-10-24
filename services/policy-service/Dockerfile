# Build stage
FROM golang:1.25-alpine AS builder

WORKDIR /app

# Install certificates for secure connections
RUN apk add --no-cache ca-certificates

# Download dependencies first for better layer caching
COPY go.mod go.sum ./
RUN go mod download

# Copy source code
COPY . .

# Build a static Linux binary
RUN CGO_ENABLED=0 GOOS=linux go build \
    -trimpath \
    -ldflags="-s -w" \
    -o /policy-service \
    ./cmd/policy-service

# Runtime stage
FROM alpine:3.22

WORKDIR /app

RUN apk add --no-cache ca-certificates \
    && addgroup -S appgroup \
    && adduser -S appuser -G appgroup

COPY --from=builder /policy-service /app/policy-service

USER appuser

EXPOSE 9090

ENTRYPOINT ["/app/policy-service"]