<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const title = ref('')

const createQuiz = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/v1/quizzes', {
      title: title.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    console.log('Quiz créé:', response.data)
    // Redirect to quiz detail page to add questions
    router.push(`/quiz/${response.data.id}`)
  } catch (error) {
    console.error('Erreur lors de la création du quiz:', error)
    alert('Échec de la création du quiz. Veuillez vous connecter d\'abord.')
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        Créer un Nouveau Quiz
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        Donnez un titre accrocheur à votre quiz pour commencer.
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <form @submit.prevent="createQuiz" class="space-y-6">
          <div>
            <label class="block text-sm font-medium text-gray-700">Titre du Quiz</label>
            <div class="mt-1">
              <input v-model="title" type="text" required class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" placeholder="ex: Mathématiques - Chapitre 1" />
            </div>
          </div>

          <div>
            <button type="submit" class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
              Créer et Ajouter des Questions
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
