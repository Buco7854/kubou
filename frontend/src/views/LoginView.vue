<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import { useI18n } from 'vue-i18n'
import { SUPPORTED_LANGUAGES } from '../i18n'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const username = ref('')
const password = ref('')
const nickname = ref('')
const selectedLanguage = ref(authStore.language)
const isGuest = ref(false)
const isRegistering = ref(false)

const handleLogin = async () => {
  try {
    const response = await axios.post('/api/v1/auth/login', {
      username: username.value,
      password: password.value
    })
    const lang = response.data.language || 'fr'
    authStore.setAuth(response.data.token, username.value, false)
    authStore.setLanguage(lang)

    const redirectPath = route.query.redirect as string || '/'
    router.push(redirectPath)
  } catch (error) {
    console.error('Login failed', error)
    alert(t('login.loginFailed'))
  }
}

const handleRegister = async () => {
  try {
    await axios.post('/api/v1/auth/register', {
      username: username.value,
      password: password.value,
      language: selectedLanguage.value
    })
    authStore.setLanguage(selectedLanguage.value)
    alert(t('login.registerSuccess'))
    isRegistering.value = false
  } catch (error) {
    console.error('Registration failed', error)
    alert(t('login.registerFailed'))
  }
}

const handleGuestLogin = async () => {
  try {
    const response = await axios.post('/api/v1/auth/guest', {
      nickname: nickname.value
    })
    authStore.setAuth(response.data.token, response.data.nickname, true)

    const redirectPath = route.query.redirect as string || '/'
    router.push(redirectPath)
  } catch (error) {
    console.error('Guest login failed', error)
    alert(t('login.guestFailed'))
  }
}

onMounted(() => {
    if (route.query.redirect && (route.query.redirect as string).includes('/lobby/')) {
        isGuest.value = true
    }
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-md">
      <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
        {{ t('login.welcome') }}
      </h2>
      <p class="mt-2 text-center text-sm text-gray-600">
        {{ t('login.subtitle') }}
      </p>
    </div>

    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">

        <!-- Toggle Guest/Account -->
        <div class="flex justify-center mb-8 bg-gray-100 p-1 rounded-lg">
          <button
            @click="isGuest = false"
            :class="['flex-1 py-2 text-sm font-medium rounded-md transition-all', !isGuest ? 'bg-white text-indigo-600 shadow-sm' : 'text-gray-500 hover:text-gray-700']"
          >
            {{ t('login.accountTab') }}
          </button>
          <button
            @click="isGuest = true"
            :class="['flex-1 py-2 text-sm font-medium rounded-md transition-all', isGuest ? 'bg-white text-indigo-600 shadow-sm' : 'text-gray-500 hover:text-gray-700']"
          >
            {{ t('login.guestTab') }}
          </button>
        </div>

        <!-- Account Forms (Login / Register) -->
        <div v-if="!isGuest" class="animate-fade-in">
          <div class="mb-6 flex justify-between items-center border-b border-gray-200 pb-2">
              <h3 class="text-lg font-medium text-gray-900">
                  {{ isRegistering ? t('login.createAccount') : t('login.signIn') }}
              </h3>
              <button @click="isRegistering = !isRegistering" class="text-sm text-indigo-600 hover:text-indigo-500 font-medium">
                  {{ isRegistering ? t('login.hasAccount') : t('login.noAccount') }}
              </button>
          </div>

          <form @submit.prevent="isRegistering ? handleRegister() : handleLogin()" class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-gray-700">{{ t('login.username') }}</label>
              <div class="mt-1">
                <input v-model="username" type="text" required class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" />
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700">{{ t('login.password') }}</label>
              <div class="mt-1">
                <input v-model="password" type="password" required class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" />
              </div>
            </div>

            <!-- Language selector (registration only) -->
            <div v-if="isRegistering">
              <label class="block text-sm font-medium text-gray-700">{{ t('login.language') }}</label>
              <div class="mt-1">
                <select v-model="selectedLanguage" class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm">
                  <option v-for="lang in SUPPORTED_LANGUAGES" :key="lang" :value="lang">
                    {{ t(`languages.${lang}`) }}
                  </option>
                </select>
              </div>
            </div>

            <div>
              <button type="submit" class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
                {{ isRegistering ? t('login.register') : t('login.signIn') }}
              </button>
            </div>
          </form>
        </div>

        <!-- Guest Form -->
        <div v-else class="animate-fade-in">
          <form @submit.prevent="handleGuestLogin" class="space-y-6">
            <div>
              <label class="block text-sm font-medium text-gray-700">{{ t('login.chooseNickname') }}</label>
              <div class="mt-1">
                <input v-model="nickname" type="text" required :placeholder="t('login.nicknamePlaceholder')" class="appearance-none block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm" />
              </div>
            </div>

            <div>
              <button type="submit" class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500">
                {{ t('login.joinButton') }}
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
