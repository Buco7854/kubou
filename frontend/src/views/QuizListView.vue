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
  <div class="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-5xl mx-auto">
      <div class="flex justify-between items-center mb-8">
        <div>
            <h2 class="text-3xl font-extrabold text-gray-900">Mes Quiz</h2>
            <p class="mt-1 text-sm text-gray-500">Gérez vos créations et lancez des parties.</p>
        </div>
        <router-link
          to="/create-quiz"
          class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-full shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 transition transform hover:scale-105"
        >
          + Créer un Quiz
        </router-link>
      </div>

      <div class="bg-white shadow-xl rounded-2xl overflow-hidden border border-gray-100">
        <ul role="list" class="divide-y divide-gray-100">
          <li v-for="quiz in quizzes" :key="quiz.id" class="hover:bg-gray-50 transition duration-150 ease-in-out">
            <div class="px-6 py-5 flex items-center justify-between">
              <div class="cursor-pointer flex-grow" @click="goToDetail(quiz.id)">
                <div class="flex items-center">
                    <div class="flex-shrink-0 h-12 w-12 rounded-lg bg-indigo-100 flex items-center justify-center text-indigo-600 text-xl font-bold mr-4">
                        {{ quiz.title.charAt(0).toUpperCase() }}
                    </div>
                    <div>
                        <p class="text-lg font-bold text-gray-900 truncate">{{ quiz.title }}</p>
                        <p class="text-sm text-gray-500 flex items-center mt-1">
                            <span class="bg-gray-100 text-gray-600 py-0.5 px-2 rounded-full text-xs font-medium mr-2">
                                {{ quiz.questions ? quiz.questions.length : 0 }} questions
                            </span>
                            <span class="text-xs text-gray-400">ID: {{ quiz.id.substring(0, 8) }}...</span>
                        </p>
                    </div>
                </div>
              </div>
              <div class="flex space-x-3">
                <button
                  @click="goToDetail(quiz.id)"
                  class="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 transition"
                >
                  ⚙️ Gérer
                </button>
                <button
                  @click="deleteQuiz(quiz.id)"
                  class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-red-500 hover:bg-red-600 transition"
                >
                  🗑️
                </button>
              </div>
            </div>
          </li>
          <li v-if="quizzes.length === 0" class="px-6 py-12 text-center">
            <div class="mx-auto h-24 w-24 text-gray-300 mb-4">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m-9 1V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z"></path></svg>
            </div>
            <h3 class="mt-2 text-sm font-medium text-gray-900">Aucun quiz</h3>
            <p class="mt-1 text-sm text-gray-500">Commencez par créer votre premier quiz !</p>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
