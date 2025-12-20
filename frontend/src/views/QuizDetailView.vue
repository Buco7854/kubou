<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const quizId = route.params.id as string

interface Question {
  id: string
  text: string
  difficultyLevel: number
  tags: string[]
}

interface Quiz {
  id: string
  title: string
  questions: Question[]
}

const quiz = ref<Quiz | null>(null)
const availableQuestions = ref<Question[]>([])
const selectedQuestionIds = ref<string[]>([])

const fetchQuiz = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get(`/api/v1/quizzes/${quizId}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    quiz.value = response.data
  } catch (error) {
    console.error('Erreur lors de la récupération du quiz:', error)
  }
}

const fetchAvailableQuestions = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/v1/questions', {
      headers: { Authorization: `Bearer ${token}` }
    })
    // Filter out questions already in the quiz
    const currentQuestionIds = new Set(quiz.value?.questions.map(q => q.id) || [])
    availableQuestions.value = response.data.filter((q: Question) => !currentQuestionIds.has(q.id))
  } catch (error) {
    console.error('Erreur lors de la récupération des questions:', error)
  }
}

const addQuestionsToQuiz = async () => {
  if (selectedQuestionIds.value.length === 0) return

  try {
    const token = localStorage.getItem('token')
    await axios.post(`/api/v1/quizzes/${quizId}/questions`, {
      questionIds: selectedQuestionIds.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })

    // Refresh data
    selectedQuestionIds.value = []
    await fetchQuiz()
    await fetchAvailableQuestions()
    alert('Questions ajoutées avec succès !')
  } catch (error) {
    console.error('Erreur lors de l\'ajout des questions:', error)
    alert('Échec de l\'ajout des questions')
  }
}

onMounted(async () => {
  await fetchQuiz()
  await fetchAvailableQuestions()
})
</script>

<template>
  <div class="max-w-4xl mx-auto" v-if="quiz">
    <div class="bg-white shadow overflow-hidden sm:rounded-lg mb-8">
      <div class="px-4 py-5 sm:px-6">
        <h3 class="text-lg leading-6 font-medium text-gray-900">Détails du Quiz</h3>
        <p class="mt-1 max-w-2xl text-sm text-gray-500">{{ quiz.title }}</p>
      </div>
      <div class="border-t border-gray-200 px-4 py-5 sm:p-0">
        <dl class="sm:divide-y sm:divide-gray-200">
          <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt class="text-sm font-medium text-gray-500">ID du Quiz</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{{ quiz.id }}</dd>
          </div>
          <div class="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
            <dt class="text-sm font-medium text-gray-500">Nombre de questions</dt>
            <dd class="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{{ quiz.questions.length }}</dd>
          </div>
        </dl>
      </div>
    </div>

    <!-- Existing Questions -->
    <div class="mb-8">
      <h3 class="text-xl font-bold text-gray-900 mb-4">Questions du Quiz</h3>
      <div class="bg-white shadow overflow-hidden sm:rounded-md">
        <ul role="list" class="divide-y divide-gray-200">
          <li v-for="question in quiz.questions" :key="question.id">
            <div class="px-4 py-4 sm:px-6">
              <div class="flex items-center justify-between">
                <p class="text-sm font-medium text-indigo-600 truncate">{{ question.text }}</p>
                <div class="ml-2 flex-shrink-0 flex">
                  <p class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                    Niveau {{ question.difficultyLevel }}
                  </p>
                </div>
              </div>
            </div>
          </li>
          <li v-if="quiz.questions.length === 0" class="px-4 py-4 sm:px-6 text-center text-gray-500">
            Ce quiz n'a pas encore de questions.
          </li>
        </ul>
      </div>
    </div>

    <!-- Add Questions -->
    <div>
      <h3 class="text-xl font-bold text-gray-900 mb-4">Ajouter des Questions depuis la Banque</h3>
      <div class="bg-white shadow sm:rounded-md p-4">
        <div v-if="availableQuestions.length > 0">
          <div class="space-y-4 max-h-96 overflow-y-auto">
            <div v-for="question in availableQuestions" :key="question.id" class="flex items-start">
              <div class="flex items-center h-5">
                <input
                  :id="question.id"
                  :value="question.id"
                  v-model="selectedQuestionIds"
                  type="checkbox"
                  class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
                />
              </div>
              <div class="ml-3 text-sm">
                <label :for="question.id" class="font-medium text-gray-700">{{ question.text }}</label>
                <p class="text-gray-500">Tags: {{ question.tags ? question.tags.join(', ') : 'Aucun' }} | Niveau: {{ question.difficultyLevel }}</p>
              </div>
            </div>
          </div>
          <div class="mt-4 pt-4 border-t border-gray-200">
            <button
              @click="addQuestionsToQuiz"
              :disabled="selectedQuestionIds.length === 0"
              class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-gray-300"
            >
              Ajouter les questions sélectionnées
            </button>
          </div>
        </div>
        <div v-else class="text-center text-gray-500 py-4">
          Aucune question disponible à ajouter. Créez-en d'abord dans la banque de questions.
        </div>
      </div>
    </div>
  </div>
  <div v-else class="text-center py-12">
    Chargement...
  </div>
</template>
