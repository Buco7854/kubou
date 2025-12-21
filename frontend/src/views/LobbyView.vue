<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const pin = route.params.pin as string
const players = ref<any[]>([])
const isHost = ref(false)
const stompClient = ref<Client | null>(null)
const gameId = ref<string | null>(null)

const fetchGameDetails = async () => {
    try {
        const token = authStore.token
        const headers = token ? { Authorization: `Bearer ${token}` } : {}

        const response = await axios.get(`/api/v1/games/by-pin/${pin}`, { headers })
        gameId.value = response.data.id

        // Populate players from initial fetch
        if (response.data.players) {
            players.value = response.data.players
        }

        // Check if current user is host
        if (token) {
            try {
                const tokenPayload = JSON.parse(atob(token.split('.')[1]))
                if (response.data.hostId === tokenPayload.sub) {
                    isHost.value = true
                }
            } catch (e) {
                console.error("Error parsing token", e)
            }
        }
    } catch (error) {
        console.error("Failed to fetch game details", error)
        alert("Impossible de trouver la partie ou erreur de connexion.")
        router.push('/')
    }
}

const connectWebSocket = () => {
  const token = authStore.token
  if (!token) {
      router.push({ name: 'login', query: { redirect: `/lobby/${pin}` } })
      return
  }

  const socket = new SockJS('http://localhost:8080/ws')

  stompClient.value = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
      Authorization: `Bearer ${token}`
    },
    onConnect: () => {
      console.log('Connected to WS')

      // Subscribe to lobby updates (PIN based for players)
      stompClient.value?.subscribe(`/topic/lobby/${pin}/players`, (message) => {
        console.log("Received lobby update (PIN):", message.body)
        players.value = JSON.parse(message.body)
      })

      // If host, subscribe to ID based updates as well
      if (isHost.value && gameId.value) {
          stompClient.value?.subscribe(`/topic/lobby/${gameId.value}/players`, (message) => {
            console.log("Received lobby update (ID):", message.body)
            players.value = JSON.parse(message.body)
          })
      }

      // Subscribe to personal queue to get Game ID when joining (only for players)
      if (!isHost.value) {
          stompClient.value?.subscribe(`/user/queue/lobby/joined`, (message) => {
              const data = JSON.parse(message.body)
              console.log("Joined game with ID:", data.gameId)
              gameId.value = data.gameId

              // Now subscribe to game start on the specific game ID
              stompClient.value?.subscribe(`/topic/game/${data.gameId}/started`, (msg) => {
                console.log("Game started!", msg.body)
                router.push(`/game/${data.gameId}`)
              })
          })
      } else if (gameId.value) {
          // Host immediately subscribes to game start
          stompClient.value?.subscribe(`/topic/game/${gameId.value}/started`, (message) => {
            console.log("Game started!", message.body)
            router.push(`/game/${gameId.value}`)
          })
      }

      // Join the lobby.
      // FIX: Even if logged in (and not host), we must send the join message to register in the session.
      // The previous logic `if (!isHost.value)` was correct for the host (who shouldn't join as player),
      // but we need to ensure logged-in users who are NOT the host DO join.
      if (!isHost.value) {
          stompClient.value?.publish({
            destination: '/app/lobby/join',
            body: JSON.stringify({ pin: pin })
          })
      }
    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message'])
      console.error('Additional details: ' + frame.body)
    }
  })

  stompClient.value.activate()
}

const startGame = () => {
    if (!gameId.value) return

    stompClient.value?.publish({
        destination: `/app/game/${gameId.value}/start`,
        body: JSON.stringify({})
    })
}

onMounted(async () => {
  await fetchGameDetails()

  if (!authStore.isLoggedIn) {
      router.push({ name: 'login', query: { redirect: route.fullPath } })
      return
  }

  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient.value) {
    stompClient.value.deactivate()
  }
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-indigo-900 to-purple-800 flex flex-col items-center justify-center text-white p-4">
    <div class="text-center mb-12 animate-fade-in-down">
      <h1 class="text-7xl font-extrabold mb-4 tracking-tighter drop-shadow-lg">PIN: {{ pin }}</h1>
      <p class="text-2xl font-light opacity-90">En attente des joueurs...</p>
    </div>

    <div class="w-full max-w-6xl">
        <div v-if="players.length === 0" class="text-center text-gray-400 text-xl italic animate-pulse">
            La salle est vide... pour l'instant.
        </div>
        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
          <div v-for="player in players" :key="player.id"
               class="bg-white/10 backdrop-blur-md border border-white/20 text-white p-6 rounded-xl shadow-xl text-center font-bold text-xl transform transition hover:scale-105 hover:bg-white/20 animate-pop-in">
            <div class="mb-2 text-4xl">👤</div>
            {{ player.nickname }}
          </div>
        </div>
    </div>

    <div class="mt-16" v-if="isHost">
       <button @click="startGame"
               class="bg-gradient-to-r from-green-400 to-blue-500 text-white px-12 py-4 rounded-full font-bold text-2xl hover:from-green-500 hover:to-blue-600 transition transform hover:scale-105 shadow-2xl ring-4 ring-white/30">
         🚀 Lancer la Partie
       </button>
    </div>

    <div class="fixed bottom-8 right-8 bg-black/50 backdrop-blur px-6 py-3 rounded-full text-sm font-mono border border-white/10">
      {{ players.length }} joueur(s) connecté(s)
    </div>
  </div>
</template>

<style scoped>
@keyframes pop-in {
  0% { opacity: 0; transform: scale(0.5); }
  100% { opacity: 1; transform: scale(1); }
}
.animate-pop-in {
  animation: pop-in 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}
.animate-fade-in-down {
    animation: fadeInDown 1s ease-out;
}
@keyframes fadeInDown {
    from { opacity: 0; transform: translateY(-20px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
