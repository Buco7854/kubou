<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
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
  if (!confirm(t('quizList.confirmDelete'))) return

  try {
    const token = localStorage.getItem('token')
    await axios.delete(`/api/v1/quizzes/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    await fetchQuizzes()
  } catch (error) {
    console.error('Erreur lors de la suppression du quiz:', error)
    alert(t('quizList.deleteError'))
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
  <div class="min-h-screen bg-gray-50 py-6 sm:py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-5xl mx-auto">
      <div class="flex justify-between items-center mb-6 sm:mb-8">
        <div class="min-w-0 flex-1 mr-3">
            <h2 class="text-2xl sm:text-3xl font-extrabold text-gray-900 truncate">{{ t('quizList.title') }}</h2>
            <p class="mt-1 text-sm text-gray-500 hidden sm:block">{{ t('quizList.subtitle') }}</p>
        </div>
        <div class="flex space-x-2 sm:space-x-3">
          <router-link
            to="/import-quiz"
            class="inline-flex items-center justify-center w-10 h-10 sm:w-auto sm:h-auto sm:px-6 sm:py-3 border border-indigo-600 font-medium rounded-full shadow-sm text-indigo-600 bg-white hover:bg-indigo-50 transition transform hover:scale-105"
            :title="t('quizList.import')"
          >
            <svg class="w-5 h-5 sm:hidden" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            <span class="hidden sm:inline text-base">{{ t('quizList.import') }}</span>
          </router-link>
          <router-link
            to="/create-quiz"
            class="inline-flex items-center justify-center w-10 h-10 sm:w-auto sm:h-auto sm:px-6 sm:py-3 border border-transparent font-medium rounded-full shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 transition transform hover:scale-105"
            :title="t('quizList.create')"
          >
            <svg class="w-5 h-5 sm:hidden" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            <span class="hidden sm:inline text-base">{{ t('quizList.create') }}</span>
          </router-link>
        </div>
      </div>

      <div class="bg-white shadow-xl rounded-2xl overflow-hidden border border-gray-100">
        <ul role="list" class="divide-y divide-gray-100">
          <li v-for="quiz in quizzes" :key="quiz.id" class="hover:bg-gray-50 transition duration-150 ease-in-out">
            <div class="px-4 sm:px-6 py-4 sm:py-5">
              <div class="flex items-center justify-between">
                <div class="cursor-pointer min-w-0 flex-1" @click="goToDetail(quiz.id)">
                  <div class="flex items-center">
                      <div class="flex-shrink-0 h-10 w-10 sm:h-12 sm:w-12 rounded-lg bg-indigo-100 flex items-center justify-center text-indigo-600 text-lg sm:text-xl font-bold mr-3 sm:mr-4">
                          {{ quiz.title.charAt(0).toUpperCase() }}
                      </div>
                      <div class="min-w-0">
                          <p class="text-base sm:text-lg font-bold text-gray-900 truncate">{{ quiz.title }}</p>
                          <p class="text-sm text-gray-500 flex items-center mt-1">
                              <span class="bg-gray-100 text-gray-600 py-0.5 px-2 rounded-full text-xs font-medium mr-2">
                                  {{ t('quizList.questions', { count: quiz.questions ? quiz.questions.length : 0 }) }}
                              </span>
                              <span class="text-xs text-gray-400 hidden sm:inline">ID: {{ quiz.id.substring(0, 8) }}...</span>
                          </p>
                      </div>
                  </div>
                </div>
                <div class="flex space-x-2 sm:space-x-3 flex-shrink-0 ml-2">
                  <button
                    @click="goToDetail(quiz.id)"
                    class="inline-flex items-center justify-center w-9 h-9 sm:w-auto sm:h-auto sm:px-4 sm:py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-full sm:rounded-md text-gray-700 bg-white hover:bg-gray-50 transition"
                    :title="t('quizList.manage')"
                  >
                    <svg class="w-4 h-4 sm:hidden" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.066 2.573c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.573 1.066c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.066-2.573c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                    <span class="hidden sm:inline">{{ t('quizList.manage') }}</span>
                  </button>
                  <button
                    @click="deleteQuiz(quiz.id)"
                    class="inline-flex items-center justify-center w-9 h-9 sm:w-auto sm:h-auto sm:px-4 sm:py-2 border border-transparent shadow-sm text-sm font-medium rounded-full sm:rounded-md text-white bg-red-500 hover:bg-red-600 transition"
                    :title="t('quizList.delete')"
                  >
                    🗑️
                  </button>
                </div>
              </div>
            </div>
          </li>
          <li v-if="quizzes.length === 0" class="px-6 py-12 text-center">
            <div class="mx-auto h-24 w-24 text-gray-300 mb-4">
                <svg fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 13h6m-3-3v6m-9 1V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z"></path></svg>
            </div>
            <h3 class="mt-2 text-sm font-medium text-gray-900">{{ t('quizList.emptyTitle') }}</h3>
            <p class="mt-1 text-sm text-gray-500">{{ t('quizList.emptyMessage') }}</p>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
