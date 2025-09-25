import { defineStore } from 'pinia'
import axios from 'axios'
import { ref, Ref } from 'vue'
import { io } from 'socket.io-client'

const walletAddress = 'http://localhost:8091'

interface Transaction {
  id: number
  senderAddress: Wallet
  receiverAddress: Wallet
  amount: number
  hash: string
  immutableChainedHash: string
  dateTime: Date
  blockHash: string
  idBlock: number
}

interface Wallet {
  address: string
  id: string
  cryptedContent: string
  amount: number
  transactions: Array<Transaction>
}

interface WalletState {
  wallet: Wallet | undefined
  state: any
  refreshWallet: boolean
}

interface TransactionPackage {
  from: string | undefined
  to: string | undefined
  amountSended: number
  amountReceived: number
}

const walletId = 'Personnal'

export const useWalletStore = defineStore('wallet', () => {
  const walletstate = ref<WalletState>({
    wallet: undefined,
    state: {},
    refreshWallet: false,
  })

  const transacPackage = ref<TransactionPackage>({
    from: undefined,
    to: undefined,
    amountSended: 0,
    amountReceived: 0,
  })

  const sendTransaction = async (sendOrRiceive: boolean) => {
    try {
      transacPackage.value.from = walletId

      console.log('Transaction package:', transacPackage.value)

      await axios
        .post(walletAddress + '/api/wallet/send', {
          from: sendOrRiceive ? walletId : 'cryptoProvider', // on inverse pour crediter
          to: sendOrRiceive ? transacPackage.value.to : walletId,
          amount: sendOrRiceive
            ? transacPackage.value.amountSended
            : transacPackage.value.amountReceived,
        })
        .then(function (response) {
          console.log(response)
        })
        .catch(function (error) {
          console.log(error)
        })
    } catch (error) {
      console.error('Failed to send transaction:', error)
    }
  }

  const initWSListener = async () => {
    try {
      let transaction: Ref<any> = ref({})

      const socket = io('127.0.0.1:3001', {
        transports: ['websocket'],
      })

      socket.on('connect', () => {
        console.log('Connected to server port 3001')
      })

      socket.on('disconnect', () => {
        console.log('Disconnected to server port 3001')
      })

      socket.on('wallet', (data) => {
        fetchWallet()
      })
    } catch (error) {
      console.error('Failed to initialize wallet:', error)
    }
  }

  const fetchWallet = async () => {
    try {
      walletstate.value.refreshWallet = false

      //  const data = (await axios.get(walletAddress + '/api/wallet/get/{name}')).data
      const data = (await axios.get(walletAddress + '/api/wallet/getDefaultWallet')).data

      walletstate.value.wallet = {
        address: data.address,
        id: data.walletId,
        cryptedContent: data.cryptedContent,
        amount: Array.isArray(data.transactions)
          ? Array.from(data.transactions).reduce((acc: number, tx: any) => {
              return acc + (tx.senderAddress.walletId === walletId ? -tx.amount : tx.amount)
            }, 0)
          : 0,
        transactions: Array.isArray(data.transactions)
          ? data.transactions.map((trans: any) => ({
              id: trans.id,
              senderAddress: trans.senderAddress,
              receiverAddress: trans.receiverAddress,
              amount: trans.amount,
              hash: trans.hash,
              immutableChainedHash: trans.immutableChainedHash,
              dateTime: new Date(trans.dateTime),
              blockHash: trans.blockHash,
              idBlock: trans.idBlock,
            }))
          : [],
      }

      walletstate.value.refreshWallet = true
    } catch (error) {
      console.error('Failed to fetch wallet :', error)
    }
  }

  return { walletstate, fetchWallet, initWSListener, sendTransaction, transacPackage }
})
