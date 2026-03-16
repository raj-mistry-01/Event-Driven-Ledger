import requests
import random
import uuid

BASE_URL = "http://localhost:8082"

ACTIVATE_URL = f"{BASE_URL}/wallet/activate"
SUSPEND_URL = f"{BASE_URL}/wallet/suspend"
CREDIT_URL = f"{BASE_URL}/wallet/credit"
DEBIT_URL = f"{BASE_URL}/wallet/debit"

wallet_ids = [
    "550d06e7-a2ee-4bc4-989a-12a1983af270",
    "80ba76e3-20a2-4d9a-8c9c-65c60e68b6b2",
    "d25dec45-7444-447d-adc5-d5875ddc40b1"
]

# local wallet state tracking
wallet_state = {}

for wid in wallet_ids:
    wallet_state[wid] = {
        "status": "active",
        "balance": 0
    }

def unique_ids():
    return str(random.randint(1000000,9999999)), str(uuid.uuid4())

for i in range(1000):

    wallet = random.choice(wallet_ids)
    action = random.choice(["activate","suspend","credit","debit"])

    clientId, clientReqId = unique_ids()

    state = wallet_state[wallet]

    try:

        if action == "activate":

            if state["status"] == "active":
                continue

            payload = {
                "walletId": wallet,
                "clientId": clientId,
                "clientRequestId": clientReqId
            }

            r = requests.post(ACTIVATE_URL, json=payload)
            print(i,"ACTIVATE",wallet,r.status_code)

            if r.status_code == 200:
                state["status"] = "active"


        elif action == "suspend":

            if state["status"] == "suspended":
                continue

            payload = {
                "walletId": wallet,
                "clientId": clientId,
                "clientRequestId": clientReqId
            }

            r = requests.post(SUSPEND_URL, json=payload)
            print(i,"SUSPEND",wallet,r.status_code)

            if r.status_code == 200:
                state["status"] = "suspended"


        elif action == "credit":

            amount = random.randint(10,200)

            payload = {
                "walletId": wallet,
                "creditAmount": amount,
                "clientId": clientId,
                "clientRequestId": clientReqId
            }

            r = requests.post(CREDIT_URL, json=payload)
            print(i,"CREDIT",wallet,amount,r.status_code)

            if r.status_code == 200:
                state["balance"] += amount


        elif action == "debit":

            amount = random.randint(10,200)

            if state["balance"] < amount:
                continue

            payload = {
                "walletId": wallet,
                "debitAmount": amount,
                "clientId": clientId,
                "clientRequestId": clientReqId
            }

            r = requests.post(DEBIT_URL, json=payload)
            print(i,"DEBIT",wallet,amount,r.status_code)

            if r.status_code == 200:
                state["balance"] -= amount

    except Exception as e:
        print("ERROR",e)

print("\nFINAL EXPECTED STATE\n")

for wid,data in wallet_state.items():
    print(wid,data)