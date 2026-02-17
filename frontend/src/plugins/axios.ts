import axios from 'axios'
import { useAuthStore } from '../stores/auth'
import router from '../router'
import i18n from '../i18n'

let isRedirecting = false

export function setupAxiosInterceptors() {
  // Response interceptor: detect 401 (JWT expired/invalid) and auto-logout
  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401 && !isRedirecting) {
        const authStore = useAuthStore()

        // Only auto-logout if the user was logged in (not on login page itself)
        if (authStore.isLoggedIn) {
          isRedirecting = true
          authStore.logout()

          const t = i18n.global.t
          alert(t('nav.sessionExpired'))

          router.push('/login').finally(() => {
            isRedirecting = false
          })
        }
      }
      return Promise.reject(error)
    }
  )
}

/**
 * Checks if a JWT token is expired by decoding the payload.
 * Returns true if expired or invalid, false if still valid.
 */
export function isTokenExpired(token: string | null): boolean {
  if (!token) return true

  try {
    const parts = token.split('.')
    if (parts.length !== 3) return true

    const payload = JSON.parse(atob(parts[1]))
    if (!payload.exp) return false // No expiry claim = assume valid

    const now = Math.floor(Date.now() / 1000)
    return payload.exp < now
  } catch {
    return true
  }
}
