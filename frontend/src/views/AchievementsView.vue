<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const achievements = ref<any[]>([])

// Metadata for display mapping
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
    'MARATHON': { 
        label: 'Marathonien', 
        description: 'Terminer un quiz complet', 
        icon: '🏃',
        color: 'border-blue-500'
    },
    'FIRST_GAME': { 
        label: 'Première Partie', 
        description: 'Terminer sa première partie', 
        icon: '🐣',
        color: 'border-green-400'
    },
    'WINNER': { 
        label: 'Champion', 
        description: 'Finir premier du classement', 
        icon: '🥇',
        color: 'border-yellow-500'
    },
    'TOP_3': { 
        label: 'Podium', 
        description: 'Finir dans le top 3', 
        icon: '🏅',
        color: 'border-gray-400'
    },
    'PERFECT': { 
        label: 'Perfection', 
        description: '100% de bonnes réponses', 
        icon: '💎',
        color: 'border-purple-500'
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
        
        // 1. Fetch MY achievements
        console.log("Fetching MY achievements...")
        const response = await axios.get('/api/v1/achievements/me', {
            headers: { Authorization: `Bearer ${token}` }
        })
        console.log("MY Achievements:", response.data)
        achievements.value = response.data

        // 2. DEBUG: Fetch ALL achievements to compare IDs
        console.log("Fetching ALL achievements (DEBUG)...")
        const debugResponse = await axios.get('/api/v1/achievements/debug', {
            headers: { Authorization: `Bearer ${token}` }
        })
        console.log("ALL Achievements in DB:", debugResponse.data)
        
        // Check current user ID
        if (token) {
            try {
                const payload = JSON.parse(atob(token.split('.')[1]))
                console.log("Current User ID (from Token):", payload.sub)
            } catch (e) {
                console.error("Error parsing token", e)
            }
        }

    } catch (error) {
        console.error("Failed to fetch achievements", error)
    }
}

onMounted(() => {
    fetchAchievements()
})
</script>

<template>
  <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">Mes Succès</h1>

    <div v-if="achievements.length === 0" class="text-center text-gray-500 py-12 bg-white rounded-lg shadow">
        <div class="text-4xl mb-3">🔒</div>
        <p>Vous n'avez pas encore débloqué de succès.</p>
        <p class="text-sm mt-2">Jouez des parties pour gagner des badges !</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="achievement in achievements" :key="achievement.id" 
             class="bg-white overflow-hidden shadow-md rounded-lg border-l-4 transition hover:shadow-lg"
             :class="getMetadata(achievement.type).color">
            <div class="px-4 py-5 sm:p-6">
                <div class="flex items-start">
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
                        <dd class="mt-2 text-xs text-gray-400">
                            Obtenu le {{ formatDate(achievement.unlockedAt) }}
                        </dd>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>
