<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { SUPPORTED_LANGUAGES } from '../i18n'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const title = ref('')
const source = ref('opentdb')
const selectedCategory = ref('')
const selectedLanguage = ref(authStore.language)
const amount = ref(5)
const loading = ref(false)
const error = ref('')

const sources = computed(() => [
  { value: 'opentdb', label: t('sources.opentdb'), language: 'en' },
  { value: 'quizzapi', label: t('sources.quizzapi'), language: 'fr' },
  { value: 'quizapi', label: t('sources.quizapi'), language: 'en' }
])

const currentSourceLanguage = computed(() => {
  const s = sources.value.find(s => s.value === source.value)
  return s ? s.language : null
})

const quizapiCategories = computed(() => [
  { value: '', label: t('importQuiz.allCategories') },
  { value: 'linux', label: t('categories.linux') },
  { value: 'devops', label: t('categories.devops') },
  { value: 'docker', label: t('categories.docker') },
  { value: 'sql', label: t('categories.sql') },
  { value: 'cms', label: t('categories.cms') },
  { value: 'code', label: t('categories.code') }
])

const quizzapiCategories = computed(() => [
  { value: '', label: t('importQuiz.allCategories') },
  { value: 'culture_generale', label: t('categories.culture_generale') },
  { value: 'musique', label: t('categories.musique') },
  { value: 'art_litterature', label: t('categories.art_litterature') },
  { value: 'tv_cinema', label: t('categories.tv_cinema') },
  { value: 'sport', label: t('categories.sport') },
  { value: 'histoire', label: t('categories.histoire') },
  { value: 'geographie', label: t('categories.geographie') },
  { value: 'science', label: t('categories.science') },
  { value: 'jeux_videos', label: t('categories.jeux_videos') },
  { value: 'gastronomie', label: t('categories.gastronomie') },
  { value: 'actu_politique', label: t('categories.actu_politique') }
])

const opentdbCategories = computed(() => [
  { value: '', label: t('importQuiz.allCategories') },
  { value: 'general_knowledge', label: t('categories.general_knowledge') },
  { value: 'books', label: t('categories.books') },
  { value: 'film', label: t('categories.film') },
  { value: 'music', label: t('categories.music') },
  { value: 'musicals_theatres', label: t('categories.musicals_theatres') },
  { value: 'television', label: t('categories.television') },
  { value: 'video_games', label: t('categories.video_games') },
  { value: 'board_games', label: t('categories.board_games') },
  { value: 'science_nature', label: t('categories.science_nature') },
  { value: 'computers', label: t('categories.computers') },
  { value: 'mathematics', label: t('categories.mathematics') },
  { value: 'mythology', label: t('categories.mythology') },
  { value: 'sports', label: t('categories.sports') },
  { value: 'geography', label: t('categories.geography') },
  { value: 'history', label: t('categories.history') },
  { value: 'politics', label: t('categories.politics') },
  { value: 'art', label: t('categories.art') },
  { value: 'celebrities', label: t('categories.celebrities') },
  { value: 'animals', label: t('categories.animals') },
  { value: 'vehicles', label: t('categories.vehicles') },
  { value: 'comics', label: t('categories.comics') },
  { value: 'gadgets', label: t('categories.gadgets') },
  { value: 'anime_manga', label: t('categories.anime_manga') },
  { value: 'cartoon_animations', label: t('categories.cartoon_animations') }
])

const categories = computed(() => {
  if (source.value === 'quizapi') return quizapiCategories.value
  if (source.value === 'quizzapi') return quizzapiCategories.value
  return opentdbCategories.value
})

const onSourceChange = () => {
  selectedCategory.value = ''
}

const importQuiz = async () => {
  if (!title.value.trim()) {
    error.value = t('importQuiz.titleRequired')
    return
  }
  if (amount.value < 1 || amount.value > 20) {
    error.value = t('importQuiz.amountRange')
    return
  }

  loading.value = true
  error.value = ''

  try {
    const token = localStorage.getItem('token')
    const tags = selectedCategory.value ? [selectedCategory.value] : []
    const response = await axios.post('/api/v1/quizzes/import', {
      title: title.value,
      source: source.value,
      tags,
      amount: amount.value,
      language: selectedLanguage.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })
    router.push(`/quiz/${response.data.id}`)
  } catch (err: any) {
    error.value = err.response?.data?.message || t('importQuiz.importError')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        {{ t('importQuiz.title') }}
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        {{ t('importQuiz.subtitle') }}
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <form @submit.prevent="importQuiz" class="space-y-6">
          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('importQuiz.quizTitle') }}</label>
            <div class="mt-1">
              <input
                v-model="title"
                type="text"
                required
                class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                :placeholder="t('importQuiz.titlePlaceholder')"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('importQuiz.source') }}</label>
            <div class="mt-1">
              <select
                v-model="source"
                @change="onSourceChange"
                class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              >
                <option v-for="s in sources" :key="s.value" :value="s.value">{{ s.label }}</option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('importQuiz.category') }}</label>
            <div class="mt-1">
              <select
                v-model="selectedCategory"
                class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
              >
                <option v-for="cat in categories" :key="cat.value" :value="cat.value">{{ cat.label }}</option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('importQuiz.language') }}</label>
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
            <p v-if="currentSourceLanguage && currentSourceLanguage !== selectedLanguage" class="mt-1 text-xs text-amber-600">
              {{ t('importQuiz.translationNotice') }}
            </p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('importQuiz.questionCount') }}</label>
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
              <span v-if="loading">{{ t('importQuiz.importing') }}</span>
              <span v-else>{{ t('importQuiz.submit') }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
