<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, Ref } from 'vue'
import { io } from 'socket.io-client'

let message: Ref<Array<any>> = ref([])
const refresh = ref(false)

const socket = io('127.0.0.1:3001', {
  transports: ['websocket'],
})

socket.on('connect', () => {
  refresh.value = false
  console.log('Connected to server port 3001')
})

socket.on('disconnect', () => {
  refresh.value = false
  console.log('Disconnected to server port 3001')
})

socket.on('wallet', (data) => {
  console.log(data)
  message.value.push(JSON.parse(data))
  refresh.value = true
})
</script>

<template>
  <div>Wallet</div>
  <div v-if="refresh" v-for="(item, index) in message" :key="index">
    <p>Address: {{ item }}</p>
    <hr />
  </div>
</template>
