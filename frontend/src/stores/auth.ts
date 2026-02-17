import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import i18n from '../i18n'
import type { SupportedLanguage } from '../i18n'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const nickname = ref(localStorage.getItem('nickname'))
  const isGuest = ref(localStorage.getItem('isGuest') === 'true')
  const language = ref<SupportedLanguage>((localStorage.getItem('language') as SupportedLanguage) || 'fr')

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(newToken: string, newNickname: string, guest: boolean = false) {
    token.value = newToken
    nickname.value = newNickname
    isGuest.value = guest
    localStorage.setItem('token', newToken)
    localStorage.setItem('nickname', newNickname)
    localStorage.setItem('isGuest', String(guest))
  }

  function setLanguage(lang: SupportedLanguage) {
    language.value = lang
    localStorage.setItem('language', lang)
    i18n.global.locale.value = lang
  }

  function logout() {
    token.value = null
    nickname.value = null
    isGuest.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('nickname')
    localStorage.removeItem('isGuest')
  }

  return { token, nickname, isGuest, isLoggedIn, language, setAuth, setLanguage, logout }
})
