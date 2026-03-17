const BASE_URL = "http://localhost:8082";

const ACTIVATE_URL = `${BASE_URL}/wallet/activate`;
const SUSPEND_URL = `${BASE_URL}/wallet/suspend`;
const CREDIT_URL = `${BASE_URL}/wallet/credit`;
const DEBIT_URL = `${BASE_URL}/wallet/debit`;

const walletIds = [
    "8ad47484-c799-4403-a36b-270606f51bbd",
];

let walletState = {};

for (let id of walletIds) {
    walletState[id] = {
        status: "active",
        balance: 0
    };
}

function randomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function uuid() {
    return crypto.randomUUID();
}

function uniqueIds() {
    return {
        clientId: randomInt(1000000, 9999999).toString(),
        clientRequestId: uuid()
    };
}

async function makeCall(i) {

    const wallet = walletIds[randomInt(0, walletIds.length - 1)];
    const actions = ["activate", "credit", "debit"];
    const action = actions[randomInt(0, actions.length - 1)];

    const { clientId, clientRequestId } = uniqueIds();
    const state = walletState[wallet];

    let url;
    let payload;

    if (action === "activate") {

        if (state.status === "active") return;

        url = ACTIVATE_URL;

        payload = {
            walletId: wallet,
            clientId,
            clientRequestId
        };

    } else if (action === "suspend") {

        if (state.status === "suspended") return;

        url = SUSPEND_URL;

        payload = {
            walletId: wallet,
            clientId,
            clientRequestId
        };

    } else if (action === "credit") {

        const amount = randomInt(10, 200);

        url = CREDIT_URL;

        payload = {
            walletId: wallet,
            creditAmount: amount,
            clientId,
            clientRequestId
        };

        state.balance += amount;

    } else if (action === "debit") {

        const amount = randomInt(10, 200);

        if (state.balance < amount) return;

        url = DEBIT_URL;

        payload = {
            walletId: wallet,
            debitAmount: amount,
            clientId,
            clientRequestId
        };

        state.balance -= amount;
    }

    try {

        const res = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });
        const rsq = await res.json()
        console.log(i, action.toUpperCase(), wallet, res.status);
        console.log('------------------------------------------------------------------------------------------')
        console.log(rsq)
        console.log('------------------------------------------------------------------------------------------')

    } catch (err) {
        console.log("ERROR", err);
    }
}

async function runTest() {

    const promises = [];

    for (let i = 0; i < 50; i++) {
        promises.push(makeCall(i));
    }

    await Promise.all(promises);

    console.log("\nFINAL EXPECTED STATE\n");
    console.log(walletState);
}

runTest();