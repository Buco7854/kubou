import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const nickname = ref(localStorage.getItem('nickname'))
  const isGuest = ref(localStorage.getItem('isGuest') === 'true')

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(newToken: string, newNickname: string, guest: boolean = false) {
    token.value = newToken
    nickname.value = newNickname
    isGuest.value = guest
    localStorage.setItem('token', newToken)
    localStorage.setItem('nickname', newNickname)
    localStorage.setItem('isGuest', String(guest))
  }

  function logout() {
    token.value = null
    nickname.value = null
    isGuest.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('nickname')
    localStorage.removeItem('isGuest')
  }

  return { token, nickname, isGuest, isLoggedIn, setAuth, logout }
})
