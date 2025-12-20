<script setup lang="ts">
import { RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { computed } from 'vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Hide navigation on game screens to prevent distractions
const showNav = computed(() => {
  return !['lobby', 'game'].includes(route.name as string)
})

const logout = () => {
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <!-- Navigation Bar -->
    <nav v-if="showNav" class="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex">
            <div class="flex-shrink-0 flex items-center cursor-pointer" @click="router.push('/')">
              <span class="text-2xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600">
                Kubou
              </span>
            </div>
            <!-- Only show these links if logged in AND NOT a guest -->
            <div class="hidden sm:ml-6 sm:flex sm:space-x-8" v-if="authStore.isLoggedIn && !authStore.isGuest">
              <router-link
                to="/quizzes"
                class="border-transparent text-gray-500 hover:border-indigo-500 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
                active-class="border-indigo-500 text-gray-900"
              >
                Mes Quiz
              </router-link>
              <router-link
                to="/questions"
                class="border-transparent text-gray-500 hover:border-indigo-500 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
                active-class="border-indigo-500 text-gray-900"
              >
                Banque de Questions
              </router-link>
            </div>
          </div>
          <div class="flex items-center">
            <div v-if="authStore.isLoggedIn" class="flex items-center space-x-4">
              <span class="text-sm font-medium text-gray-700">
                {{ authStore.nickname }}
                <span v-if="authStore.isGuest" class="ml-1 text-xs bg-gray-200 text-gray-600 px-2 py-0.5 rounded-full">Invité</span>
              </span>
              <button
                @click="logout"
                class="text-sm text-red-600 hover:text-red-800 font-medium"
              >
                Déconnexion
              </button>
            </div>
            <div v-else class="flex space-x-4">
              <router-link
                to="/login"
                class="text-indigo-600 hover:text-indigo-900 font-medium text-sm"
              >
                Connexion
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="flex-grow">
      <RouterView />
    </main>

    <!-- Footer -->
    <footer v-if="showNav" class="bg-white border-t border-gray-200 mt-auto">
        <div class="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
            <p class="text-center text-sm text-gray-400">
                &copy; {{ new Date().getFullYear() }} Kubou. Learning made fun.
            </p>
        </div>
    </footer>
  </div>
</template>

<style>
body {
  @apply bg-gray-50;
}
</style>
