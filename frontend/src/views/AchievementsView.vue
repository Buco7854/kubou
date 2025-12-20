<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const achievements = ref<any[]>([])

const fetchAchievements = async () => {
    try {
        const token = authStore.token
        const response = await axios.get('/api/v1/achievements/me', {
            headers: { Authorization: `Bearer ${token}` }
        })
        achievements.value = response.data
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

    <div v-if="achievements.length === 0" class="text-center text-gray-500 py-12">
        Vous n'avez pas encore débloqué de succès. Continuez à jouer !
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="achievement in achievements" :key="achievement.id" class="bg-white overflow-hidden shadow rounded-lg border-l-4 border-yellow-400">
            <div class="px-4 py-5 sm:p-6">
                <div class="flex items-center">
                    <div class="flex-shrink-0 bg-yellow-100 rounded-full p-3">
                        <span class="text-2xl">🏆</span>
                    </div>
                    <div class="ml-5 w-0 flex-1">
                        <dt class="text-lg font-medium text-gray-900 truncate">
                            {{ achievement.type }}
                        </dt>
                        <dd class="mt-1 text-sm text-gray-500">
                            Débloqué le {{ new Date(achievement.unlockedAt).toLocaleDateString() }}
                        </dd>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>
