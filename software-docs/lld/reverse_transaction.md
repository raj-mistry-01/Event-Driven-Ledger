# Command: ReverseTransaction

## 1. Command Definition

**Command Name**  
ReverseTransaction

**Purpose**  
Reverse a previously applied monetary transaction (Credit or Debit) by appending a compensating reversal event.  
History remains immutable; the reversal is a new compensating event that negates the financial effect.

**Inputs**

- wallet_id (UUID) – identifier of the wallet
- original_transaction_id (UUID) – event_id of the original Credit or Debit event to reverse
- client_id (string) – identifies the calling client (defines idempotency scope)
- client_request_id (string) – unique request identifier from the client

**System-Generated**

- event_id (UUID) – unique identifier for the generated reversal event

**Expected Version**  
Required – command must supply the expected current version of the wallet stream (optimistic concurrency control)

## 2. Preconditions (Business Rules)

- Wallet aggregate must exist
- Wallet current status must be ACTIVE
- original_transaction_id must exist in the events table
- Original event must:
    - belong to the same wallet_id
    - be a monetary event (WalletCredited or WalletDebited)
- Original event must NOT have been already reversed
- (client_id, client_request_id) pair must not have been successfully processed before

Lifecycle events (WalletCreated, WalletActivated, WalletSuspended, WalletClosed) cannot be reversed.

## 3. Execution Flow

1. Receive ReverseTransaction command
2. Idempotency check
    - Query processed_commands table using (client_id, client_request_id)
    - If record exists:  
      → return previously stored response (wallet_id + transaction_id + reversed_transaction_id)  
      → stop processing
3. Check wallet existence & get expected version
    - SELECT last_version FROM wallet_stream_head WHERE wallet_id = ?
    - If no record found → reject with WALLET_NOT_FOUND
    - Set expected_version = last_version
4. Load wallet aggregate state
    - Load latest snapshot (if snapshotting is used)
    - Replay events from snapshot version + 1 to expected_version
    - Determine current status
5. Validate wallet state
    - If current status is not ACTIVE → reject (SUSPENDED or CLOSED)
6. Locate original event
    - Fetch event from events table by original_transaction_id
    - Validate:
        - event.wallet_id == wallet_id
        - event.event_type is WALLET_CREATED or WALLET_DEBITED
    - If not found or invalid → reject with INVALID_TRANSACTION
7. Check reversal eligibility
    - Scan replayed events 
    - Ensure no existing reversal event references original_transaction_id
    - If already reversed → reject with ALREADY_REVERSED
8. Derive reversal semantics
    - If original event is WalletCredited(amount):  
      → reversal_type = DEBIT_REVERSAL, amount = original.amount 
    - If original event is WalletDebited(amount):  
      → reversal_type = CREDIT_REVERSAL, amount = original.amount
9. Compute new event version
    - new_version = expected_version + 1
10. Create reversal domain event
    - event_id = 
    - event_type = 6 or 7 // WALLET_DEBIT_REVERSAL or WALLET_CREDIT_REVERSAL
    - event_payload = {
        <br>
        "original_event_id":  <br>
        "amount": amount, <br>
      }
    - event_version = new_version
    - event_timestamp = current UTC timestamp
    - wallet_id
    - client_id
    - client_request_id
11. BEGIN DATABASE TRANSACTION
12. Persist reversal event into events table  
13. Event version match
    - if matched then update wallet stream head
    - else internal retry
14. Record successful command execution
    - Insert into processed_commands:
        - event_id
        - wallet_id
        - client_id
        - client_request_id
        - processed_at = now()
15. Register event for publishing
    - Insert corresponding row into outbox_events  
      with status = PENDING
16. COMMIT transaction
17. Return success response to client (immediately after commit)

## 4. Transaction Boundary

**Inside single database transaction (steps 11–16):**

- Insert TransactionReversed event
- Update wallet_stream_head version (with optimistic lock)
- Insert processed_commands record
- Insert outbox_events record

**Outside transaction:**

- Idempotency lookup
- Stream head read & aggregate loading
- Original event lookup & validation
- Reversal eligibility check
- Event creation
- Kafka / message broker publishing
- Projection updates (balance, transaction history, reversal links)
- Client response is sent after commit

## 5. Failure Handling

| Failure Scenario                          | System Behavior                              | Client Response / HTTP Status                    |
|-------------------------------------------|----------------------------------------------|--------------------------------------------------|
| Duplicate (client_id, client_request_id)  | Return previously stored successful response | 200 OK (idempotent)                              |
| Wallet does not exist                     | Reject before transaction                    | 404 Not Found – WALLET_NOT_FOUND                 |
| Wallet not ACTIVE (SUSPENDED or CLOSED)   | Reject before transaction                    | 403 Forbidden – WALLET_SUSPENDED / WALLET_CLOSED |
| Original transaction_id not found         | Reject before transaction                    | 404 Not Found – INVALID_TRANSACTION              |
| Original event not monetary (Credit/Debit)| Reject before transaction                    | 400 Bad Request – INVALID_TRANSACTION            |
| Original event already reversed           | Reject before transaction                    | 409 Conflict – ALREADY_REVERSED                  |
| Concurrency conflict (update count = 0)   | internal retry                               |                                                  |
| Retry limit exceeded                      | No state change – clean failure              | 503 Service Unavailable – RETRY_LATER            |
| Database constraint violation             | Transaction rolled back – no partial writes  | 500 Internal Server Error                        |
| Command service crash after commit        | Events & outbox persisted → eventual publish | Client already received 200 OK                   |
| Kafka / broker unavailable                | Outbox retains event → background retry      | No impact – 200 OK already sent                  |
| Unexpected internal error                 | Transaction rolled back if before commit     | 500 Internal Server Error                        |

## 6. Output Contract

**Success Response (200 OK)**

```json
{
  "wallet_id": "",
  "transaction_id": "",
  "reversed_transaction_id": ""
}
```
**Common Error Responses**
- 404 Not Found – WALLET_NOT_FOUND
```json
{
  "error_code": "WALLET_NOT_FOUND",
  "message": "Wallet with id {wallet_id} not found."
}
```
- 403 Forbidden – WALLET_SUSPENDED
```json
{
  "error_code": "WALLET_SUSPENDED",
  "message": "Wallet with id {wallet_id} is currently suspended."
}
```
- 403 Forbidden – WALLET_CLOSED
```json
{
  "error_code": "WALLET_CLOSED",
  "message": "Wallet with id {wallet_id} is closed and cannot be reversed."
}
```
- 404 Not Found – INVALID_TRANSACTION
```json
{
  "error_code": "INVALID_TRANSACTION",
  "message": "Original transaction with id {original_transaction_id} not found or invalid."
}
```
- 409 Conflict – ALREADY_REVERSED
```json
{
  "error_code": "ALREADY_REVERSED",
  "message": "Original transaction with id {original_transaction_id} has already been reversed."
}
```
- 503 Service Unavailable – RETRY_LATER
```json
{
  "error_code": "RETRY_LATER",
  "message": "High concurrency detected. Please retry the request."
}
```
- 500 Internal Server Error – for unexpected errors
```json
{
  "error_code": "INTERNAL_ERROR",
  "message": "An unexpected error occurred. Please try again later."
}
``` 