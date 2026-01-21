<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const achievements = ref<any[]>([])
const isGuest = ref(false)
const guestMessage = ref('')

// Metadata for display mapping - ONLY ACTIVE BADGES
const achievementMetadata: Record<string, { label: string, description: string, icon: string, color: string }> = {
    'SNIPER': { 
        label: 'Sniper', 
        description: '5 bonnes réponses d\'affilée', 
        icon: '🎯',
        color: 'border-red-500'
    },
    'FLASH': { 
        label: 'Flash', 
        description: 'Réponse correcte en moins de 1 seconde', 
        icon: '⚡',
        color: 'border-yellow-400'
    },
    'TURBO': { 
        label: 'Turbo', 
        description: '3 réponses rapides (< 5s) d\'affilée', 
        icon: '🚀',
        color: 'border-orange-500'
    },
    'LUCKY': { 
        label: 'Chanceux', 
        description: 'Bonne réponse sur une question difficile', 
        icon: '🍀',
        color: 'border-green-500'
    }
}

const getMetadata = (type: string) => {
    return achievementMetadata[type] || { 
        label: type, 
        description: 'Succès débloqué', 
        icon: '🏆',
        color: 'border-gray-200'
    }
}

const formatDate = (dateInput: any) => {
    if (!dateInput) return 'Date inconnue'
    if (Array.isArray(dateInput)) {
        const date = new Date(dateInput[0], dateInput[1] - 1, dateInput[2], dateInput[3] || 0, dateInput[4] || 0)
        return date.toLocaleDateString()
    }
    return new Date(dateInput).toLocaleDateString()
}

const fetchAchievements = async () => {
    try {
        const token = authStore.token
        const response = await axios.get('/api/v1/achievements/status', {
            headers: { Authorization: `Bearer ${token}` }
        })
        
        isGuest.value = response.data.guest
        guestMessage.value = response.data.message
        achievements.value = response.data.achievements

        // Check local storage for recently unlocked achievements during game
        const recentlyUnlocked = JSON.parse(localStorage.getItem('recentlyUnlockedAchievements') || '[]')
        
        if (recentlyUnlocked.length > 0) {
            // Merge server data with local unlocks
            achievements.value = achievements.value.map(ach => {
                // If achievement is in local storage list AND not yet unlocked on server side (or just to be sure)
                if (recentlyUnlocked.includes(ach.type) && !ach.unlocked) {
                    return { 
                        ...ach, 
                        unlocked: true, 
                        unlockedAt: new Date() // Use current date as fallback
                    }
                }
                return ach
            })
            
            // Optional: Clear local storage after syncing, or keep it for a session
            // localStorage.removeItem('recentlyUnlockedAchievements') 
        }

    } catch (error) {
        console.error("Failed to fetch achievements", error)
    }
}

const goToLogin = () => {
    authStore.logout()
    router.push('/login')
}

onMounted(() => {
    fetchAchievements()
})
</script>

<template>
  <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">Mes Succès</h1>

    <!-- Guest Warning -->
    <div v-if="isGuest" class="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-8 rounded-r-lg shadow-sm">
        <div class="flex">
            <div class="flex-shrink-0">
                <span class="text-2xl">⚠️</span>
            </div>
            <div class="ml-3">
                <p class="text-sm text-yellow-700 font-bold">
                    {{ guestMessage }}
                </p>
                <div class="mt-4">
                    <button @click="goToLogin" class="text-sm font-medium text-yellow-700 hover:text-yellow-600 underline">
                        Créer un compte ou se connecter
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Achievements Grid -->
    <div v-if="achievements.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="achievement in achievements" :key="achievement.type" 
             class="bg-white overflow-hidden shadow-md rounded-lg border-l-4 transition hover:shadow-lg relative"
             :class="[
                achievement.unlocked ? getMetadata(achievement.type).color : 'border-gray-300 bg-gray-50'
             ]">
            <div class="px-4 py-5 sm:p-6">
                <div class="flex items-start" :class="{ 'opacity-50 grayscale': !achievement.unlocked }">
                    <div class="flex-shrink-0 bg-gray-50 rounded-full p-3 shadow-sm">
                        <span class="text-3xl">{{ getMetadata(achievement.type).icon }}</span>
                    </div>
                    <div class="ml-5 w-0 flex-1">
                        <dt class="text-lg font-bold text-gray-900 truncate">
                            {{ getMetadata(achievement.type).label }}
                        </dt>
                        <dd class="mt-1 text-sm text-gray-600">
                            {{ getMetadata(achievement.type).description }}
                        </dd>
                        <dd v-if="achievement.unlocked" class="mt-2 text-xs text-green-600 font-semibold">
                            Obtenu le {{ formatDate(achievement.unlockedAt) }}
                        </dd>
                        <dd v-else class="mt-2 text-xs text-gray-500 font-semibold flex items-center">
                            <span class="mr-1">🔒</span> Verrouillé
                        </dd>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>
