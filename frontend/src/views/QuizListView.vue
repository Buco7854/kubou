<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const quizzes = ref<any[]>([])

const fetchQuizzes = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/v1/quizzes', {
      headers: { Authorization: `Bearer ${token}` }
    })
    quizzes.value = response.data
  } catch (error) {
    console.error('Erreur lors de la récupération des quiz:', error)
  }
}

const deleteQuiz = async (id: string) => {
  if (!confirm('Êtes-vous sûr de vouloir supprimer ce quiz ?')) return

  try {
    const token = localStorage.getItem('token')
    await axios.delete(`/api/v1/quizzes/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    await fetchQuizzes()
  } catch (error) {
    console.error('Erreur lors de la suppression du quiz:', error)
    alert('Impossible de supprimer le quiz.')
  }
}

const goToDetail = (id: string) => {
  router.push(`/quiz/${id}`)
}

onMounted(() => {
  fetchQuizzes()
})
</script>

<template>
  <div class="max-w-4xl mx-auto">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-900">Mes Quiz</h2>
      <router-link
        to="/create-quiz"
        class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700"
      >
        Créer un Quiz
      </router-link>
    </div>

    <div class="bg-white shadow overflow-hidden sm:rounded-md">
      <ul role="list" class="divide-y divide-gray-200">
        <li v-for="quiz in quizzes" :key="quiz.id">
          <div class="px-4 py-4 sm:px-6 flex items-center justify-between hover:bg-gray-50 transition">
            <div class="cursor-pointer flex-grow" @click="goToDetail(quiz.id)">
              <p class="text-lg font-medium text-indigo-600 truncate">{{ quiz.title }}</p>
              <p class="text-sm text-gray-500">{{ quiz.questions ? quiz.questions.length : 0 }} questions</p>
            </div>
            <div class="flex space-x-2">
              <button
                @click="goToDetail(quiz.id)"
                class="inline-flex items-center px-3 py-1 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50"
              >
                Gérer
              </button>
              <button
                @click="deleteQuiz(quiz.id)"
                class="inline-flex items-center px-3 py-1 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-red-600 hover:bg-red-700"
              >
                Supprimer
              </button>
            </div>
          </div>
        </li>
        <li v-if="quizzes.length === 0" class="px-4 py-4 sm:px-6 text-center text-gray-500">
          Aucun quiz trouvé. Créez-en un !
        </li>
      </ul>
    </div>
  </div>
</template>
