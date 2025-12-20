import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const nickname = ref(localStorage.getItem('nickname'))

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(newToken: string, newNickname: string) {
    token.value = newToken
    nickname.value = newNickname
    localStorage.setItem('token', newToken)
    localStorage.setItem('nickname', newNickname)
  }

  function logout() {
    token.value = null
    nickname.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('nickname')
  }

  return { token, nickname, isLoggedIn, setAuth, logout }
})
