<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
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
  correctAnswerIndex?: number // Only for host
}

const currentQuestion = ref<Question | null>(null)
const timeRemaining = ref(30) // Default 30s
const timerInterval = ref<number | null>(null)
const selectedOptionIndex = ref<number | null>(null)
const answerSubmitted = ref(false)
const pointsAwarded = ref<number | null>(null)
const leaderboard = ref<any[]>([])
const teams = ref<any[]>([])
const players = ref<any[]>([]) // Store all players to map IDs
const showLeaderboard = ref(false)
const showCorrectAnswer = ref(false)
const correctAnswerIndex = ref<number | null>(null)
const correctAnswerText = ref<string | null>(null)
const isHost = ref(false)
const achievements = ref<any[]>([])
const answeredCount = ref(0)
const totalPlayers = ref(0)
const roundFinished = ref(false)
const gameFinished = ref(false)
const winner = ref<any>(null)
const totalQuestions = ref(0)
const currentQuestionIndex = ref(-1) // Start at -1 so first question (index 0) increments to 0
const myTeamName = ref<string | null>(null)

const updateMyTeamName = () => {
    if (!isHost.value && currentUserId.value && players.value.length > 0) {
        const myPlayer = players.value.find((p: any) => p.userId === currentUserId.value)
        if (myPlayer) {
            const myTeam = teams.value.find((t: any) => t.playerIds && t.playerIds.includes(myPlayer.id))
            if (myTeam) {
                myTeamName.value = myTeam.name
            }
        }
    }
}

const resetRound = () => {
  selectedOptionIndex.value = null
  answerSubmitted.value = false
  pointsAwarded.value = null
  showLeaderboard.value = false
  showCorrectAnswer.value = false
  correctAnswerIndex.value = null
  correctAnswerText.value = null
  roundFinished.value = false
  timeRemaining.value = 30
  answeredCount.value = 0

  if (timerInterval.value) clearInterval(timerInterval.value)

  timerInterval.value = setInterval(() => {
    if (timeRemaining.value > 0) {
      timeRemaining.value--
    } else {
      if (timerInterval.value) clearInterval(timerInterval.value)
      // Time up!
      roundFinished.value = true
      showCorrectAnswer.value = true
    }
  }, 1000)
}

const handleNewQuestion = (question: any) => {
    // Show "Get Ready" screen
    currentQuestion.value = null
    showLeaderboard.value = false
    showCorrectAnswer.value = false
    roundFinished.value = false
    pointsAwarded.value = null

    // Wait 3 seconds before showing the question
    setTimeout(() => {
        currentQuestion.value = question
        currentQuestionIndex.value++
        resetRound()
    }, 3000)
}

