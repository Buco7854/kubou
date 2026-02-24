<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { SUPPORTED_LANGUAGES } from '../i18n'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const title = ref('')
const text = ref('')
const selectedLanguage = ref(authStore.language)
const amount = ref(5)
const loading = ref(false)
const error = ref('')

const generateQuiz = async () => {
  if (!title.value.trim()) {
    error.value = t('aiQuiz.titleRequired')
    return
  }
  if (!text.value.trim()) {
    error.value = t('aiQuiz.textRequired')
    return
  }
  if (amount.value < 1 || amount.value > 20) {
    error.value = t('aiQuiz.amountRange')
    return
  }

  loading.value = true
  error.value = ''

  try {
    const token = localStorage.getItem('token')
    const response = await axios.post('/api/v1/quizzes/generate-ai', {
      title: title.value,
      text: text.value,
      amount: amount.value,
      language: selectedLanguage.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    router.push(`/quiz/${response.data.id}`)
  } catch (err: any) {
    error.value = err.response?.data?.message || t('aiQuiz.generateError')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-lg">
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        {{ t('aiQuiz.title') }}
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        {{ t('aiQuiz.subtitle') }}
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-lg">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <form @submit.prevent="generateQuiz" class="space-y-6">
          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('aiQuiz.quizTitle') }}</label>
            <div class="mt-1">
              <input
                v-model="title"
                type="text"
                required
                class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                :placeholder="t('aiQuiz.titlePlaceholder')"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('aiQuiz.textContent') }}</label>
            <div class="mt-1">
              <textarea
                v-model="text"
                rows="8"
                required
                class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                :placeholder="t('aiQuiz.textPlaceholder')"
              />
            </div>
            <p class="mt-1 text-xs text-gray-500">{{ t('aiQuiz.textHint') }}</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('aiQuiz.language') }}</label>
            <div class="mt-1">
              <select
                v-model="selectedLanguage"
                class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              >
                <option v-for="lang in SUPPORTED_LANGUAGES" :key="lang" :value="lang">
                  {{ t(`languages.${lang}`) }}
                </option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('aiQuiz.questionCount') }}</label>
            <div class="mt-1">
              <input
                v-model.number="amount"
                type="number"
                min="1"
                max="20"
                class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              />
            </div>
          </div>

          <div v-if="error" class="rounded-md bg-red-50 p-4">
            <p class="text-sm text-red-700">{{ error }}</p>
          </div>

          <div>
            <button
              type="submit"
              :disabled="loading"
              class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="loading">{{ t('aiQuiz.generating') }}</span>
              <span v-else>{{ t('aiQuiz.submit') }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
