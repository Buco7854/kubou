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
const gameId = route.params.id as string
const stompClient = ref<Client | null>(null)

interface Question {
  id: string
  text: string
  options: string[]
  difficultyLevel: number
}

const currentQuestion = ref<Question | null>(null)
const timeRemaining = ref(30) // Default 30s
const timerInterval = ref<number | null>(null)
const selectedOptionIndex = ref<number | null>(null)
const answerSubmitted = ref(false)
const pointsAwarded = ref<number | null>(null)
const leaderboard = ref<any[]>([])
const teams = ref<any[]>([])
const showLeaderboard = ref(false)
const isHost = ref(false)
const achievements = ref<any[]>([])

const fetchGameDetails = async () => {
    try {
        const token = authStore.token
        const response = await axios.get(`/api/v1/games/${gameId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })

        if (token) {
            const tokenPayload = JSON.parse(atob(token.split('.')[1]))
            if (response.data.hostId === tokenPayload.sub) {
                isHost.value = true
            }
        }
    } catch (error) {
        console.error("Failed to fetch game details", error)
    }
}

const connectWebSocket = () => {
  const token = authStore.token
  if (!token) {
      router.push('/login')
      return
  }

  const socket = new SockJS('http://localhost:8080/ws')

  stompClient.value = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
      Authorization: `Bearer ${token}`
    },
    onConnect: () => {
      console.log('Connected to Game WS')

      // Subscribe to questions
      stompClient.value?.subscribe(`/topic/game/${gameId}/question`, (message) => {
        const question = JSON.parse(message.body)
        currentQuestion.value = question
        resetRound()
      })

      // Subscribe to results (personal)
      stompClient.value?.subscribe(`/user/queue/result`, (message) => {
        const result = JSON.parse(message.body)
        pointsAwarded.value = result.pointsAwarded
      })

      // Subscribe to achievements (personal)
      stompClient.value?.subscribe(`/user/queue/achievements`, (message) => {
        const achievement = JSON.parse(message.body)
        achievements.value.push(achievement)
        setTimeout(() => {
            achievements.value = achievements.value.filter(a => a.id !== achievement.id)
        }, 5000) // Hide after 5s
      })

      // Subscribe to leaderboard
      stompClient.value?.subscribe(`/topic/game/${gameId}/leaderboard`, (message) => {
        leaderboard.value = JSON.parse(message.body)
        showLeaderboard.value = true
      })

      // Subscribe to teams
      stompClient.value?.subscribe(`/topic/game/${gameId}/teams`, (message) => {
        teams.value = JSON.parse(message.body)
      })

      // Subscribe to game finished
      stompClient.value?.subscribe(`/topic/game/${gameId}/finished`, (message) => {
        alert("Game Over! " + message.body)
        router.push('/')
      })
    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message'])
    }
  })

  stompClient.value.activate()
}

const resetRound = () => {
  selectedOptionIndex.value = null
  answerSubmitted.value = false
  pointsAwarded.value = null
  showLeaderboard.value = false
  timeRemaining.value = 30

  if (timerInterval.value) clearInterval(timerInterval.value)

  timerInterval.value = setInterval(() => {
    if (timeRemaining.value > 0) {
      timeRemaining.value--
    } else {
      if (timerInterval.value) clearInterval(timerInterval.value)
      // Auto submit if not submitted? Or just show time up.
    }
  }, 1000)
}

const submitAnswer = (index: number) => {
  if (answerSubmitted.value) return

  selectedOptionIndex.value = index
  answerSubmitted.value = true

  const timeToAnswer = 30000 - (timeRemaining.value * 1000) // Approx

  stompClient.value?.publish({
    destination: `/app/game/${gameId}/submit`,
    body: JSON.stringify({
      questionId: currentQuestion.value?.id,
      answerIndex: index,
      timeToAnswerMs: timeToAnswer
    })
  })
}

const nextQuestion = () => {
  stompClient.value?.publish({
    destination: `/app/game/${gameId}/next`,
    body: JSON.stringify({})
  })
}

onMounted(async () => {
  if (!authStore.isLoggedIn) {
      router.push('/login')
      return
  }
  await fetchGameDetails()
  connectWebSocket()
})

onUnmounted(() => {
  if (stompClient.value) {
    stompClient.value.deactivate()
  }
  if (timerInterval.value) clearInterval(timerInterval.value)
})
</script>

<template>
  <div class="min-h-screen bg-gray-900 flex flex-col relative overflow-hidden font-sans text-white">

    <!-- Background Elements -->
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden z-0 opacity-20 pointer-events-none">
        <div class="absolute top-10 left-10 w-64 h-64 bg-purple-600 rounded-full mix-blend-multiply filter blur-3xl animate-blob"></div>
        <div class="absolute top-10 right-10 w-64 h-64 bg-yellow-600 rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-2000"></div>
        <div class="absolute -bottom-8 left-20 w-64 h-64 bg-pink-600 rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-4000"></div>
    </div>

    <!-- Achievements Overlay -->
    <div class="absolute top-24 right-4 z-50 space-y-3 pointer-events-none">
        <div v-for="achievement in achievements" :key="achievement.id"
             class="bg-gradient-to-r from-yellow-400 to-yellow-600 text-white p-4 shadow-2xl rounded-xl animate-slide-in flex items-center space-x-3 border-2 border-yellow-200">
            <div class="text-3xl">🏆</div>
            <div>
                <p class="font-bold text-lg">Succès Débloqué !</p>
                <p class="text-sm opacity-90">{{ achievement.type }}</p>
            </div>
        </div>
    </div>

    <!-- Header -->
    <div class="bg-white/10 backdrop-blur-md shadow-lg p-4 flex justify-between items-center z-10 border-b border-white/10">
      <div class="text-2xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-indigo-400 to-cyan-400">
        Kubou
      </div>
      <div class="relative">
          <div class="text-3xl font-black font-mono" :class="{'text-red-500 animate-pulse': timeRemaining < 10, 'text-white': timeRemaining >= 10}">
            {{ timeRemaining }}
          </div>
          <svg class="absolute -top-2 -right-2 w-12 h-12 -z-10 opacity-50" viewBox="0 0 100 100">
             <circle cx="50" cy="50" r="45" fill="none" stroke="currentColor" stroke-width="5" class="text-gray-700" />
             <circle cx="50" cy="50" r="45" fill="none" stroke="currentColor" stroke-width="5" class="text-indigo-500 transition-all duration-1000 ease-linear"
                     :stroke-dasharray="283" :stroke-dashoffset="283 * (1 - timeRemaining/30)" transform="rotate(-90 50 50)" />
          </svg>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-grow flex flex-col items-center justify-center p-4 z-10 w-full max-w-7xl mx-auto">

      <!-- Question View -->
      <div v-if="currentQuestion && !showLeaderboard" class="w-full max-w-5xl flex flex-col h-full justify-center">
        <div class="bg-white text-gray-900 p-10 rounded-2xl shadow-2xl mb-10 text-center transform transition-all hover:scale-[1.01]">
          <h2 class="text-3xl md:text-5xl font-bold leading-tight">{{ currentQuestion.text }}</h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 w-full">
          <button
            v-for="(option, index) in currentQuestion.options"
            :key="index"
            @click="submitAnswer(index)"
            :disabled="answerSubmitted"
            :class="[
              'p-8 rounded-xl text-2xl font-bold text-white shadow-lg transition-all duration-200 transform hover:scale-[1.02] active:scale-95 flex items-center justify-center h-32',
              index === 0 ? 'bg-red-500 hover:bg-red-600 shadow-red-500/50' :
              index === 1 ? 'bg-blue-500 hover:bg-blue-600 shadow-blue-500/50' :
              index === 2 ? 'bg-yellow-500 hover:bg-yellow-600 shadow-yellow-500/50' :
              'bg-green-500 hover:bg-green-600 shadow-green-500/50',
              answerSubmitted && selectedOptionIndex !== index ? 'opacity-40 grayscale cursor-not-allowed' : '',
              answerSubmitted && selectedOptionIndex === index ? 'ring-8 ring-white scale-105 z-10' : ''
            ]"
          >
            <span class="mr-4 opacity-50 text-3xl">
                {{ index === 0 ? '▲' : index === 1 ? '◆' : index === 2 ? '●' : '■' }}
            </span>
            {{ option }}
          </button>
        </div>

        <div v-if="pointsAwarded !== null" class="mt-8 text-center animate-bounce-in">
          <div class="inline-block bg-green-500 text-white px-8 py-4 rounded-full text-3xl font-black shadow-xl border-4 border-green-300">
            +{{ pointsAwarded }} Points!
          </div>
        </div>
      </div>

      <!-- Leaderboard View -->
      <div v-if="showLeaderboard" class="w-full max-w-6xl grid grid-cols-1 md:grid-cols-2 gap-8 animate-fade-in">
        <!-- Player Leaderboard -->
        <div class="bg-white/10 backdrop-blur-lg rounded-2xl shadow-2xl overflow-hidden border border-white/10">
            <div class="bg-indigo-600 p-4 text-center">
                <h2 class="text-2xl font-bold text-white uppercase tracking-wider">🏆 Classement Joueurs</h2>
            </div>
            <div class="divide-y divide-white/10">
              <div v-for="(player, index) in leaderboard" :key="index"
                   class="flex items-center justify-between p-5 hover:bg-white/5 transition">
                <div class="flex items-center">
                  <div class="w-10 h-10 rounded-full flex items-center justify-center font-black text-xl mr-4"
                       :class="index === 0 ? 'bg-yellow-400 text-yellow-900' : index === 1 ? 'bg-gray-300 text-gray-800' : index === 2 ? 'bg-orange-400 text-orange-900' : 'bg-gray-700 text-gray-400'">
                      {{ index + 1 }}
                  </div>
                  <span class="text-xl font-semibold truncate max-w-[150px]">{{ player.nickname }}</span>
                </div>
                <div class="flex items-center space-x-4">
                    <div v-if="player.currentStreak > 2" class="flex items-center text-orange-400 font-bold animate-pulse">
                        <span class="text-2xl mr-1">🔥</span> {{ player.currentStreak }}
                    </div>
                    <span class="text-2xl font-bold text-indigo-300">{{ player.score }}</span>
                </div>
              </div>
            </div>
        </div>

        <!-- Team Leaderboard (if active) -->
        <div v-if="teams.length > 0" class="bg-white/10 backdrop-blur-lg rounded-2xl shadow-2xl overflow-hidden border border-white/10">
            <div class="bg-purple-600 p-4 text-center">
                <h2 class="text-2xl font-bold text-white uppercase tracking-wider">⚔️ Classement Équipes</h2>
            </div>
            <div class="divide-y divide-white/10">
              <div v-for="(team, index) in teams.sort((a,b) => b.score - a.score)" :key="team.id"
                   class="flex items-center justify-between p-5 hover:bg-white/5 transition">
                <div class="flex items-center">
                  <span class="text-2xl font-bold w-8 text-gray-500 mr-4">#{{ index + 1 }}</span>
                  <span class="text-xl font-semibold">{{ team.name }}</span>
                </div>
                <span class="text-2xl font-bold text-purple-300">{{ team.score }}</span>
              </div>
            </div>
        </div>

        <div class="col-span-1 md:col-span-2 mt-8 text-center" v-if="isHost">
          <button @click="nextQuestion"
                  class="bg-gradient-to-r from-indigo-500 to-purple-600 text-white px-10 py-4 rounded-full font-bold text-2xl hover:from-indigo-600 hover:to-purple-700 transition transform hover:scale-105 shadow-xl ring-4 ring-white/20">
            Question Suivante ➡️
          </button>
        </div>
        <div class="col-span-1 md:col-span-2 mt-12 text-center" v-else>
            <div class="inline-block animate-bounce bg-white/20 px-6 py-3 rounded-full text-lg font-medium">
                ⏳ En attente de l'hôte...
            </div>
        </div>
      </div>

      <div v-if="!currentQuestion && !showLeaderboard" class="text-center">
        <div class="text-6xl mb-4 animate-spin-slow">🎲</div>
        <h2 class="text-3xl font-bold text-white/80">Préparez-vous...</h2>
      </div>

    </div>
  </div>
</template>

<style scoped>
.animate-blob {
  animation: blob 7s infinite;
}
.animation-delay-2000 {
  animation-delay: 2s;
}
.animation-delay-4000 {
  animation-delay: 4s;
}
@keyframes blob {
  0% { transform: translate(0px, 0px) scale(1); }
  33% { transform: translate(30px, -50px) scale(1.1); }
  66% { transform: translate(-20px, 20px) scale(0.9); }
  100% { transform: translate(0px, 0px) scale(1); }
}
.animate-bounce-in {
    animation: bounceIn 0.8s cubic-bezier(0.215, 0.610, 0.355, 1.000);
}
@keyframes bounceIn {
    0% { opacity: 0; transform: scale3d(.3, .3, .3); }
    20% { transform: scale3d(1.1, 1.1, 1.1); }
    40% { transform: scale3d(.9, .9, .9); }
    60% { opacity: 1; transform: scale3d(1.03, 1.03, 1.03); }
    80% { transform: scale3d(.97, .97, .97); }
    100% { opacity: 1; transform: scale3d(1, 1, 1); }
}
.animate-spin-slow {
    animation: spin 3s linear infinite;
}
@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}
</style>
