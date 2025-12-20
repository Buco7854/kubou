<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
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
    authStore.setAuth(response.data.token, username.value, false)

    // Redirect to intended page or home
    const redirectPath = route.query.redirect as string || '/'
    router.push(redirectPath)
  } catch (error) {
    console.error('Échec de la connexion', error)
    alert('Échec de la connexion')
  }
}

const handleGuestLogin = async () => {
  try {
    const response = await axios.post('/api/v1/auth/guest', {
      nickname: nickname.value
    })
    authStore.setAuth(response.data.token, response.data.nickname, true)

    // Redirect to intended page or home
    const redirectPath = route.query.redirect as string || '/'
    router.push(redirectPath)
  } catch (error) {
    console.error('Échec de la connexion invité', error)
    alert('Échec de la connexion invité')
  }
}

onMounted(() => {
    // If user was redirected here because they tried to access a lobby,
    // default to guest mode for convenience
    if (route.query.redirect && (route.query.redirect as string).includes('/lobby/')) {
        isGuest.value = true
    }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        Bienvenue sur Kubou
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        Connectez-vous pour créer des quiz ou rejoignez en tant qu'invité.
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">

        <!-- Toggle -->
        <div class="flex justify-center mb-8 bg-gray-100 p-1 rounded-lg">
          <button
            @click="isGuest = false"
            :class="['flex-1 py-2 text-sm font-medium rounded-md transition-all', !isGuest ? 'bg-white text-indigo-600 shadow-sm' : 'text-gray-500 hover:text-gray-700']"
          >
            Compte Kubou
          </button>
          <button
            @click="isGuest = true"
            :class="['flex-1 py-2 text-sm font-medium rounded-md transition-all', isGuest ? 'bg-white text-indigo-600 shadow-sm' : 'text-gray-500 hover:text-gray-700']"
          >
            Invité Rapide
          </button>
        </div>

        <!-- Login Form -->
        <div v-if="!isGuest" class="animate-fade-in">
          <form @submit.prevent="handleLogin" class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-gray-700">Nom d'utilisateur</label>
              <div class="mt-1">
                <input v-model="username" type="text" required class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700">Mot de passe</label>
              <div class="mt-1">
                <input v-model="password" type="password" required class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" />
              </div>
            </div>

            <div>
              <button type="submit" class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
                Se connecter
              </button>
            </div>
          </form>
        </div>

        <!-- Guest Form -->
        <div v-else class="animate-fade-in">
          <form @submit.prevent="handleGuestLogin" class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-gray-700">Choisissez un pseudo</label>
              <div class="mt-1">
                <input v-model="nickname" type="text" required placeholder="ex: SuperJoueur123" class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" />
              </div>
            </div>

            <div>
              <button type="submit" class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500">
                Rejoindre
              </button>
            </div>
          </form>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
    animation: fadeIn 0.3s ease-in-out;
}
@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}
</style>
