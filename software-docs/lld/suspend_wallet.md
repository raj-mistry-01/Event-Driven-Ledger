# Command: SuspendWallet

## 1. Command Definition

**Command Name**  
SuspendWallet

**Purpose**  
Temporarily suspend an ACTIVE wallet so that no financial operations (credit, debit, transfers) are allowed while preserving the current balance.

**Inputs**

- wallet_id (UUID) – identifier of the wallet to suspend
- client_id (string) – identifies the calling client (defines idempotency scope)
- client_request_id (string) – unique request identifier from the client

**System-Generated**

- event_id (UUID) – unique identifier for the generated domain event

**Expected Version**  
Required – command must supply the expected current version of the wallet stream (optimistic concurrency control)

## 2. Preconditions (Business Rules)

- Wallet aggregate must exist
- Wallet current status must be ACTIVE
- Wallet must NOT be in CLOSED state
- (client_id, client_request_id) pair must not have been successfully processed before


## 3. Execution Flow

1. Receive SuspendWallet command
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
   - Determine wallet state
5. Validate business rules
    - If no wallet found → reject with WALLET_NOT_FOUND
    - If current status is CLOSED → reject with WALLET_CLOSED
    - If current status is already SUSPENDED → reject with WALLET_ALREADY_SUSPENDED (or treat as idempotent success – policy decision)
6. Create WalletSuspended domain event
    - event_id = new UUID
    - event_type = 2 // WALLET_SUSPENDED
    - event_payload = {
     <br>
     "status": 2 // SUSPENDED
     <br>
     }
    - event_version = 
    - event_timestamp = 
    - wallet_id
    - client_id
    - client_request_id
7. Begin database transaction
8. Persist WalletSuspended event into events table
9. Event version match
    - if matched then update wallet stream head
    - else internal retry
10. Record successful command execution
    - Insert into processed_commands:
        - event_id
        - wallet_id
        - client_id
        - client_request_id
        - processed_at = now()
11. Register event for publishing
    - Insert corresponding row into outbox_events  
      with status = PENDING
12. Commit database transaction
13. Return success response to client (immediately after commit)

## 4. Transaction Boundary

**Inside single database transaction (steps 7–12):**

- Insert WalletSuspended event
- Update wallet_stream_head version
- Insert processed_commands record
- Insert outbox_events record

**Outside transaction:**

- Idempotency lookup
- Aggregate loading & event replay
- Business rule validation
- Event creation
- Kafka / message broker publishing
- Projection updates
- Client response is sent after commit

## 5. Failure Handling

| Failure Scenario                          | System Behavior                                      | Client Response / HTTP Status           |
|-------------------------------------------|------------------------------------------------------|-----------------------------------------|
| Duplicate (client_id, client_request_id)  | Return previously stored successful response         | 200 OK (idempotent)                     |
| Wallet does not exist                     | Reject before transaction                            | 404 Not Found – WALLET_NOT_FOUND        |
| Wallet already SUSPENDED                  | Reject (or return success – policy decision)         | 409 Conflict – WALLET_ALREADY_SUSPENDED |
| Wallet is CLOSED                          | Reject before transaction                            | 403 Forbidden – WALLET_CLOSED           |
| Expected version mismatch                 | internal retry                                       |                                         |
| Database constraint violation             | Transaction rolled back – no partial writes          | 500 Internal Server Error               |
| Command service crash after commit        | Events & outbox rows are persisted → eventual publish| Client already received 200 OK          |
| Unexpected internal error                 | Transaction rolled back if before commit             | 500 Internal Server Error               |

## 6. Output Contract

**Success Response (200 OK)**

```json
{
  "wallet_id": "",
  "transaction_id": ""
}
```
**Common Failure Responses**
- **404 Not Found** – WALLET_NOT_FOUND
```json
{
  "error_code": "WALLET_NOT_FOUND",
  "message": "Wallet does not exist"
}
```
- **403 Forbidden** – WALLET_CLOSED
```json
{
  "error_code": "WALLET_CLOSED",
  "message": "Wallet is closed and cannot be suspended"
}
```
- **409 Conflict** – WALLET_ALREADY_SUSPENDED
```json
{
  "error_code": "WALLET_ALREADY_SUSPENDED",
  "message": "Wallet is already suspended"
}
```
- **500 Internal Server Error** – for unexpected errors or database issues
```json
{
  "error_code": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred. Please try again later."
}
```