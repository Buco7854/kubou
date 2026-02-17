<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import axios from 'axios'

const { t } = useI18n()

interface Question {
  id: string
  text: string
  options: string[]
  correctAnswerIndex: number
  tags: string[]
  difficultyLevel: number
}

const questions = ref<Question[]>([])
const newQuestion = ref({
  text: '',
  options: ['', '', '', ''],
  correctAnswerIndex: 0,
  tags: '',
  difficultyLevel: 1
})
const showCreateForm = ref(false)

const fetchQuestions = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('/api/v1/questions', {
      headers: { Authorization: `Bearer ${token}` }
    })
    questions.value = response.data
  } catch (error) {
    console.error('Erreur lors de la récupération des questions:', error)
  }
}

const createQuestion = async () => {
  try {
    const token = localStorage.getItem('token')
    const questionPayload = {
      ...newQuestion.value,
      options: newQuestion.value.options.filter(opt => opt.trim() !== ''),
      tags: newQuestion.value.tags.split(',').map(tag => tag.trim()).filter(tag => tag !== '')
    }

    if (questionPayload.options.length < 2) {
        alert(t('questionBank.minOptions'))
        return
    }

    await axios.post('/api/v1/questions', questionPayload, {
      headers: { Authorization: `Bearer ${token}` }
    })

    // Reset form and refresh list
    newQuestion.value = {
      text: '',
      options: ['', '', '', ''],
      correctAnswerIndex: 0,
      tags: '',
      difficultyLevel: 1
    }
    showCreateForm.value = false
    fetchQuestions()
  } catch (error) {
    console.error('Erreur lors de la création de la question:', error)
    alert(t('questionBank.createError'))
  }
}

