<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, Ref } from 'vue'
import { io } from 'socket.io-client'
import { useWalletStore } from '@/stores/wallet'

const walletStore = useWalletStore()
const showTransactions = ref(false)
onMounted(() => {
  walletStore.initWSListener()
  walletStore.fetchWallet()
})
</script>

<template>
  <body>
    <div class="wallet-block">
      <div>Wallet</div>

      <div v-if="walletStore.walletstate.refreshWallet">
        <div v-if="walletStore.walletstate.wallet">
          <div>id of wallet : {{ walletStore.walletstate.wallet.id }}</div>
          <div>address : {{ walletStore.walletstate.wallet.address }}</div>
          <div>amount : {{ walletStore.walletstate.wallet.amount }} coin(s)</div>
          <div>transaction Number : {{ walletStore.walletstate.wallet.transactions.length }}</div>
          <div>
            <button @click="showTransactions = !showTransactions">List des transactions</button>
          </div>
          <div
            v-show="showTransactions"
            class="wallet-transactions"
            v-for="transaction in walletStore.walletstate.wallet.transactions"
          >
            <div>
              <div>ID: {{ transaction.id }}</div>
              <div>From: {{ transaction.senderAddress.walletId }}</div>
              <div>To: {{ transaction.receiverAddress.walletId }}</div>
              <div>Amount: {{ transaction.amount }}</div>
              <div>Timestamp: {{ transaction.dateTime }}</div>
            </div>
          </div>
        </div>
        <div v-else>No Wallet datas</div>
      </div>
    </div>

    <div class="emit-transaction">
      <div>Send Transaction to another wallet</div>
      <div>
        <label for="toAddress">To Address:</label>
        <input id="toAddress" v-model="walletStore.transacPackage.to" required />
      </div>
      <div>
        <label for="amount">Amount:</label>
        <input
          id="amount"
          type="number"
          v-model="walletStore.transacPackage.amountSended"
          min="1"
          required
        />
      </div>
      <button @click="walletStore.sendTransaction(true)">Envoyer</button>
    </div>

    <div class="emit-transaction">
      <div>Credit wallet from crypto provider</div>
      <div>
        <label for="amount">Amount:</label>
        <input
          id="amount"
          type="number"
          v-model="walletStore.transacPackage.amountReceived"
          min="1"
          required
        />
      </div>
      <button @click="walletStore.sendTransaction(false)">Envoyer</button>
    </div>
  </body>
</template>

<style scoped>
body {
  display: grid;
  grid-template-rows: auto 1fr;
  width: 100vw;
  height: 100vh;
  margin: 0;
  font-family: 'Segoe UI', Arial, sans-serif;
}
.wallet-block,
.emit-transaction {
  grid-row: 1 / 2;
  float: left;
  display: flex;
  flex-direction: column;
  width: 500px;
  margin: 40px auto;
  padding: 24px;
  background: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.07);
  font-family: 'Segoe UI', Arial, sans-serif;
}

.emit-transaction input {
  width: 100%;
  padding: 8px;
  margin-top: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.wallet-block > div:first-child {
  font-size: 1.7rem;
  font-weight: bold;
  margin-bottom: 18px;
  color: #2d3748;
}

.wallet-transactions > div {
  margin-bottom: 10px;
  color: #444;
}

.wallet-transactions > div:nth-child(odd) {
  background-color: #f1f1f1;
  padding: 10px;
  border-radius: 8px;
}

.wallet-transactions {
  display: block;
  margin-bottom: 10px;
}
</style>
