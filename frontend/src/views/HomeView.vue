<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { useSmartReviewStore } from '../stores/smartReview' // Import the new store
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const smartReviewStore = useSmartReviewStore() // Initialize the new store
const gamePin = ref('')
const isGenerating = ref(false)
const activeSessions = ref<any[]>([])
const participatingSessions = ref<any[]>([])
const recentAchievements = ref<any[]>([])

// Metadata for display mapping (Duplicated from AchievementsView for now)
const achievementMetadata = computed<Record<string, { label: string, description: string, icon: string, color: string, bg: string }>>(() => ({
    'SNIPER': { label: t('achievements.sniper.label'), description: t('achievements.sniper.description'), icon: '🎯', color: 'text-red-600', bg: 'bg-red-100' },
    'FLASH': { label: t('achievements.flash.label'), description: t('achievements.flash.description'), icon: '⚡', color: 'text-yellow-600', bg: 'bg-yellow-100' },
    'MARATHON': { label: t('achievements.marathon.label'), description: t('achievements.marathon.description'), icon: '🏃', color: 'text-blue-600', bg: 'bg-blue-100' },
    'FIRST_GAME': { label: t('achievements.firstGame.label'), description: t('achievements.firstGame.description'), icon: '🐣', color: 'text-green-600', bg: 'bg-green-100' },
    'WINNER': { label: t('achievements.winner.label'), description: t('achievements.winner.description'), icon: '🥇', color: 'text-yellow-700', bg: 'bg-yellow-200' },
    'TOP_3': { label: t('achievements.top3.label'), description: t('achievements.top3.description'), icon: '🏅', color: 'text-gray-600', bg: 'bg-gray-200' },
    'PERFECT': { label: t('achievements.perfect.label'), description: t('achievements.perfect.description'), icon: '💎', color: 'text-purple-600', bg: 'bg-purple-100' }
}))

const getMetadata = (type: string) => {
    return achievementMetadata.value[type] || { label: type, description: t('achievements.defaultDescription'), icon: '🏆', color: 'text-gray-600', bg: 'bg-gray-100' }
}

const joinGame = () => {
  if (gamePin.value.trim()) {
    router.push(`/lobby/${gamePin.value}`)
  }
}

const fetchMySessions = async () => {
    if (authStore.isLoggedIn && !authStore.isGuest) {
        try {
            const token = authStore.token
            const response = await axios.get('/api/v1/games/my-sessions', {
                headers: { Authorization: `Bearer ${token}` }
            })
            activeSessions.value = response.data.filter((s: any) => s.state !== 'FINISHED')
        } catch (error) {
            console.error("Failed to fetch sessions", error)
        }
    }
}

const fetchParticipatingSessions = async () => {
    if (authStore.isLoggedIn) {
        try {
            const token = authStore.token
            const response = await axios.get('/api/v1/games/participating', {
                headers: { Authorization: `Bearer ${token}` }
            })
            participatingSessions.value = response.data.filter((s: any) => s.state !== 'FINISHED')
        } catch (error) {
            console.error("Failed to fetch participating sessions", error)
        }
    }
}

const fetchRecentAchievements = async () => {
    if (authStore.isLoggedIn && !authStore.isGuest) {
        try {
            const token = authStore.token
            const response = await axios.get('/api/v1/achievements/status', {
                headers: { Authorization: `Bearer ${token}` }
            })
            // Take last 3 achievements
            if (response.data.achievements) {
                recentAchievements.value = response.data.achievements.slice(-3).reverse()
            }
        } catch (error) {
            console.error("Failed to fetch achievements", error)
        }
    }
}

const resumeSession = (pin: string) => {
    router.push(`/lobby/${pin}`)
}

const closeSession = async (gameId: string) => {
    if (!confirm(t('home.confirmCloseSession'))) return;
    try {
        const token = authStore.token
        await axios.delete(`/api/v1/games/${gameId}`, {
             headers: { Authorization: `Bearer ${token}` }
        })
        fetchMySessions()
    } catch (error) {
        console.error("Failed to close session", error)
        alert(t('home.closeSessionError'))
    }
}

const leaveSession = async (gameId: string) => {
    if (!confirm(t('home.confirmLeaveSession'))) return;
    participatingSessions.value = participatingSessions.value.filter(s => s.id !== gameId);
}

