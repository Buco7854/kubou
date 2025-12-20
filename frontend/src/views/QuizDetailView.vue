<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
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
const showSettings = ref(false)
const showCreateQuestionModal = ref(false)
const scoringSettings = ref({
    baseScore: 1000,
    timeWeight: 0.5,
    streakMultiplier: 1.0
})
const isTeamMode = ref(false)

// New Question Form Data
const newQuestion = ref({
  text: '',
  options: ['', '', '', ''],
  correctAnswerIndex: 0,
  tags: '',
  difficultyLevel: 1
})

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

const createAndAddQuestion = async () => {
  try {
    const token = localStorage.getItem('token')
    // 1. Create the question
    const questionPayload = {
      ...newQuestion.value,
      // Filter out empty options
      options: newQuestion.value.options.filter(opt => opt.trim() !== ''),
      tags: newQuestion.value.tags.split(',').map(tag => tag.trim()).filter(tag => tag !== '')
    }

    if (questionPayload.options.length < 2) {
        alert("Veuillez ajouter au moins 2 options.")
        return
    }

    const createResponse = await axios.post('/api/v1/questions', questionPayload, {
      headers: { Authorization: `Bearer ${token}` }
    })
    const createdQuestionId = createResponse.data.id

    // 2. Add it to the quiz
    await axios.post(`/api/v1/quizzes/${quizId}/questions`, {
      questionIds: [createdQuestionId]
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })

    // Reset and refresh
    newQuestion.value = {
      text: '',
      options: ['', '', '', ''],
      correctAnswerIndex: 0,
      tags: '',
      difficultyLevel: 1
    }
    showCreateQuestionModal.value = false
    await fetchQuiz()
    await fetchAvailableQuestions()
    alert('Question créée et ajoutée !')

  } catch (error) {
    console.error('Erreur lors de la création de la question:', error)
    alert('Erreur lors de la création.')
  }
}

const addOption = () => {
    newQuestion.value.options.push('')
}

const removeOption = (index: number) => {
    if (newQuestion.value.options.length > 2) {
        newQuestion.value.options.splice(index, 1)
        // Adjust correct answer index if needed
        if (newQuestion.value.correctAnswerIndex >= index && newQuestion.value.correctAnswerIndex > 0) {
            newQuestion.value.correctAnswerIndex--
        }
    }
}

const createGameSession = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/v1/games', {
      quizId: quizId,
      scoringSettings: scoringSettings.value,
      teamMode: isTeamMode.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })

    // Redirect to lobby
    router.push(`/lobby/${response.data.pin}`)
  } catch (error) {
    console.error('Erreur lors de la création de la session de jeu:', error)
    alert('Échec de la création de la session de jeu')
  }
}

