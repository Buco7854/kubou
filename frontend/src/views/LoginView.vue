<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const username = ref('')
const password = ref('')
const nickname = ref('')
const isGuest = ref(false)

const handleLogin = async () => {
  try {
    const response = await axios.post('/api/v1/auth/login', {
      username: username.value,
      password: password.value
    })
    localStorage.setItem('token', response.data.token)
    router.push('/')
  } catch (error) {
    console.error('Login failed', error)
    alert('Login failed')
  }
}

const handleGuestLogin = async () => {
  try {
    const response = await axios.post('/api/v1/auth/guest', {
      nickname: nickname.value
    })
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('nickname', response.data.nickname)
    router.push('/')
  } catch (error) {
    console.error('Guest login failed', error)
    alert('Guest login failed')
  }
}
</script>

<template>
  <div class="max-w-md mx-auto bg-white p-8 rounded-lg shadow-md">
    <div class="flex justify-center mb-6">
      <button
        @click="isGuest = false"
        :class="['px-4 py-2 rounded-l-md', !isGuest ? 'bg-indigo-600 text-white' : 'bg-gray-200 text-gray-700']"
      >
        Login
      </button>
      <button
        @click="isGuest = true"
        :class="['px-4 py-2 rounded-r-md', isGuest ? 'bg-indigo-600 text-white' : 'bg-gray-200 text-gray-700']"
      >
        Guest
      </button>
    </div>

    <div v-if="!isGuest">
      <h2 class="text-2xl font-bold mb-6 text-center">Login</h2>
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Username</label>
          <input v-model="username" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" required />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Password</label>
          <input v-model="password" type="password" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" required />
        </div>
        <button type="submit" class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
          Sign In
        </button>
      </form>
    </div>

    <div v-else>
      <h2 class="text-2xl font-bold mb-6 text-center">Guest Access</h2>
      <form @submit.prevent="handleGuestLogin" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">Nickname</label>
          <input v-model="nickname" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm border p-2" required />
        </div>
        <button type="submit" class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
          Join as Guest
        </button>
      </form>
    </div>
  </div>
</template>