const generateSmartReview = async () => {
    if (!authStore.isLoggedIn) {
        alert(t('home.loginRequired'))
        return
    }
    isGenerating.value = true
    try {
        const token = authStore.token
        const response = await axios.post('/api/v1/smart-review/generate', {}, {
            headers: { Authorization: `Bearer ${token}` }
        })

        if (response.status === 204 || response.data.questions.length === 0) { // Check for empty questions array
            alert(t('home.noErrorsForReview'))
        } else {
            const quiz = response.data
            smartReviewStore.setSmartReviewQuiz(quiz) // Store the quiz in Pinia
            alert(t('home.reviewGenerated', { title: quiz.title }))
            router.push(`/smart-review-quiz`) // Navigate to the new route
        }
    } catch (error) {
        console.error("Failed to generate review", error)
        alert(t('home.reviewError'))
    } finally {
        isGenerating.value = false
    }
}

onMounted(() => {
    fetchMySessions()
    fetchParticipatingSessions()
    fetchRecentAchievements()
})
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-indigo-50 to-purple-100 flex flex-col items-center justify-center p-4">
    <div class="max-w-4xl w-full text-center space-y-12">

        <!-- Hero Section -->
        <div class="space-y-4 animate-fade-in-down">
            <h1 class="text-6xl md:text-8xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600 tracking-tight">
                Kubou
            </h1>
            <p class="text-xl md:text-2xl text-gray-600 font-light max-w-2xl mx-auto">
                {{ t('home.tagline') }}
            </p>
        </div>

        <!-- Active Sessions (Host) -->
        <div v-if="activeSessions.length > 0" class="max-w-md mx-auto animate-fade-in-down">
            <div class="bg-white/80 backdrop-blur-sm p-4 rounded-xl border border-indigo-200 shadow-lg">
                <h3 class="text-lg font-bold text-indigo-900 mb-3">🔴 {{ t('home.hostSessions') }}</h3>
                <div class="space-y-2">
                    <div v-for="session in activeSessions" :key="session.id"
                         class="flex justify-between items-center p-3 bg-white rounded-lg border border-indigo-100 hover:border-indigo-300 transition shadow-sm hover:shadow-md">
                        <div class="text-left cursor-pointer flex-grow" @click="resumeSession(session.pin)">
                            <span class="block font-mono text-xl font-bold text-indigo-600 tracking-widest">{{ session.pin }}</span>
                            <span class="text-xs text-gray-500">{{ session.state }}</span>
                        </div>
                        <div class="flex space-x-2">
                            <button @click="resumeSession(session.pin)" class="text-sm bg-indigo-100 text-indigo-700 px-3 py-1 rounded-full font-medium hover:bg-indigo-200">
                                {{ t('home.resume') }}
                            </button>
                            <button @click="closeSession(session.id)" class="text-sm bg-red-100 text-red-700 px-3 py-1 rounded-full font-medium hover:bg-red-200">
                                {{ t('home.close') }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Participating Sessions (Player) -->
        <div v-if="participatingSessions.length > 0" class="max-w-md mx-auto animate-fade-in-down">
            <div class="bg-white/80 backdrop-blur-sm p-4 rounded-xl border border-green-200 shadow-lg">
                <h3 class="text-lg font-bold text-green-900 mb-3">🟢 {{ t('home.playerSessions') }}</h3>
                <div class="space-y-2">
                    <div v-for="session in participatingSessions" :key="session.id"
                         class="flex justify-between items-center p-3 bg-white rounded-lg border border-green-100 hover:border-green-300 transition shadow-sm hover:shadow-md">
                        <div class="text-left cursor-pointer flex-grow" @click="resumeSession(session.pin)">
                            <span class="block font-mono text-xl font-bold text-green-600 tracking-widest">{{ session.pin }}</span>
                            <span class="text-xs text-gray-500">{{ t('home.participant') }}</span>
                        </div>
                        <div class="flex space-x-2">
                            <button @click="resumeSession(session.pin)" class="text-sm bg-green-100 text-green-700 px-3 py-1 rounded-full font-medium hover:bg-green-200">
                                {{ t('home.join') }}
                            </button>
                            <button @click="leaveSession(session.id)" class="text-sm bg-gray-100 text-gray-700 px-3 py-1 rounded-full font-medium hover:bg-gray-200">
                                {{ t('home.leave') }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Game Join Card -->
        <div class="max-w-md mx-auto bg-white p-8 rounded-2xl shadow-2xl transform transition hover:scale-[1.02] duration-300 border border-gray-100">
            <form @submit.prevent="joinGame" class="flex flex-col space-y-6">
                <div>
                    <label class="block text-sm font-bold text-gray-500 mb-2 uppercase tracking-wide">{{ t('home.joinGame') }}</label>
                    <input
                    v-model="gamePin"
                    type="text"
                    :placeholder="t('home.gamePin')"
                    class="w-full text-center text-3xl font-mono tracking-[0.5em] p-4 border-2 border-gray-200 rounded-xl focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 focus:outline-none transition-all uppercase placeholder-gray-300"
                    maxlength="6"
                    />
                </div>
                <button
                type="submit"
                class="w-full bg-indigo-600 text-white font-bold py-4 rounded-xl text-lg hover:bg-indigo-700 transition shadow-lg hover:shadow-xl transform active:scale-95"
                >
                {{ t('home.enterArena') }}
                </button>
            </form>
        </div>

        <!-- Achievements Preview (Authenticated & Not Guest) -->
        <div v-if="recentAchievements.length > 0" class="max-w-2xl mx-auto animate-fade-in-down">
            <div class="flex items-center justify-between mb-4 px-2">
                <h3 class="text-lg font-bold text-gray-800 flex items-center">
                    <span class="mr-2">🏆</span> {{ t('home.recentAchievements') }}
                </h3>
                <router-link to="/achievements" class="text-sm font-medium text-indigo-600 hover:text-indigo-800 hover:underline">
                    {{ t('home.viewAll') }}
                </router-link>
            </div>
            <div class="grid grid-cols-3 gap-4">
                <div v-for="achievement in recentAchievements" :key="achievement.id"
                     class="bg-white p-3 rounded-xl shadow-sm border border-gray-100 flex flex-col items-center text-center transform hover:scale-105 transition cursor-pointer"
                     @click="router.push('/achievements')">
                    <div class="w-10 h-10 rounded-full flex items-center justify-center text-2xl mb-2" :class="getMetadata(achievement.type).bg">
                        {{ getMetadata(achievement.type).icon }}
                    </div>
                    <span class="text-xs font-bold text-gray-700 truncate w-full">{{ getMetadata(achievement.type).label }}</span>
                </div>
            </div>
        </div>

        <!-- Action Buttons -->
        <div class="flex flex-col md:flex-row justify-center gap-4">
            <!-- Only show Create Quiz if logged in AND NOT a guest -->
            <router-link v-if="authStore.isLoggedIn && !authStore.isGuest" to="/create-quiz" class="group relative inline-flex items-center justify-center px-8 py-3 text-base font-medium text-white bg-green-600 rounded-full hover:bg-green-700 transition shadow-md overflow-hidden">
                <span class="absolute w-0 h-0 transition-all duration-500 ease-out bg-white rounded-full group-hover:w-56 group-hover:h-56 opacity-10"></span>
                <span class="relative">✨ {{ t('home.createQuiz') }}</span>
            </router-link>

            <router-link v-if="!authStore.isLoggedIn" to="/login" class="inline-flex items-center justify-center px-8 py-3 text-base font-medium text-indigo-600 bg-white border-2 border-indigo-100 rounded-full hover:bg-indigo-50 transition shadow-sm">
                {{ t('home.loginRegister') }}
            </router-link>

            <div v-else class="flex gap-4">
                <!-- Only show My Quizzes if logged in AND NOT a guest -->
                <router-link v-if="!authStore.isGuest" to="/quizzes" class="inline-flex items-center justify-center px-8 py-3 text-base font-medium text-indigo-600 bg-white border-2 border-indigo-100 rounded-full hover:bg-indigo-50 transition shadow-sm">
                    📂 {{ t('home.myQuizzes') }}
                </router-link>
            </div>
        </div>

        <!-- Smart Review Section (Authenticated & Not Guest) -->
        <div v-if="authStore.isLoggedIn && !authStore.isGuest" class="max-w-2xl mx-auto bg-white/60 backdrop-blur-sm p-6 rounded-xl border border-indigo-100 mt-8">
            <div class="flex items-center justify-between flex-col md:flex-row gap-4">
                <div class="text-left">
                    <h3 class="text-lg font-bold text-gray-800">🚀 {{ t('home.boostSkills') }}</h3>
                    <p class="text-sm text-gray-500">{{ t('home.boostDescription') }}</p>
                </div>
                <button
                    @click="generateSmartReview"
                    :disabled="isGenerating"
                    class="inline-flex items-center px-6 py-3 border border-transparent text-sm font-bold rounded-lg text-white bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 shadow-md disabled:opacity-50 transition-all"
                >
                    <span v-if="isGenerating" class="animate-pulse">{{ t('home.generating') }}</span>
                    <span v-else>🧠 {{ t('home.smartReview') }}</span>
                </button>
            </div>
        </div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in-down {
    animation: fadeInDown 0.8s ease-out;
}
@keyframes fadeInDown {
    from { opacity: 0; transform: translateY(-20px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
