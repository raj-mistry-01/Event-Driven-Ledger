# Command: DebitWallet

## 1. Command Definition

**Command Name**  
DebitWallet

**Purpose**  
Debit (remove funds from) an ACTIVE by amount.

**Inputs**

- wallet_id (UUID) – identifier of the wallet to debit
- amount (positive number)
- client_id (string) – identifies the calling client (defines idempotency scope)
- client_request_id (string) – unique request identifier from the client

**System-Generated**

- event_id (UUID) – unique identifier for the generated domain event

## 2. Preconditions (Business Rules)

- Wallet aggregate must exist
- Wallet current status must be ACTIVE
- Wallet must NOT be SUSPENDED or CLOSED
- amount > 0
- Wallet balance ≥ amount
- (client_id, client_request_id) pair must not have been successfully processed before

## 3. Execution Flow

1. Receive DebitWallet command
2. Idempotency check
    - Query processed_commands table using (client_id, client_request_id)
    - If record exists:  
      → return previously stored response (wallet_id + transaction_id)  
      → stop processing
3. Check wallet existence and get current version
    - SELECT last_version FROM wallet_stream_head WHERE wallet_id = ?
    - If no record found → reject with WALLET_NOT_FOUND
    - Set expected_version = last_version
4. Load wallet aggregate state
    - Load latest snapshot  
    - Replay events from snapshot version + 1 to expected_version  
    - Determine wallet state and current balance
5. Validate business rules
    - If current status is not ACTIVE → reject (SUSPENDED or CLOSED)
    - If amount ≤ 0 → reject with INVALID_AMOUNT
    - If current balance < amount → reject with INSUFFICIENT_FUNDS
6. Compute new event version
    - new_version = last_version + 1
7. Create WalletDebited domain event
    - event_id = new UUID
    - event_type = 5 // WALLET_DEBITED
    - event_payload = {
     <br>
     "amount": amount
     <br>
     } 
    - event_version = new_version
    - event_timestamp
    - wallet_id
    - client_id
    - client_request_id
8. Begin database transaction
9. Persist WalletDebited event into events table
10. Event version match
    - if matched then update wallet stream head
    - else internal retry
11. Record successful command execution
    - Insert into processed_commands:
        - event_id
        - wallet_id
        - client_id
        - client_request_id
        - processed_at = now()
12. Register event for publishing
    - Insert corresponding row into outbox_events  
      with status = PENDING
13. Commit database transaction
14. Return success response to client (immediately after commit)

## 4. Transaction Boundary

**Inside single database transaction (steps 8–13):**

- Insert WalletDebited event
- Update wallet_stream_head version (with optimistic lock)
- Insert processed_commands record
- Insert outbox_events record

**Outside transaction:**

- Idempotency lookup
- Stream head read & aggregate loading
- Business rule validation
- Event creation
- Kafka / message broker publishing
- Projection updates (balance, transaction history)
- Client response is sent after commit

## 5. Failure Handling

| Failure Scenario                          | System Behavior                                   | Client Response / HTTP Status          |
|-------------------------------------------|---------------------------------------------------|----------------------------------------|
| Duplicate (client_id, client_request_id)  | Return previously stored successful response      | 200 OK (idempotent)                    |
| Wallet does not exist                     | Reject before transaction                         | 404 Not Found – WALLET_NOT_FOUND       |
| Wallet SUSPENDED                          | Reject before transaction                         | 403 Forbidden – WALLET_SUSPENDED       |
| Wallet CLOSED                             | Reject before transaction                         | 403 Forbidden – WALLET_CLOSED          |
| amount ≤ 0                                | Reject before transaction                         | 400 Bad Request – INVALID_AMOUNT       |
| balance < amount                          | Reject before transaction                         | 409 Conflict – INSUFFICIENT_FUNDS      |
| Concurrency conflict (update count = 0)   | internal retry                                    |                                        |
| Database constraint violation             | Transaction rolled back – no partial writes       | 500 Internal Server Error              |
| Command service crash after commit        | Events & outbox rows persisted → eventual publish | Client already received 200 OK         |
| Kafka / broker unavailable                | Outbox retains event → background retry           | No impact – 200 OK already sent        |
| Unexpected internal error                 | Transaction rolled back if before commit          | 500 Internal Server Error              |

## 6. Output Contract

**Success Response (200 OK)**

```json
{
  "wallet_id": "",
  "transaction_id": ""
}
```
**Common Error Responses**
- 404 Not Found – WALLET_NOT_FOUND
```
{
  "error_code": "WALLET_NOT_FOUND",
  "message": "Wallet with id {wallet_id} not found."
}
```
- 403 Forbidden – WALLET_SUSPENDED
```
{
  "error_code": "WALLET_SUSPENDED",
  "message": "Wallet with id {wallet_id} is currently suspended."
}
```
- 403 Forbidden – WALLET_CLOSED
```
{
  "error_code": "WALLET_CLOSED",
  "message": "Wallet with id {wallet_id} is closed and cannot be debited."
}
```
- 409 Conflict – INSUFFICIENT_FUNDS
```
{
  "error_code": "INSUFFICIENT_FUNDS",
  "message": "Wallet with id {wallet_id} does not have enough balance."
}
```
- 400 Bad Request – INVALID_AMOUNT
```
{
  "error_code": "INVALID_AMOUNT",
  "message": "Amount must be a positive number."
}
```
- 500 Internal Server Error – for unexpected errors
```
{
  "error_code": "INTERNAL_ERROR",
  "message": "An unexpected error occurred. Please try again later."
}
```
