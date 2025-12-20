<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const gameId = route.params.id as string
const summary = ref<any>(null)
const rawData = ref<any[]>([])

const fetchAnalytics = async () => {
    try {
        const token = authStore.token
        const summaryRes = await axios.get(`/api/v1/analytics/game/${gameId}/summary`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        summary.value = summaryRes.data

        const rawRes = await axios.get(`/api/v1/analytics/game/${gameId}`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        rawData.value = rawRes.data
    } catch (error) {
        console.error("Failed to fetch analytics", error)
    }
}

onMounted(() => {
    fetchAnalytics()
})
</script>

<template>
  <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">Analyses de la Partie</h1>

    <div v-if="summary" class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
                <dt class="text-sm font-medium text-gray-500 truncate">Précision Globale</dt>
                <dd class="mt-1 text-3xl font-semibold text-gray-900">{{ (summary.accuracy * 100).toFixed(1) }}%</dd>
            </div>
        </div>
        <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
                <dt class="text-sm font-medium text-gray-500 truncate">Temps Moyen de Réponse</dt>
                <dd class="mt-1 text-3xl font-semibold text-gray-900">{{ (summary.averageTimeMs / 1000).toFixed(2) }}s</dd>
            </div>
        </div>
        <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
                <dt class="text-sm font-medium text-gray-500 truncate">Total Réponses</dt>
                <dd class="mt-1 text-3xl font-semibold text-gray-900">{{ summary.totalResponses }}</dd>
            </div>
        </div>
    </div>

    <div class="bg-white shadow overflow-hidden sm:rounded-lg mb-8">
        <div class="px-4 py-5 sm:px-6">
            <h3 class="text-lg leading-6 font-medium text-gray-900">Détails par Question</h3>
        </div>
        <div class="border-t border-gray-200">
            <dl v-if="summary && summary.questionAccuracy">
                <div v-for="(accuracy, qId) in summary.questionAccuracy" :key="qId" class="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                    <dt class="text-sm font-medium text-gray-500">Question ID: {{ qId }}</dt>
                    <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                        <div class="w-full bg-gray-200 rounded-full h-2.5 dark:bg-gray-700">
                            <div class="bg-blue-600 h-2.5 rounded-full" :style="{ width: (accuracy * 100) + '%' }"></div>
                        </div>
                        <span class="text-xs">{{ (accuracy * 100).toFixed(0) }}% de réussite</span>
                    </dd>
                </div>
            </dl>
        </div>
    </div>

    <div class="bg-white shadow overflow-hidden sm:rounded-lg">
        <div class="px-4 py-5 sm:px-6">
            <h3 class="text-lg leading-6 font-medium text-gray-900">Historique Brut</h3>
        </div>
        <div class="flex flex-col">
            <div class="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
                <div class="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
                    <div class="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
                        <table class="min-w-full divide-y divide-gray-200">
                            <thead class="bg-gray-50">
                                <tr>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Joueur</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Question</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Résultat</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Temps</th>
                                    <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Score</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-200">
                                <tr v-for="row in rawData" :key="row.id">
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ row.playerId }}</td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ row.questionId }}</td>
                                    <td class="px-6 py-4 whitespace-nowrap">
                                        <span :class="row.correct ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'" class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full">
                                            {{ row.correct ? 'Correct' : 'Incorrect' }}
                                        </span>
                                    </td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ (row.timeTakenMs / 1000).toFixed(2) }}s</td>
                                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ row.scoreAwarded }}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </div>
</template>
