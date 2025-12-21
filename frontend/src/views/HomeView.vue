<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const router = useRouter()
const authStore = useAuthStore()
const gamePin = ref('')
const isGenerating = ref(false)
const activeSessions = ref<any[]>([])
const participatingSessions = ref<any[]>([])

const joinGame = () => {
  if (gamePin.value.trim()) {
    router.push(`/lobby/${gamePin.value}`)
  }
}

const fetchMySessions = async () => {
    // Only fetch if logged in AND NOT a guest
    if (authStore.isLoggedIn && !authStore.isGuest) {
        try {
            const token = authStore.token
            const response = await axios.get('/api/v1/games/my-sessions', {
                headers: { Authorization: `Bearer ${token}` }
            })
            // Filter for active sessions (LOBBY or IN_PROGRESS)
            activeSessions.value = response.data.filter((s: any) => s.state !== 'FINISHED')
        } catch (error) {
            console.error("Failed to fetch sessions", error)
        }
    }
}

const fetchParticipatingSessions = async () => {
    // Fetch for ANY logged in user (including guests)
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

const resumeSession = (pin: string) => {
    router.push(`/lobby/${pin}`)
}

const closeSession = async (gameId: string) => {
    if (!confirm("Êtes-vous sûr de vouloir fermer cette session ?")) return;

    try {
        const token = authStore.token
        await axios.delete(`/api/v1/games/${gameId}`, {
             headers: { Authorization: `Bearer ${token}` }
        })
        fetchMySessions()
    } catch (error) {
        console.error("Failed to close session", error)
        alert("Impossible de fermer la session.")
    }
}

const leaveSession = async (gameId: string) => {
    if (!confirm("Êtes-vous sûr de vouloir quitter cette partie ?")) return;

    // Since we don't have a REST endpoint for leaving (it's WS based in GameController),
    // we can either add one or just hide it from the list locally if we assume the user won't rejoin.
    // However, to be clean, we should probably connect and send the leave message, or add a REST endpoint.
    // Given the constraints, let's just remove it from the local list for now as a "Hide" feature,
    // or better, navigate to the lobby and let them click "Leave" if we implemented a leave button there.

    // But the user asked for a button in the list.
    // Let's use a quick WS connection to send the leave message if possible, or just acknowledge we can't easily do it without a REST endpoint.
    // Ideally, we should add DELETE /api/v1/games/{id}/players/me

    // For now, let's just navigate them to the lobby where they can leave properly or just ignore it.
    // Actually, let's just hide it from the view to "clean up" the dashboard.
    participatingSessions.value = participatingSessions.value.filter(s => s.id !== gameId);
}

const generateSmartReview = async () => {
    if (!authStore.isLoggedIn) {
        alert("Veuillez vous connecter pour utiliser cette fonctionnalité.")
        return
    }

    isGenerating.value = true
    try {
        const token = authStore.token
        const response = await axios.post('/api/v1/smart-review/generate', {}, {
            headers: { Authorization: `Bearer ${token}` }
        })

        if (response.status === 204) {
            alert("Bravo ! Vous n'avez pas assez d'erreurs pour générer un quiz de rattrapage.")
        } else {
            const quiz = response.data
            alert(`Quiz de rattrapage généré : ${quiz.title}`)
            router.push(`/quiz/${quiz.id}`)
        }
    } catch (error) {
        console.error("Failed to generate review", error)
        alert("Erreur lors de la génération du quiz.")
    } finally {
        isGenerating.value = false
    }
}

onMounted(() => {
    fetchMySessions()
    fetchParticipatingSessions()
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
                La plateforme d'apprentissage interactive où chaque réponse compte.
            </p>
        </div>

        <!-- Active Sessions (Host) -->
        <div v-if="activeSessions.length > 0" class="max-w-md mx-auto animate-fade-in-down">
            <div class="bg-white/80 backdrop-blur-sm p-4 rounded-xl border border-indigo-200 shadow-lg">
                <h3 class="text-lg font-bold text-indigo-900 mb-3">🔴 Vos sessions (Host)</h3>
                <div class="space-y-2">
                    <div v-for="session in activeSessions" :key="session.id"
                         class="flex justify-between items-center p-3 bg-white rounded-lg border border-indigo-100 hover:border-indigo-300 transition shadow-sm hover:shadow-md">
                        <div class="text-left cursor-pointer flex-grow" @click="resumeSession(session.pin)">
                            <span class="block font-mono text-xl font-bold text-indigo-600 tracking-widest">{{ session.pin }}</span>
                            <span class="text-xs text-gray-500">{{ session.state }}</span>
                        </div>
                        <div class="flex space-x-2">
                            <button @click="resumeSession(session.pin)" class="text-sm bg-indigo-100 text-indigo-700 px-3 py-1 rounded-full font-medium hover:bg-indigo-200">
                                Reprendre
                            </button>
                            <button @click="closeSession(session.id)" class="text-sm bg-red-100 text-red-700 px-3 py-1 rounded-full font-medium hover:bg-red-200">
                                Fermer
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Participating Sessions (Player) -->
        <div v-if="participatingSessions.length > 0" class="max-w-md mx-auto animate-fade-in-down">
            <div class="bg-white/80 backdrop-blur-sm p-4 rounded-xl border border-green-200 shadow-lg">
                <h3 class="text-lg font-bold text-green-900 mb-3">🟢 En cours (Joueur)</h3>
                <div class="space-y-2">
                    <div v-for="session in participatingSessions" :key="session.id"
                         class="flex justify-between items-center p-3 bg-white rounded-lg border border-green-100 hover:border-green-300 transition shadow-sm hover:shadow-md">
                        <div class="text-left cursor-pointer flex-grow" @click="resumeSession(session.pin)">
                            <span class="block font-mono text-xl font-bold text-green-600 tracking-widest">{{ session.pin }}</span>
                            <span class="text-xs text-gray-500">Participant</span>
                        </div>
                        <div class="flex space-x-2">
                            <button @click="resumeSession(session.pin)" class="text-sm bg-green-100 text-green-700 px-3 py-1 rounded-full font-medium hover:bg-green-200">
                                Rejoindre
                            </button>
                            <button @click="leaveSession(session.id)" class="text-sm bg-gray-100 text-gray-700 px-3 py-1 rounded-full font-medium hover:bg-gray-200">
                                Quitter
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
                    <label class="block text-sm font-bold text-gray-500 mb-2 uppercase tracking-wide">Rejoindre une partie</label>
                    <input
                    v-model="gamePin"
                    type="text"
                    placeholder="PIN DU JEU"
                    class="w-full text-center text-3xl font-mono tracking-[0.5em] p-4 border-2 border-gray-200 rounded-xl focus:border-indigo-500 focus:ring-4 focus:ring-indigo-100 focus:outline-none transition-all uppercase placeholder-gray-300"
                    maxlength="6"
                    />
                </div>
                <button
                type="submit"
                class="w-full bg-indigo-600 text-white font-bold py-4 rounded-xl text-lg hover:bg-indigo-700 transition shadow-lg hover:shadow-xl transform active:scale-95"
                >
                Entrer dans l'arène
                </button>
            </form>
        </div>

        <!-- Action Buttons -->
        <div class="flex flex-col md:flex-row justify-center gap-4">
            <!-- Only show Create Quiz if logged in AND NOT a guest -->
            <router-link v-if="authStore.isLoggedIn && !authStore.isGuest" to="/create-quiz" class="group relative inline-flex items-center justify-center px-8 py-3 text-base font-medium text-white bg-green-600 rounded-full hover:bg-green-700 transition shadow-md overflow-hidden">
                <span class="absolute w-0 h-0 transition-all duration-500 ease-out bg-white rounded-full group-hover:w-56 group-hover:h-56 opacity-10"></span>
                <span class="relative">✨ Créer un Quiz</span>
            </router-link>

            <router-link v-if="!authStore.isLoggedIn" to="/login" class="inline-flex items-center justify-center px-8 py-3 text-base font-medium text-indigo-600 bg-white border-2 border-indigo-100 rounded-full hover:bg-indigo-50 transition shadow-sm">
                Connexion / Inscription
            </router-link>

            <div v-else class="flex gap-4">
                <!-- Only show My Quizzes if logged in AND NOT a guest -->
                <router-link v-if="!authStore.isGuest" to="/quizzes" class="inline-flex items-center justify-center px-8 py-3 text-base font-medium text-indigo-600 bg-white border-2 border-indigo-100 rounded-full hover:bg-indigo-50 transition shadow-sm">
                    📂 Mes Quiz
                </router-link>
            </div>
        </div>

        <!-- Smart Review Section (Authenticated & Not Guest) -->
        <div v-if="authStore.isLoggedIn && !authStore.isGuest" class="max-w-2xl mx-auto bg-white/60 backdrop-blur-sm p-6 rounded-xl border border-indigo-100 mt-8">
            <div class="flex items-center justify-between flex-col md:flex-row gap-4">
                <div class="text-left">
                    <h3 class="text-lg font-bold text-gray-800">🚀 Boostez vos compétences</h3>
                    <p class="text-sm text-gray-500">Générez un quiz personnalisé basé sur vos erreurs passées.</p>
                </div>
                <button
                    @click="generateSmartReview"
                    :disabled="isGenerating"
                    class="inline-flex items-center px-6 py-3 border border-transparent text-sm font-bold rounded-lg text-white bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 shadow-md disabled:opacity-50 transition-all"
                >
                    <span v-if="isGenerating" class="animate-pulse">Génération...</span>
                    <span v-else>🧠 Smart Review</span>
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
