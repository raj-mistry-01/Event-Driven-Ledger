# Command: CloseWallet

## 1. Command Definition

**Command Name**  
CloseWallet

**Purpose**  
Permanently close a wallet. Once closed, the wallet cannot be reactivated or used for any financial operation (credits, debits, transfers, etc.).

**Inputs**

- wallet_id (UUID) – identifier of the wallet to close
- client_id (string) – identifies the calling client (defines idempotency scope)
- client_request_id (string) – unique request identifier from the client

**System-Generated**

- event_id (UUID) – unique identifier for the generated domain event

**Expected Version**  
Required – command must supply the expected current version of the wallet stream (optimistic concurrency control)

## 2. Preconditions (Business Rules)

- Wallet aggregate must exist
- Wallet must NOT already be in CLOSED state
- Wallet may be in ACTIVE or SUSPENDED state
- (client_id, client_request_id) pair must not have been successfully processed before

## 3. Execution Flow

1. Receive CloseWallet command
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
    - If current status is CLOSED → reject with WALLET_ALREADY_CLOSED
6. Create WalletClosed domain event
    - event_id = 
    - event_type = 3 // WALLET_CLOSED
     - event_payload = {
     <br>
     "status": 3 // CLOSED
     <br>
     }
    - event_version 
    - event_timestamp 
    - wallet_id
    - client_id
    - client_request_id
7. Begin database transaction
8. Persist WalletClosed event into events table  
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

- Insert WalletClosed event
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
| Wallet already CLOSED                     | Reject before transaction                             | 409 Conflict – WALLET_ALREADY_CLOSED   |
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
- **404 Not Found – WALLET_NOT_FOUND**
  
  ```json
  {
    "error_code": "WALLET_NOT_FOUND",
    "message": "The specified wallet does not exist."
  }
  ```
- **409 Conflict – WALLET_ALREADY_CLOSED**
  
  ```json
  {
    "error_code": "WALLET_ALREADY_CLOSED",
    "message": "The wallet is already closed."
  }
  ```
- **500 Internal Server Error**
  
  ```json
  {
    "error_code": "INTERNAL_SERVER_ERROR",
    "message": "An unexpected error occurred. Please try again later."
  }
  ```