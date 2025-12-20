<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const route = useRoute()
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
const showLeaderboard = ref(false)
const isHost = ref(false) // Need to determine if host to show "Next" button

// Determine host status similar to LobbyView (omitted for brevity, assuming passed or re-checked)
// For now, let's just show Next button always or check token.

const connectWebSocket = () => {
  const token = localStorage.getItem('token')
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

      // Subscribe to leaderboard
      stompClient.value?.subscribe(`/topic/game/${gameId}/leaderboard`, (message) => {
        leaderboard.value = JSON.parse(message.body)
        showLeaderboard.value = true
      })

      // Subscribe to game finished
      stompClient.value?.subscribe(`/topic/game/${gameId}/finished`, (message) => {
        alert("Game Over! " + message.body)
        // Redirect to home or summary
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

onMounted(() => {
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
  <div class="min-h-screen bg-gray-100 flex flex-col">
    <!-- Header -->
    <div class="bg-white shadow-sm p-4 flex justify-between items-center">
      <div class="text-xl font-bold text-indigo-600">Kubou</div>
      <div class="text-2xl font-bold" :class="{'text-red-600': timeRemaining < 10}">{{ timeRemaining }}s</div>
    </div>

    <!-- Main Content -->
    <div class="flex-grow flex flex-col items-center justify-center p-4">

      <!-- Question View -->
      <div v-if="currentQuestion && !showLeaderboard" class="w-full max-w-4xl">
        <div class="bg-white p-8 rounded-lg shadow-md mb-8 text-center">
          <h2 class="text-3xl font-bold text-gray-800">{{ currentQuestion.text }}</h2>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <button
            v-for="(option, index) in currentQuestion.options"
            :key="index"
            @click="submitAnswer(index)"
            :disabled="answerSubmitted"
            :class="[
              'p-8 rounded-lg text-xl font-bold text-white shadow-md transition transform hover:scale-105',
              index === 0 ? 'bg-red-500 hover:bg-red-600' :
              index === 1 ? 'bg-blue-500 hover:bg-blue-600' :
              index === 2 ? 'bg-yellow-500 hover:bg-yellow-600' :
              'bg-green-500 hover:bg-green-600',
              answerSubmitted && selectedOptionIndex !== index ? 'opacity-50 cursor-not-allowed' : '',
              answerSubmitted && selectedOptionIndex === index ? 'ring-4 ring-black' : ''
            ]"
          >
            {{ option }}
          </button>
        </div>

        <div v-if="pointsAwarded !== null" class="mt-8 text-center animate-bounce">
          <div class="text-2xl font-bold text-green-600">
            +{{ pointsAwarded }} Points!
          </div>
        </div>
      </div>

      <!-- Leaderboard View -->
      <div v-if="showLeaderboard" class="w-full max-w-2xl">
        <h2 class="text-3xl font-bold text-center mb-8 text-indigo-900">Classement</h2>
        <div class="bg-white rounded-lg shadow-lg overflow-hidden">
          <div v-for="(player, index) in leaderboard" :key="index" class="flex items-center justify-between p-4 border-b last:border-b-0">
            <div class="flex items-center">
              <span class="text-2xl font-bold w-8 text-gray-400 mr-4">{{ index + 1 }}</span>
              <span class="text-xl font-semibold">{{ player.nickname }}</span>
            </div>
            <span class="text-xl font-bold text-indigo-600">{{ player.score }} pts</span>
          </div>
        </div>

        <div class="mt-8 text-center">
          <button @click="nextQuestion" class="bg-indigo-600 text-white px-8 py-3 rounded-full font-bold text-xl hover:bg-indigo-700 transition shadow-lg">
            Question Suivante
          </button>
        </div>
      </div>

      <div v-if="!currentQuestion && !showLeaderboard" class="text-center text-gray-500 text-xl">
        Préparez-vous...
      </div>

    </div>
  </div>
</template>