onMounted(async () => {
  await fetchQuiz()
  await fetchAvailableQuestions()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <div v-if="quiz" class="py-8 px-4 sm:px-6 lg:px-8">
      <div class="max-w-5xl mx-auto space-y-8">

          <!-- Header Card -->
          <div class="bg-white shadow-xl rounded-2xl overflow-hidden border border-gray-100">
              <div class="bg-indigo-600 px-6 py-8 sm:px-10 flex flex-col md:flex-row justify-between items-center">
                  <div class="text-white mb-4 md:mb-0">
                      <h1 class="text-3xl font-extrabold tracking-tight">{{ quiz.title }}</h1>
                      <p class="mt-2 text-indigo-100 text-sm font-medium">ID: {{ quiz.id }}</p>
                  </div>
                  <div class="flex space-x-3">
                      <button
                      @click="showSettings = !showSettings"
                      class="inline-flex items-center px-5 py-2.5 border border-white/30 text-sm font-bold rounded-lg text-white bg-white/10 hover:bg-white/20 focus:outline-none transition backdrop-blur-sm"
                      >
                      ⚙️ Paramètres
                      </button>
                      <button
                      @click="createGameSession"
                      class="inline-flex items-center px-6 py-2.5 border border-transparent text-sm font-bold rounded-lg text-indigo-700 bg-white hover:bg-indigo-50 focus:outline-none shadow-lg transform hover:scale-105 transition"
                      >
                      🚀 Lancer le Live
                      </button>
                  </div>
              </div>

              <!-- Settings Panel -->
              <div v-if="showSettings" class="bg-gray-50 px-6 py-6 border-b border-gray-200 animate-fade-in">
                  <h4 class="text-lg font-bold text-gray-900 mb-4 flex items-center">
                      <span class="bg-indigo-100 text-indigo-600 p-1 rounded mr-2">🛠️</span> Configuration de la Partie
                  </h4>
                  <div class="grid grid-cols-1 gap-6 sm:grid-cols-3 mb-6">
                      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
                          <label class="block text-sm font-medium text-gray-700 mb-1">Score de Base</label>
                          <input v-model.number="scoringSettings.baseScore" type="number" class="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
                      </div>
                      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
                          <label class="block text-sm font-medium text-gray-700 mb-1">Poids du Temps (0-1)</label>
                          <input v-model.number="scoringSettings.timeWeight" type="number" step="0.1" min="0" max="1" class="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
                          <p class="text-xs text-gray-500 mt-1">1 = La vitesse est cruciale</p>
                      </div>
                      <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-200">
                          <label class="block text-sm font-medium text-gray-700 mb-1">Multiplicateur Streak</label>
                          <input v-model.number="scoringSettings.streakMultiplier" type="number" step="0.1" min="1" class="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
                      </div>
                  </div>

                  <div class="flex items-center bg-purple-50 p-4 rounded-lg border border-purple-100">
                      <input id="team-mode" v-model="isTeamMode" type="checkbox" class="h-5 w-5 text-purple-600 focus:ring-purple-500 border-gray-300 rounded" />
                      <label for="team-mode" class="ml-3 block text-sm font-bold text-purple-900">
                          Activer le Mode Équipe (Team Battle) ⚔️
                      </label>
                  </div>
              </div>

              <!-- Stats Bar -->
              <div class="bg-white px-6 py-4 flex items-center justify-between border-t border-gray-100">
                  <div class="flex space-x-8">
                      <div class="flex flex-col">
                          <span class="text-xs font-medium text-gray-500 uppercase">Questions</span>
                          <span class="text-2xl font-bold text-gray-900">{{ quiz.questions.length }}</span>
                      </div>
                      <div class="flex flex-col">
                          <span class="text-xs font-medium text-gray-500 uppercase">Difficulté Moy.</span>
                          <span class="text-2xl font-bold text-gray-900">
                              {{ quiz.questions.length > 0 ? (quiz.questions.reduce((acc, q) => acc + q.difficultyLevel, 0) / quiz.questions.length).toFixed(1) : '-' }}
                          </span>
                      </div>
                  </div>
              </div>
          </div>

          <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
              <!-- Questions List -->
              <div class="lg:col-span-2 space-y-6">
                  <h3 class="text-xl font-bold text-gray-900 flex items-center">
                      <span class="bg-green-100 text-green-600 p-1.5 rounded-lg mr-2 text-sm">📋</span>
                      Questions du Quiz
                  </h3>
                  <div class="bg-white shadow-lg rounded-xl overflow-hidden border border-gray-100">
                      <ul role="list" class="divide-y divide-gray-100">
                      <li v-for="(question, index) in quiz.questions" :key="question.id" class="hover:bg-gray-50 transition p-6">
                          <div class="flex items-start">
                              <span class="flex-shrink-0 h-8 w-8 flex items-center justify-center rounded-full bg-gray-100 text-gray-500 font-bold text-sm mr-4">
                                  {{ index + 1 }}
                              </span>
                              <div class="flex-1">
                                  <p class="text-lg font-medium text-gray-900">{{ question.text }}</p>
                                  <div class="mt-2 flex items-center space-x-2">
                                      <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                          Niveau {{ question.difficultyLevel }}
                                      </span>
                                      <span v-for="tag in question.tags" :key="tag" class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                                          #{{ tag }}
                                      </span>
                                  </div>
                              </div>
                          </div>
                      </li>
                      <li v-if="quiz.questions.length === 0" class="p-12 text-center text-gray-500 italic">
                          Ce quiz est vide. Ajoutez des questions depuis la banque !
                      </li>
                      </ul>
                  </div>
              </div>

              <!-- Add Questions Sidebar -->
              <div class="space-y-6">
                  <h3 class="text-xl font-bold text-gray-900 flex items-center justify-between">
                      <div class="flex items-center">
                        <span class="bg-orange-100 text-orange-600 p-1.5 rounded-lg mr-2 text-sm">🏦</span>
                        Banque
                      </div>
                      <button @click="showCreateQuestionModal = true" class="text-sm bg-indigo-100 text-indigo-700 px-3 py-1 rounded-full hover:bg-indigo-200 transition">
                          + Créer
                      </button>
                  </h3>
                  <div class="bg-white shadow-lg rounded-xl overflow-hidden border border-gray-100 flex flex-col h-[600px]">
                      <div class="p-4 border-b border-gray-100 bg-gray-50">
                          <p class="text-sm text-gray-500">Sélectionnez des questions à ajouter.</p>
                      </div>

                      <div class="flex-1 overflow-y-auto p-4 space-y-3">
                          <div v-if="availableQuestions.length > 0">
                              <div v-for="question in availableQuestions" :key="question.id"
                                   class="flex items-start p-3 rounded-lg border border-gray-200 hover:border-indigo-300 hover:bg-indigo-50 transition cursor-pointer"
                                   @click="!selectedQuestionIds.includes(question.id) ? selectedQuestionIds.push(question.id) : selectedQuestionIds = selectedQuestionIds.filter(id => id !== question.id)">
                                  <div class="flex items-center h-5 mt-1">
                                      <input
                                      :id="question.id"
                                      :value="question.id"
                                      v-model="selectedQuestionIds"
                                      type="checkbox"
                                      class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
                                      @click.stop
                                      />
                                  </div>
                                  <div class="ml-3 text-sm">
                                      <label :for="question.id" class="font-medium text-gray-900 cursor-pointer">{{ question.text }}</label>
                                      <p class="text-gray-500 text-xs mt-1">Tags: {{ question.tags ? question.tags.join(', ') : '-' }}</p>
                                  </div>
                              </div>
                          </div>
                          <div v-else class="text-center text-gray-500 py-8">
                              Aucune question disponible.
                          </div>
                      </div>

                      <div class="p-4 border-t border-gray-100 bg-gray-50">
                          <button
                          @click="addQuestionsToQuiz"
                          :disabled="selectedQuestionIds.length === 0"
                          class="w-full inline-flex justify-center py-3 px-4 border border-transparent shadow-sm text-sm font-bold rounded-lg text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none disabled:bg-gray-300 disabled:cursor-not-allowed transition"
                          >
                          Ajouter ({{ selectedQuestionIds.length }})
                          </button>
                      </div>
                  </div>
              </div>
          </div>
      </div>

      <!-- Create Question Modal -->
      <div v-if="showCreateQuestionModal" class="fixed inset-0 z-50 overflow-y-auto" aria-labelledby="modal-title" role="dialog" aria-modal="true">
        <div class="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" aria-hidden="true" @click="showCreateQuestionModal = false"></div>
            <span class="hidden sm:inline-block sm:align-middle sm:h-screen" aria-hidden="true">&#8203;</span>
            <div class="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
                <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                    <h3 class="text-lg leading-6 font-medium text-gray-900 mb-4" id="modal-title">Créer une nouvelle question</h3>
                    <form @submit.prevent="createAndAddQuestion" class="space-y-4">
                        <div>
                            <label class="block text-sm font-medium text-gray-700">Énoncé</label>
                            <input v-model="newQuestion.text" type="text" required class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-2">Options</label>
                            <div v-for="(option, index) in newQuestion.options" :key="index" class="flex items-center mb-2">
                                <input type="radio" :value="index" v-model="newQuestion.correctAnswerIndex" class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 mr-2" title="Marquer comme bonne réponse" />
                                <input v-model="newQuestion.options[index]" type="text" required class="flex-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" :placeholder="`Option ${index + 1}`" />
                                <button type="button" @click="removeOption(index)" class="ml-2 text-red-500 hover:text-red-700" v-if="newQuestion.options.length > 2">
                                    &times;
                                </button>
                            </div>
                            <button type="button" @click="addOption" class="text-sm text-indigo-600 hover:text-indigo-800 font-medium">
                                + Ajouter une option
                            </button>
                        </div>

                        <div class="grid grid-cols-2 gap-4">
                            <div>
                                <label class="block text-sm font-medium text-gray-700">Tags</label>
                                <input v-model="newQuestion.tags" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" placeholder="math, algèbre" />
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700">Difficulté (1-5)</label>
                                <input v-model="newQuestion.difficultyLevel" type="number" min="1" max="5" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
                            </div>
                        </div>
                    </form>
                </div>
                <div class="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                    <button type="button" @click="createAndAddQuestion" class="w-full inline-flex justify-center rounded-md border border-transparent shadow-sm px-4 py-2 bg-indigo-600 text-base font-medium text-white hover:bg-indigo-700 focus:outline-none sm:ml-3 sm:w-auto sm:text-sm">
                        Créer et Ajouter
                    </button>
                    <button type="button" @click="showCreateQuestionModal = false" class="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-white text-base font-medium text-gray-700 hover:bg-gray-50 focus:outline-none sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm">
                        Annuler
                    </button>
                </div>
            </div>
        </div>
      </div>

    </div>
    <div v-else class="flex h-screen items-center justify-center">
        <div class="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-600"></div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
    animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
