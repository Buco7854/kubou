<script setup lang="ts">
import { RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const sidebarOpen = ref(false)

// Hide navigation on game screens to prevent distractions
const showNav = computed(() => {
  return !['lobby', 'game'].includes(route.name as string)
})

// Close sidebar on route change
watch(() => route.path, () => {
  sidebarOpen.value = false
})

const logout = () => {
  authStore.logout()
  sidebarOpen.value = false
  router.push('/')
}

const navLinks = computed(() => {
  if (!authStore.isLoggedIn || authStore.isGuest) return []
  return [
    { to: '/quizzes', label: t('nav.myQuizzes') },
    { to: '/questions', label: t('nav.questionBank') },
    { to: '/achievements', label: t('nav.achievements') }
  ]
})
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <!-- Navigation Bar -->
    <nav v-if="showNav" class="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-14 sm:h-16">
          <!-- Left: Logo + Desktop links -->
          <div class="flex items-center">
            <div class="flex-shrink-0 flex items-center cursor-pointer" @click="router.push('/')">
              <span class="text-xl sm:text-2xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600">
                Kubou
              </span>
            </div>
            <!-- Desktop nav links (hidden on mobile) -->
            <div class="hidden md:ml-6 md:flex md:space-x-6" v-if="authStore.isLoggedIn && !authStore.isGuest">
              <router-link
                v-for="link in navLinks"
                :key="link.to"
                :to="link.to"
                class="border-transparent text-gray-500 hover:border-indigo-500 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium transition-colors"
                active-class="border-indigo-500 text-gray-900"
              >
                {{ link.label }}
              </router-link>
            </div>
          </div>

          <!-- Right: Desktop auth + Mobile hamburger -->
          <div class="flex items-center space-x-3">
            <!-- Desktop auth (hidden on mobile) -->
            <div class="hidden md:flex items-center space-x-4">
              <template v-if="authStore.isLoggedIn">
                <span class="text-sm font-medium text-gray-700">
                  {{ authStore.nickname }}
                  <span v-if="authStore.isGuest" class="ml-1 text-xs bg-gray-200 text-gray-600 px-2 py-0.5 rounded-full">{{ t('nav.guest') }}</span>
                </span>
                <button
                  @click="logout"
                  class="text-sm text-red-600 hover:text-red-800 font-medium"
                >
                  {{ t('nav.logout') }}
                </button>
              </template>
              <template v-else>
                <router-link
                  to="/login"
                  class="text-indigo-600 hover:text-indigo-900 font-medium text-sm"
                >
                  {{ t('nav.login') }}
                </router-link>
              </template>
            </div>

            <!-- Mobile hamburger button -->
            <button
              @click="sidebarOpen = !sidebarOpen"
              class="md:hidden inline-flex items-center justify-center p-2 rounded-md text-gray-500 hover:text-gray-700 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-indigo-500 transition-colors"
              aria-label="Menu"
            >
              <svg v-if="!sidebarOpen" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
              <svg v-else class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </nav>

    <!-- Mobile Sidebar Overlay -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="sidebarOpen"
          class="fixed inset-0 bg-black/40 z-[60] md:hidden"
          @click="sidebarOpen = false"
        />
      </Transition>

      <Transition name="slide">
        <div
          v-if="sidebarOpen"
          class="fixed top-0 right-0 h-full w-72 bg-white shadow-2xl z-[70] md:hidden flex flex-col"
        >
          <!-- Sidebar header -->
          <div class="flex items-center justify-between px-4 py-4 border-b border-gray-100">
            <span class="text-lg font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600">
              Kubou
            </span>
            <button
              @click="sidebarOpen = false"
              class="p-2 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
            >
              <svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <!-- User info -->
          <div v-if="authStore.isLoggedIn" class="px-4 py-3 bg-gray-50 border-b border-gray-100">
            <div class="flex items-center space-x-2">
              <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center">
                <span class="text-sm font-bold text-indigo-600">{{ authStore.nickname?.charAt(0)?.toUpperCase() }}</span>
              </div>
              <div>
                <p class="text-sm font-medium text-gray-900">{{ authStore.nickname }}</p>
                <p v-if="authStore.isGuest" class="text-xs text-gray-500">{{ t('nav.guest') }}</p>
              </div>
            </div>
          </div>

          <!-- Navigation links -->
          <nav class="flex-1 px-2 py-3 space-y-1 overflow-y-auto">
            <router-link
              to="/"
              class="flex items-center px-3 py-2.5 text-sm font-medium rounded-lg transition-colors"
              :class="route.path === '/' ? 'bg-indigo-50 text-indigo-700' : 'text-gray-700 hover:bg-gray-50'"
            >
              {{ t('nav.home') }}
            </router-link>

            <template v-if="authStore.isLoggedIn && !authStore.isGuest">
              <router-link
                v-for="link in navLinks"
                :key="link.to"
                :to="link.to"
                class="flex items-center px-3 py-2.5 text-sm font-medium rounded-lg transition-colors"
                :class="route.path === link.to ? 'bg-indigo-50 text-indigo-700' : 'text-gray-700 hover:bg-gray-50'"
              >
                {{ link.label }}
              </router-link>
            </template>
          </nav>

          <!-- Sidebar footer -->
          <div class="border-t border-gray-100 px-4 py-3">
            <template v-if="authStore.isLoggedIn">
              <button
                @click="logout"
                class="w-full flex items-center justify-center px-3 py-2.5 text-sm font-medium text-red-600 hover:bg-red-50 rounded-lg transition-colors"
              >
                {{ t('nav.logout') }}
              </button>
            </template>
            <template v-else>
              <router-link
                to="/login"
                class="w-full flex items-center justify-center px-3 py-2.5 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg transition-colors"
              >
                {{ t('nav.login') }}
              </router-link>
            </template>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Main Content -->
    <main class="flex-grow">
      <RouterView />
    </main>

    <!-- Footer -->
    <footer v-if="showNav" class="bg-white border-t border-gray-200 mt-auto">
      <div class="max-w-7xl mx-auto py-4 sm:py-6 px-4 sm:px-6 lg:px-8">
        <p class="text-center text-xs sm:text-sm text-gray-400">
          &copy; {{ new Date().getFullYear() }} Kubou. {{ t('footer.tagline') }}
        </p>
      </div>
    </footer>
  </div>
</template>

<style>
body {
  @apply bg-gray-50;
}

/* Sidebar overlay fade */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Sidebar slide */
.slide-enter-active {
  transition: transform 0.25s ease-out;
}
.slide-leave-active {
  transition: transform 0.2s ease-in;
}
.slide-enter-from {
  transform: translateX(100%);
}
.slide-leave-to {
  transform: translateX(100%);
}
</style>
