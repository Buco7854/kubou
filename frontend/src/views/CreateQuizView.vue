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
    console.log('Quiz created:', response.data)
    // Redirect to quiz edit page or list (for now just alert)
    alert(`Quiz "${response.data.title}" created! ID: ${response.data.id}`)
    title.value = ''
  } catch (error) {
    console.error('Error creating quiz:', error)
    alert('Failed to create quiz. Please login first.')
  }
}
</script>

<template>
  <div class="max-w-2xl mx-auto bg-white p-8 rounded-lg shadow-md">
    <h2 class="text-2xl font-bold mb-6">Create New Quiz</h2>
    <form @submit.prevent="createQuiz" class="space-y-6">
      <div>
        <label class="block text-sm font-medium text-gray-700">Quiz Title</label>
        <input v-model="title" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" placeholder="e.g., Math 101 - Algebra" required />
      </div>
      <div class="flex justify-end">
        <button type="submit" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
          Create Quiz
        </button>
      </div>
    </form>
  </div>
</template>
