import { defineStore } from 'pinia'
import axios from 'axios'
import { ref, Ref } from 'vue'
import { io } from 'socket.io-client'

const walletAddress = 'http://127.0.0.1:8091'

const consensusAddres = 'http://127.0.0.1:8090'

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
  amount: number
}

export const useWalletStore = defineStore('wallet', () => {
  const walletstate = ref<WalletState>({
    wallet: undefined,
    state: {},
    refreshWallet: false,
  })

  const transacPackage = ref<TransactionPackage>({
    from: undefined,
    to: undefined,
    amount: 0,
  })

  const sendTransaction = async () => {
    try {
      transacPackage.value.from = walletstate.value.wallet?.address

      console.log('Transaction package:', transacPackage.value)
      const response = await axios.post(
        consensusAddres + '/api/transaction/send',
        transacPackage.value,
      )
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

      const data = (await axios.get(walletAddress + '/api/wallet/get')).data

      walletstate.value.wallet = {
        address: data.address,
        id: data.walletId,
        cryptedContent: data.cryptedContent,
        amount: Array.isArray(data.transactions)
          ? data.transactions.reduce((acc: any, tx: any) => acc + (tx.amount || 0), 0)
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
      console.log(walletstate.value.wallet.transactions[0])
      walletstate.value.refreshWallet = true
    } catch (error) {
      console.error('Failed to fetch wallet :', error)
    }
  }

  return { walletstate, fetchWallet, initWSListener, sendTransaction, transacPackage }
})
