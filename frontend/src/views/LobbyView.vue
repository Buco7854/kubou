<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const route = useRoute()
const router = useRouter()
const pin = route.params.pin as string
const players = ref<any[]>([])
const isHost = ref(false) // In a real app, determine this from backend or local state
const stompClient = ref<Client | null>(null)
const gameId = ref<string | null>(null) // We need gameId to start, but pin is for lobby

// For simplicity, assuming pin maps to gameId or we get gameId from join response
// In the backend code provided, joinLobby uses PIN.
// But startGame uses gameId. We might need to fetch gameId from PIN or assume PIN=ID for now if not specified.
// Looking at backend: GameSession has ID and PIN. They might be different.
// Let's assume for now we can get game info.

const connectWebSocket = () => {
  const token = localStorage.getItem('token')
  const socket = new SockJS('http://localhost:8080/ws')

  stompClient.value = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
      Authorization: `Bearer ${token}`
    },
    onConnect: () => {
      console.log('Connected to WS')

      // Subscribe to lobby updates
      stompClient.value?.subscribe(`/topic/lobby/${pin}/players`, (message) => {
        players.value = JSON.parse(message.body)
      })

      // Subscribe to game start
      // We need the gameId for this subscription usually, or maybe topic uses PIN?
      // Backend: messagingTemplate.convertAndSend("/topic/game/" + gameId + "/started", "Game Started!");
      // We need to know gameId.
      // For now, let's assume we can get it or the topic uses PIN in a real scenario.
      // Let's try to subscribe to a topic that might notify us.
      // Or maybe we just wait for the host to click start.

      // Join the lobby
      stompClient.value?.publish({
        destination: '/app/lobby/join',
        body: JSON.stringify({ pin: pin })
      })
    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message'])
      console.error('Additional details: ' + frame.body)
    }
  })

  stompClient.value.activate()
}

const startGame = () => {
    // This requires gameId. We need to fetch the game session details by PIN first to get ID.
    // Or we can update backend to accept PIN for start.
    // For this prototype, let's assume we have a way to get gameId or just use PIN if they are same (unlikely).
    // Let's fetch game details by PIN via REST first.
    console.log("Start game clicked")
    // Implementation pending backend endpoint to get gameId from PIN
}

onMounted(() => {
  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient.value) {
    stompClient.value.deactivate()
  }
})
</script>

<template>
  <div class="min-h-screen bg-indigo-600 flex flex-col items-center justify-center text-white">
    <div class="text-center mb-12">
      <h1 class="text-6xl font-bold mb-4">PIN: {{ pin }}</h1>
      <p class="text-xl">En attente des joueurs...</p>
    </div>

    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 w-full max-w-4xl px-4">
      <div v-for="player in players" :key="player.id" class="bg-white text-indigo-600 p-4 rounded-lg shadow-lg text-center font-bold text-xl animate-bounce">
        {{ player.nickname }}
      </div>
    </div>

    <div class="mt-12" v-if="players.length > 0"> <!-- Show start button if host -->
       <button @click="startGame" class="bg-white text-indigo-600 px-8 py-3 rounded-full font-bold text-xl hover:bg-gray-100 transition shadow-lg">
         Commencer la Partie
       </button>
    </div>

    <div class="mt-8 text-sm opacity-75">
      {{ players.length }} joueur(s) connecté(s)
    </div>
  </div>
</template>