const fetchGameDetails = async () => {
    try {
        const token = authStore.token
        const response = await axios.get(`/api/v1/games/${gameId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })

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

        if (response.data.players) {
            players.value = response.data.players
            totalPlayers.value = response.data.players.length
        }

        if (response.data.teams) {
            teams.value = response.data.teams
            updateMyTeamName()
        }

        // Only set index if game is in progress, otherwise keep -1 for Lobby
        if (response.data.state === 'IN_PROGRESS' || response.data.state === 'QUESTION_RESULTS') {
            currentQuestionIndex.value = response.data.currentQuestionIndex
        } else {
            currentQuestionIndex.value = -1
        }

        // Fetch quiz details to get total questions
        const quizResponse = await axios.get(`/api/v1/quizzes/${response.data.quizId}`, {
             headers: { Authorization: `Bearer ${token}` }
        })
        totalQuestions.value = quizResponse.data.questions.length

        // Fetch current question state if game is in progress
        if (response.data.state === 'IN_PROGRESS' && response.data.currentQuestionIndex >= 0) {
             const question = quizResponse.data.questions[response.data.currentQuestionIndex]
             if (question) {
                 const questionData = {
                     id: question.id,
                     text: question.text,
                     options: question.options,
                     difficultyLevel: question.difficultyLevel,
                     correctAnswerIndex: undefined
                 }

                 // If it's the first question, force the "Get Ready" delay so players see their team
                 if (response.data.currentQuestionIndex === 0) {
                     currentQuestionIndex.value = -1 // Reset to -1 so handleNewQuestion increments to 0
                     handleNewQuestion(questionData)
                 } else {
                     // Otherwise show immediately
                     currentQuestion.value = questionData
                     resetRound()
                 }
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

      // Subscribe to questions (Players)
      stompClient.value?.subscribe(`/topic/game/${gameId}/question`, (message) => {
        if (!isHost.value) {
            const question = JSON.parse(message.body)
            handleNewQuestion(question)
        }
      })

      // Subscribe to full questions (Host)
      if (isHost.value) {
          stompClient.value?.subscribe(`/topic/game/${gameId}/host/question`, (message) => {
            const question = JSON.parse(message.body)
            const questionData = {
                ...question,
                correctAnswerIndex: undefined
            }
            handleNewQuestion(questionData)
          })

          // Subscribe to progress updates
          stompClient.value?.subscribe(`/topic/game/${gameId}/progress`, (message) => {
              const progress = JSON.parse(message.body)
              answeredCount.value = progress.answeredCount
              totalPlayers.value = progress.totalPlayers
          })
      }

      // Subscribe to round end
      stompClient.value?.subscribe(`/topic/game/${gameId}/round_end`, (message) => {
          roundFinished.value = true
          if (timerInterval.value) clearInterval(timerInterval.value)
          // Initially show correct answer, then allow toggle to leaderboard
          showCorrectAnswer.value = true
          showLeaderboard.value = false
      })

      // Subscribe to correct answer
      stompClient.value?.subscribe(`/topic/game/${gameId}/answer`, (message) => {
          const data = JSON.parse(message.body)
          correctAnswerIndex.value = data.correctAnswerIndex
          correctAnswerText.value = data.correctAnswerText
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
      })

      // Subscribe to teams
      stompClient.value?.subscribe(`/topic/game/${gameId}/teams`, (message) => {
        teams.value = JSON.parse(message.body)
        updateMyTeamName()
      })

      // Subscribe to game finished
      stompClient.value?.subscribe(`/topic/game/${gameId}/finished`, (message) => {
        gameFinished.value = true
        roundFinished.value = true
        showLeaderboard.value = true
        showCorrectAnswer.value = false
        if (leaderboard.value.length > 0) {
            winner.value = leaderboard.value[0]
        }
      })
    },
    onStompError: (frame) => {
      console.error('Broker reported error: ' + frame.headers['message'])
    }
  })

  stompClient.value.activate()
}

const submitAnswer = (index: number) => {
  if (answerSubmitted.value) return

  // Prevent host from answering
  if (isHost.value) return

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

const toggleView = () => {
    showLeaderboard.value = !showLeaderboard.value
    showCorrectAnswer.value = !showCorrectAnswer.value
}

const currentUserId = computed(() => {
    const token = authStore.token
    if (token) {
        try {
            const tokenPayload = JSON.parse(atob(token.split('.')[1]))
            return tokenPayload.sub
        } catch (e) {
            return null
        }
    }
    return null
})

const returnToHome = () => {
    router.push('/')
}

const podium = computed(() => {
    return leaderboard.value.slice(0, 3)
})

const remainingLeaderboard = computed(() => {
    return leaderboard.value.slice(3)
})

const isLastQuestion = computed(() => {
    // currentQuestionIndex is 0-based, totalQuestions is count.
    // If we are at index 2 and total is 3, it's the last question.
    // However, currentQuestionIndex is incremented when a new question arrives.
    // Let's assume currentQuestionIndex tracks the *current* question being played.
    // We need to be careful about when it's updated.
    // In fetchGameDetails, we get the index from DB.
    // In websocket, we increment it.
    // So if we are at the last question, index + 1 == total.
    return currentQuestionIndex.value + 1 >= totalQuestions.value
})

const getPlayerTeamName = (userId: string) => {
    if (teams.value.length === 0) return null
    const player = players.value.find((p: any) => p.userId === userId)
    if (!player) return null
    const team = teams.value.find(t => t.playerIds && t.playerIds.includes(player.id))
    return team ? team.name : null
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
      <div class="relative" v-if="!gameFinished">
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

      <!-- Game Over View -->
      <div v-if="gameFinished" class="text-center animate-fade-in w-full max-w-5xl">
          <div class="mb-8">
              <h1 class="text-6xl font-black text-transparent bg-clip-text bg-gradient-to-r from-yellow-400 via-orange-500 to-red-500 mb-4 animate-pulse">
                  PARTIE TERMINÉE !
              </h1>
              <p class="text-2xl text-gray-300">Merci d'avoir joué !</p>
          </div>

          <!-- Podium -->
          <div class="flex justify-center items-end space-x-4 mb-12 h-80">
              <!-- 2nd Place -->
              <div v-if="podium.length > 1" class="flex flex-col items-center animate-slide-up" style="animation-delay: 0.2s">
                  <div class="bg-gray-400 w-24 h-32 rounded-t-lg flex flex-col justify-between items-center pb-4 pt-10 shadow-lg relative z-10">
                      <div class="absolute -top-8 w-16 h-16 bg-gray-300 rounded-full border-4 border-gray-500 flex items-center justify-center text-2xl shadow-md">🥈</div>
                      <div class="text-lg font-bold text-gray-800 truncate max-w-[90px] px-1 z-20">{{ podium[1].nickname }}</div>
                      <span class="text-4xl font-black text-gray-600">2</span>
                  </div>
                  <div class="mt-2 font-bold text-gray-300">{{ podium[1].score }} pts</div>
              </div>

              <!-- 1st Place -->
              <div v-if="podium.length > 0" class="flex flex-col items-center animate-slide-up z-20">
                  <div class="text-6xl mb-4 animate-bounce">👑</div>
                  <div class="bg-yellow-400 w-32 h-48 rounded-t-lg flex flex-col justify-between items-center pb-4 pt-12 shadow-2xl shadow-yellow-500/50 relative z-10">
                      <div class="absolute -top-10 w-20 h-20 bg-yellow-300 rounded-full border-4 border-yellow-500 flex items-center justify-center text-4xl shadow-md">🥇</div>
                      <div class="text-xl font-black text-yellow-900 truncate max-w-[120px] px-1 z-20">{{ podium[0].nickname }}</div>
                      <span class="text-6xl font-black text-yellow-600">1</span>
                  </div>
                  <div class="mt-2 font-black text-yellow-400 text-xl">{{ podium[0].score }} pts</div>
              </div>

              <!-- 3rd Place -->
              <div v-if="podium.length > 2" class="flex flex-col items-center animate-slide-up" style="animation-delay: 0.4s">
                  <div class="bg-orange-400 w-24 h-28 rounded-t-lg flex flex-col justify-between items-center pb-4 pt-10 shadow-lg relative z-10">
                      <div class="absolute -top-8 w-16 h-16 bg-orange-300 rounded-full border-4 border-orange-600 flex items-center justify-center text-2xl shadow-md">🥉</div>
                      <div class="text-lg font-bold text-orange-900 truncate max-w-[90px] px-1 z-20">{{ podium[2].nickname }}</div>
                      <span class="text-4xl font-black text-orange-700">3</span>
                  </div>
                  <div class="mt-2 font-bold text-orange-300">{{ podium[2].score }} pts</div>
              </div>
          </div>

          <!-- Remaining Leaderboard -->
          <div v-if="remainingLeaderboard.length > 0" class="bg-white/5 backdrop-blur-md rounded-2xl overflow-hidden border border-white/10 max-h-64 overflow-y-auto custom-scrollbar w-full max-w-2xl mx-auto">
              <div v-for="(player, index) in remainingLeaderboard" :key="index"
                   class="flex items-center justify-between p-4 hover:bg-white/5 transition border-b border-white/5 last:border-0"
                   :class="{'bg-indigo-500/20': player.userId === currentUserId}">
                  <div class="flex items-center">
                      <div class="w-8 h-8 rounded-full bg-gray-700 text-gray-400 flex items-center justify-center font-bold mr-4">
                          {{ index + 4 }}
                      </div>
                      <span class="text-lg font-medium">{{ player.nickname }}</span>
                  </div>
                  <span class="text-xl font-bold text-indigo-300">{{ player.score }}</span>
              </div>
          </div>

          <div class="mt-12">
              <button @click="returnToHome" class="bg-white text-indigo-900 px-8 py-3 rounded-full font-bold text-xl hover:bg-gray-100 transition shadow-lg">
                  Retour à l'accueil
              </button>
          </div>
      </div>

      <!-- Question View (Active or Correct Answer) -->
      <div v-else-if="currentQuestion && !showLeaderboard" class="w-full max-w-5xl flex flex-col h-full justify-center">
        <div class="bg-white text-gray-900 p-10 rounded-2xl shadow-2xl mb-10 text-center transform transition-all hover:scale-[1.01]">
          <h2 class="text-3xl md:text-5xl font-bold leading-tight">{{ currentQuestion.text }}</h2>

          <!-- Host Info -->
          <div v-if="isHost" class="mt-4 flex justify-center space-x-8 text-gray-500">
              <div class="flex flex-col items-center">
                  <span class="text-sm uppercase tracking-wider font-bold">Réponses</span>
                  <span class="text-2xl font-black text-indigo-600">{{ answeredCount }} / {{ totalPlayers }}</span>
              </div>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 w-full">
          <button
            v-for="(option, index) in currentQuestion.options"
            :key="index"
            @click="submitAnswer(index)"
            :disabled="answerSubmitted || isHost || roundFinished"
            :class="[
              'p-8 rounded-xl text-2xl font-bold text-white shadow-lg transition-all duration-200 transform hover:scale-[1.02] active:scale-95 flex items-center justify-center h-32 relative overflow-hidden',
              index === 0 ? 'bg-red-500 hover:bg-red-600 shadow-red-500/50' :
              index === 1 ? 'bg-blue-500 hover:bg-blue-600 shadow-blue-500/50' :
              index === 2 ? 'bg-yellow-500 hover:bg-yellow-600 shadow-yellow-500/50' :
              'bg-green-500 hover:bg-green-600 shadow-green-500/50',
              (answerSubmitted || isHost || roundFinished) && selectedOptionIndex !== index && correctAnswerIndex !== index ? 'opacity-40 grayscale cursor-not-allowed' : '',
              answerSubmitted && selectedOptionIndex === index ? 'ring-8 ring-white scale-105 z-10' : '',
              isHost ? 'cursor-default hover:scale-100 opacity-100 grayscale-0' : '', // Host sees colors but disabled
              showCorrectAnswer && correctAnswerIndex === index ? 'ring-8 ring-green-400 scale-105 z-20 animate-pulse' : '',
              showCorrectAnswer && correctAnswerIndex !== index ? 'opacity-20' : ''
            ]"
          >
            <span class="mr-4 opacity-50 text-3xl">
                {{ index === 0 ? '▲' : index === 1 ? '◆' : index === 2 ? '●' : '■' }}
            </span>
            {{ option }}

            <div v-if="showCorrectAnswer && correctAnswerIndex === index" class="absolute top-2 right-2 bg-white text-green-600 rounded-full p-1 shadow-md">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>
            </div>
          </button>
        </div>

        <div v-if="pointsAwarded !== null && !showCorrectAnswer" class="mt-8 text-center animate-bounce-in">
          <div class="inline-block bg-green-500 text-white px-8 py-4 rounded-full text-3xl font-black shadow-xl border-4 border-green-300">
            +{{ pointsAwarded }} Points!
          </div>
        </div>

        <!-- Removed redundant correct answer text block -->

        <div v-if="isHost || roundFinished" class="mt-8 text-center flex justify-center space-x-4">
            <button v-if="roundFinished" @click="toggleView" class="bg-indigo-600 text-white px-8 py-4 rounded-full hover:bg-indigo-500 transition font-bold shadow-lg text-xl">
                {{ showLeaderboard ? 'Voir Réponse' : 'Voir Classement' }}
            </button>

            <!-- Next Question button moved to Leaderboard view for Host -->
        </div>
      </div>

      <!-- Leaderboard View -->
      <div v-else-if="showLeaderboard && !gameFinished" class="w-full max-w-6xl mx-auto animate-fade-in flex flex-col items-center gap-8">

        <div class="w-full grid grid-cols-1 gap-8" :class="teams.length > 0 ? 'md:grid-cols-2' : 'max-w-3xl'">
            <!-- Player Leaderboard -->
            <div class="bg-white/10 backdrop-blur-lg rounded-2xl shadow-2xl overflow-hidden border border-white/10">
                <div class="bg-indigo-600 p-4 text-center">
                    <h2 class="text-2xl font-bold text-white uppercase tracking-wider">🏆 Classement Joueurs</h2>
                </div>
                <div class="divide-y divide-white/10">
                  <div v-for="(player, index) in leaderboard" :key="index"
                       class="flex items-center justify-between p-5 transition"
                       :class="{'bg-indigo-500/30 border-l-4 border-indigo-400': player.userId === currentUserId, 'hover:bg-white/5': player.userId !== currentUserId}">
                    <div class="flex items-center">
                      <div class="w-10 h-10 rounded-full flex items-center justify-center font-black text-xl mr-4"
                           :class="index === 0 ? 'bg-yellow-400 text-yellow-900' : index === 1 ? 'bg-gray-300 text-gray-800' : index === 2 ? 'bg-orange-400 text-orange-900' : 'bg-gray-700 text-gray-400'">
                          {{ index + 1 }}
                      </div>
                      <div class="flex flex-col">
                          <span class="text-xl font-semibold truncate max-w-[150px]">{{ player.nickname }} <span v-if="player.userId === currentUserId" class="text-xs bg-indigo-500 px-2 py-0.5 rounded ml-2">Moi</span></span>
                          <span v-if="teams.length > 0" class="text-xs text-gray-400 uppercase tracking-wider font-bold">
                              {{ player.teamName || getPlayerTeamName(player.userId) }}
                          </span>
                      </div>
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
        </div>

        <div class="flex justify-center space-x-6 mt-4">
             <button @click="toggleView" class="bg-indigo-600 text-white px-8 py-4 rounded-full hover:bg-indigo-500 transition font-bold shadow-lg text-xl">
                Voir Réponse
            </button>

            <button v-if="isHost" @click="nextQuestion"
                  class="bg-gradient-to-r from-green-500 to-emerald-600 text-white px-12 py-4 rounded-full font-bold text-2xl hover:from-green-600 hover:to-emerald-700 transition transform hover:scale-105 shadow-xl ring-4 ring-white/20">
            {{ isLastQuestion ? 'Terminer la Partie 🏁' : 'Question Suivante ➡️' }}
          </button>
        </div>
        <div class="mt-8 text-center" v-if="!isHost">
            <div class="inline-block animate-bounce bg-white/20 px-6 py-3 rounded-full text-lg font-medium">
                ⏳ En attente de l'hôte...
            </div>
        </div>
      </div>

      <div v-if="!currentQuestion && !showLeaderboard && !gameFinished" class="text-center">
        <div class="text-6xl mb-4 animate-spin-slow">🎲</div>
        <h2 class="text-3xl font-bold text-white/80">Préparez-vous...</h2>
        <div v-if="myTeamName" class="mt-4 text-xl text-purple-300 font-bold animate-pulse">
            Vous êtes dans l'équipe : {{ myTeamName }}
        </div>
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
.animate-slide-up {
    animation: slideUp 0.8s ease-out forwards;
    opacity: 0;
    transform: translateY(50px);
}
@keyframes slideUp {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
