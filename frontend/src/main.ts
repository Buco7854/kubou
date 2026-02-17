import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue'
import router from './router'
import i18n from './i18n'
import { setupAxiosInterceptors, isTokenExpired } from './plugins/axios'

const pinia = createPinia()
const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(i18n)

// Set up axios interceptors (must be after pinia is installed)
setupAxiosInterceptors()

// Check for expired token on app startup
const storedToken = localStorage.getItem('token')
if (storedToken && isTokenExpired(storedToken)) {
  // Token is already expired, clean up immediately
  localStorage.removeItem('token')
  localStorage.removeItem('nickname')
  localStorage.removeItem('isGuest')
}

app.mount('#app')
