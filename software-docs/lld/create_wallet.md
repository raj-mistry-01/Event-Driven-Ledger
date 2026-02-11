
## 1. Command Definition

**Command Name**  
CreateWallet

**Purpose**  
Create a new wallet aggregate in the system and optionally initialize it with an initial credit amount.

**Inputs**

- client_id (string) – identifies the calling client (defines idempotency scope)
- client_request_id (string) – unique identifier of this request from the client
- initial_amount (integer) – non-negative integer

**System-Generated**

- wallet_id (UUID) – newly generated identifier for the wallet
- event_id (UUID) – unique identifier for each domain event created

**Expected Version**  
NONE (this is a creation command – no prior aggregate state exists)

## 2. Preconditions (Business Rules)

- (client_id, client_request_id) pair must not have been successfully processed before
- initial_amount ≥ 0
- No wallet with the generated wallet_id must already exist

## 3. Execution Flow

1. Receive CreateWallet command
2. Idempotency check
   - Query processed_commands table using (client_id, client_request_id)
   - If record exists:  
     → return the previously stored response (wallet_id + transaction_id)  
     → stop processing
3. Generate new wallet_id (UUID v4 or equivalent)
4. Create WalletCreated domain event
   - event_id 
   - event_type = 0 // WALLET_CREATED
   - event_payload = {
        <br>
       "initial_amount": initial_amount, <br>
       "status": 1  // ACTIVE
        <br>
     }
   - event_version = 1
   - event_timestamp 
   - wallet_id
   - client_id
   - client_request_id
5. Begin database transaction
6. Persist WalletCreated event into events table
7. Insert record into wallet_stream_head
   - wallet_id
   - version = 1
8. If initial_amount > 0:
   - Create WalletCredited domain event  
     - event_id 
     - event_type 
     - event_payload = {
          <br>
         "amount": initial_amount
          <br>
       }
     - event_version = 2
     - event_timestamp 
     - wallet_id
     - client_id
     - client_request_id
   - Persist WalletCredited event into events table
   - Update wallet_stream_head version = 2
9. Record successful command execution
   - Insert into processed_commands:  
     - client_id  
     - client_request_id  
     - wallet_id  
     - event_id of WalletCreated 
     - processed_at 
10. Register events for publishing
    - Insert corresponding row(s) into outbox_events
      with status = PENDING.
11. Commit database transaction.

12. Return success response to client.

## 4. Transaction Boundary

**Inside single database transaction (steps 5–11):**

- Insert WalletCreated event
- (conditional) Insert WalletCredited event
- Insert / update wallet_stream_head record
- Insert processed_commands record
- Insert outbox_events record(s)

**Outside transaction:**

- Idempotency lookup
- wallet_id generation
- Kafka / message broker publishing (system work)
- Projection updates (balance, transaction history, etc.) (syetem work)
- Client response is sent as soon as commit

## 5. Failure Handling

| Failure Scenario                          | System Behavior                                       | Client Response / HTTP Status      |
|-------------------------------------------|-------------------------------------------------------|------------------------------------|
| Duplicate (client_id, client_request_id)  | Return previously stored successful response          | 200 OK (idempotent)                |
| initial_amount < 0                        | Reject before transaction                             | 400 Bad Request – INVALID_AMOUNT   |
| Database constraint violation             | Transaction rolled back – no partial writes           | 500 Internal Server Error          |
| Database unavailable / deadlock           | Transaction fails or never starts                     | 503 Service Unavailable (retryable)|
| Command service crash after commit        | Events & outbox rows are persisted → eventual publish | Client already received 200 OK     |
| Projection service down                   | No impact on command success                          | 200 OK (eventual consistency)      |
| Unexpected internal error                 | Transaction rolled back if before commit              | 500 Internal Server Error          |

## 6. Output Contract

**Success Response (200 OK)**

```json
{
  "wallet_id": "",
  "transaction_id": ""
}
```
**Command Failure Responses**
- 400 Bad Request – INVALID_AMOUNT
```json
{
  "error_code": "INVALID_AMOUNT",
  "message": "Initial amount must be a non-negative integer."
}
```
- 500 Internal Server Error – DATABASE_ERROR
```json
{
  "error_code": "INTERNAL_ERROR",
  "message": "Please try again later."
}
```
