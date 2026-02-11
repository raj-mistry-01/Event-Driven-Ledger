# Command: ActivateWallet

## 1. Command Definition

**Command Name**  
ActivateWallet

**Inputs**

- wallet_id (UUID) – identifier of the wallet to activate
- client_id (string) – identifies the calling client (defines idempotency scope)
- client_request_id (string) – unique request identifier from the client

**System-Generated**

- event_id (UUID) – unique identifier for the generated domain event

**Expected Version**  
Required – command must supply the expected current version of the wallet stream (optimistic concurrency control)

## 2. Preconditions (Business Rules)

- Wallet aggregate must exist
- Wallet current status must be SUSPENDED
- Wallet must NOT be in CLOSED state
- (client_id, client_request_id) pair must not have been successfully processed before

## 3. Execution Flow

1. Receive ActivateWallet command
2. Idempotency check
    - Query processed_commands table using (client_id, client_request_id)
    - If record exists:  
      → return previously stored response (wallet_id + transaction_id)  
      → stop processing
3. Load wallet aggregate state
    - Load latest snapshot (if snapshotting is used)
    - Replay events from snapshot version + 1 to current version
    - Determine current status 
4. Validate business rules
    - If no wallet found → reject with WALLET_NOT_FOUND
    - If current status is CLOSED → reject with WALLET_CLOSED
    - If current status is already ACTIVE → reject with WALLET_ALREADY_ACTIVE
5. Create WalletActivated domain event
    - event_id 
    - event_type = 1 // WALLET_ACTIVATED
    - event_payload = {
         <br>
        "status": 1  // ACTIVE
         <br>
      }
    - event_version 
    - event_timestamp
    - wallet_id 
    - client_id
    - client_request_id
6. Begin database transaction
7. Persist WalletActivated event into events table
8. Event version match
   - if matched then update wallet stream head
   - else internal retry
9. Record successful command execution
    - Insert into processed_commands:  
      - event_id
      - wallet_id
      - client_id
      - client_request_id
      - processed_at 
10. Register event for publishing
    - Insert corresponding row(s) into outbox_events
      with status = PENDING
11. Commit database transaction
12. Return success response to client (immediately after commit)

## 4. Transaction Boundary

**Inside single database transaction (steps 6–11):**

- Insert WalletActivated event
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

| Failure Scenario                          | System Behavior                                       | Client Response / HTTP Status          |
|-------------------------------------------|-------------------------------------------------------|----------------------------------------|
| Duplicate (client_id, client_request_id)  | Return previously stored successful response          | 200 OK (idempotent)                    |
| Wallet does not exist                     | Reject before transaction                             | 404 Not Found – WALLET_NOT_FOUND       |
| Wallet already ACTIVE                     | Reject (or return success – policy decision)          | 409 Conflict – WALLET_ALREADY_ACTIVE   |
| Wallet is CLOSED                          | Reject before transaction                             | 403 Forbidden – WALLET_CLOSED          |
| Expected version mismatch                 | internal retry                                        |                                    |
| Database constraint violation             | Transaction rolled back – no partial writes           | 500 Internal Server Error              |
| Command service crash after commit        | Events & outbox rows are persisted → eventual publish | Client already received 200 OK         |
| Unexpected internal error                 | Transaction rolled back if before commit              | 500 Internal Server Error              |

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
    "message": "Wallet is closed and cannot be activated"
  }
  ```
- **409 Conflict** – WALLET_ALREADY_ACTIVE  
  ```json
  {
    "error_code": "WALLET_ALREADY_ACTIVE",
    "message": "Wallet is already active"
  }
  ```
- **500 Internal Server Error** – for unexpected errors during processing  
  ```json
  {
    "error_code": "INTERNAL_ERROR",
    "message": "An unexpected error occurred. Please try again later."
  }
  ```