const deleteQuestion = async (id: string) => {
  if (!confirm(t('questionBank.confirmDelete'))) return

  try {
    const token = localStorage.getItem('token')
    await axios.delete(`/api/v1/questions/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    fetchQuestions()
  } catch (error) {
    console.error('Erreur lors de la suppression de la question:', error)
    alert(t('questionBank.deleteError'))
  }
}

const addOption = () => {
    newQuestion.value.options.push('')
}

const removeOption = (index: number) => {
    if (newQuestion.value.options.length > 2) {
        newQuestion.value.options.splice(index, 1)
        if (newQuestion.value.correctAnswerIndex >= index && newQuestion.value.correctAnswerIndex > 0) {
            newQuestion.value.correctAnswerIndex--
        }
    }
}

onMounted(() => {
  fetchQuestions()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
    <div class="max-w-6xl mx-auto">
      <div class="flex justify-between items-center mb-6 sm:mb-8">
        <div class="min-w-0 flex-1 mr-3">
            <h2 class="text-2xl sm:text-3xl font-extrabold text-gray-900 truncate">{{ t('questionBank.title') }}</h2>
            <p class="mt-1 text-sm text-gray-500 hidden sm:block">{{ t('questionBank.subtitle') }}</p>
        </div>
        <button
          @click="showCreateForm = !showCreateForm"
          class="inline-flex items-center justify-center w-10 h-10 sm:w-auto sm:h-auto sm:px-6 sm:py-3 border border-transparent font-medium rounded-full shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 transition transform hover:scale-105"
        >
          <svg v-if="!showCreateForm" class="w-5 h-5 sm:hidden" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          <svg v-else class="w-5 h-5 sm:hidden" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
          <span class="hidden sm:inline text-base">{{ showCreateForm ? t('questionBank.cancel') : t('questionBank.addQuestion') }}</span>
        </button>
      </div>

      <!-- Create Question Form -->
      <div v-if="showCreateForm" class="bg-white rounded-2xl shadow-xl overflow-hidden border border-gray-100 mb-8 animate-fade-in">
        <div class="bg-indigo-50 px-6 py-4 border-b border-indigo-100">
            <h3 class="text-lg font-bold text-indigo-900">{{ t('questionBank.newQuestion') }}</h3>
        </div>
        <div class="p-6 sm:p-8">
            <form @submit.prevent="createQuestion" class="space-y-6">
                <div>
                <label class="block text-sm font-bold text-gray-700 mb-2">{{ t('questionBank.questionText') }}</label>
                <input v-model="newQuestion.text" type="text" class="block w-full rounded-lg border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-lg p-3 border" :placeholder="t('questionBank.questionPlaceholder')" required />
                </div>

                <div>
                    <label class="block text-sm font-bold text-gray-700 mb-2">{{ t('questionBank.options') }}</label>
                    <div class="space-y-3">
                        <div v-for="(option, index) in newQuestion.options" :key="index" class="flex items-center">
                            <div class="flex-shrink-0 h-10 w-10 rounded-l-lg flex items-center justify-center text-white font-bold"
                                 :class="index === 0 ? 'bg-red-500' : index === 1 ? 'bg-blue-500' : index === 2 ? 'bg-yellow-500' : 'bg-green-500'">
                                {{ index === 0 ? '▲' : index === 1 ? '◆' : index === 2 ? '●' : '■' }}
                            </div>
                            <input v-model="newQuestion.options[index]" type="text" class="block w-full border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm p-2.5 border-y border-r" required :placeholder="t('questionBank.optionPlaceholder', { n: index + 1 })" />

                            <div class="flex items-center px-3 bg-gray-50 border-y border-r border-gray-300 h-10">
                                <input type="radio" :value="index" v-model="newQuestion.correctAnswerIndex" class="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300" :title="t('questionBank.markCorrect')" />
                            </div>

                            <button type="button" @click="removeOption(index)" class="ml-2 text-red-500 hover:text-red-700 p-2" v-if="newQuestion.options.length > 2" :title="t('questionBank.removeOption')">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
                            </button>
                        </div>
                    </div>
                    <button type="button" @click="addOption" class="mt-2 text-sm text-indigo-600 hover:text-indigo-800 font-medium flex items-center">
                        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                        {{ t('questionBank.addOption') }}
                    </button>
                </div>

                <div class="grid grid-cols-1 gap-6 sm:grid-cols-2 bg-gray-50 p-6 rounded-xl">
                    <div>
                        <label class="block text-sm font-bold text-gray-700 mb-2">{{ t('questionBank.tags') }}</label>
                        <input v-model="newQuestion.tags" type="text" class="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm p-2.5 border" :placeholder="t('questionBank.tagsPlaceholder')" />
                    </div>
                    <div>
                        <label class="block text-sm font-bold text-gray-700 mb-2">{{ t('questionBank.difficulty') }}</label>
                        <input v-model="newQuestion.difficultyLevel" type="number" min="1" max="5" class="block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm p-2.5 border" />
                    </div>
                </div>

                <div class="flex justify-end pt-4">
                <button type="submit" class="inline-flex justify-center py-3 px-8 border border-transparent shadow-sm text-base font-bold rounded-full text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transform hover:scale-105 transition">
                    {{ t('questionBank.save') }}
                </button>
                </div>
            </form>
        </div>
      </div>

      <!-- Questions List -->
      <div class="bg-white shadow-xl rounded-2xl overflow-hidden border border-gray-100">
        <ul role="list" class="divide-y divide-gray-100">
          <li v-for="question in questions" :key="question.id" class="hover:bg-gray-50 transition duration-150">
            <div class="px-4 sm:px-6 py-4 sm:py-5">
              <div class="flex items-start sm:items-center justify-between gap-2 mb-2">
                <p class="text-base sm:text-lg font-bold text-gray-900 min-w-0 break-words">{{ question.text }}</p>
                <div class="flex items-center space-x-2 sm:space-x-3 flex-shrink-0">
                    <span class="hidden sm:inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800">
                    {{ t('questionBank.level', { level: question.difficultyLevel }) }}
                    </span>
                    <button @click="deleteQuestion(question.id)" class="text-red-500 hover:text-red-700 p-1 rounded-full hover:bg-red-50 transition" :title="t('questionBank.removeOption')">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                    </button>
                </div>
              </div>
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 mb-3">
                  <div v-for="(opt, idx) in question.options" :key="idx"
                       class="text-sm px-3 py-1 rounded border flex items-center"
                       :class="idx === question.correctAnswerIndex ? 'bg-green-50 border-green-200 text-green-700 font-medium' : 'bg-gray-50 border-gray-100 text-gray-500'">
                      <span v-if="idx === question.correctAnswerIndex" class="mr-2 text-green-600">✓</span>
                      {{ opt }}
                  </div>
              </div>
              <div class="flex items-center text-sm text-gray-500">
                <span class="mr-2">{{ t('questionBank.tags') }}:</span>
                <span v-if="question.tags && question.tags.length" class="flex gap-1">
                    <span v-for="tag in question.tags" :key="tag" class="bg-gray-100 px-2 py-0.5 rounded text-xs">#{{ tag }}</span>
                </span>
                <span v-else class="italic text-gray-400">{{ t('questionBank.noTags') }}</span>
              </div>
            </div>
          </li>
          <li v-if="questions.length === 0" class="px-6 py-12 text-center text-gray-500">
            <div class="text-4xl mb-2">📭</div>
            <p>{{ t('questionBank.emptyBank') }}</p>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
    animation: fadeIn 0.4s ease-out;
}
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(-10px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>
