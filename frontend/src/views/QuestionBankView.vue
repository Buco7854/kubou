<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

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
    console.error('Error fetching questions:', error)
  }
}

const createQuestion = async () => {
  try {
    const token = localStorage.getItem('token')
    const questionPayload = {
      ...newQuestion.value,
      tags: newQuestion.value.tags.split(',').map(tag => tag.trim())
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
    console.error('Error creating question:', error)
    alert('Failed to create question')
  }
}

onMounted(() => {
  fetchQuestions()
})
</script>

<template>
  <div class="max-w-4xl mx-auto">
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-900">Question Bank</h2>
      <button
        @click="showCreateForm = !showCreateForm"
        class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700"
      >
        {{ showCreateForm ? 'Cancel' : 'Add Question' }}
      </button>
    </div>

    <!-- Create Question Form -->
    <div v-if="showCreateForm" class="bg-white p-6 rounded-lg shadow-md mb-8">
      <h3 class="text-lg font-medium mb-4">New Question</h3>
      <form @submit.prevent="createQuestion" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Question Text</label>
          <input v-model="newQuestion.text" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" required />
        </div>

        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div v-for="(option, index) in newQuestion.options" :key="index">
            <label class="block text-sm font-medium text-gray-700">Option {{ index + 1 }}</label>
            <input v-model="newQuestion.options[index]" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" required />
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700">Correct Answer (Option Number)</label>
          <select v-model="newQuestion.correctAnswerIndex" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2">
            <option v-for="(option, index) in newQuestion.options" :key="index" :value="index">
              Option {{ index + 1 }}
            </option>
          </select>
        </div>

        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label class="block text-sm font-medium text-gray-700">Tags (comma separated)</label>
            <input v-model="newQuestion.tags" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" placeholder="math, algebra" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Difficulty (1-5)</label>
            <input v-model="newQuestion.difficultyLevel" type="number" min="1" max="5" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" />
          </div>
        </div>

        <div class="flex justify-end pt-4">
          <button type="submit" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500">
            Save Question
          </button>
        </div>
      </form>
    </div>

    <!-- Questions List -->
    <div class="bg-white shadow overflow-hidden sm:rounded-md">
      <ul role="list" class="divide-y divide-gray-200">
        <li v-for="question in questions" :key="question.id">
          <div class="px-4 py-4 sm:px-6">
            <div class="flex items-center justify-between">
              <p class="text-sm font-medium text-indigo-600 truncate">{{ question.text }}</p>
              <div class="ml-2 flex-shrink-0 flex">
                <p class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                  Level {{ question.difficultyLevel }}
                </p>
              </div>
            </div>
            <div class="mt-2 sm:flex sm:justify-between">
              <div class="sm:flex">
                <p class="flex items-center text-sm text-gray-500">
                  Tags: {{ question.tags ? question.tags.join(', ') : 'None' }}
                </p>
              </div>
            </div>
          </div>
        </li>
        <li v-if="questions.length === 0" class="px-4 py-4 sm:px-6 text-center text-gray-500">
          No questions found. Add some!
        </li>
      </ul>
    </div>
  </div>
</template>
