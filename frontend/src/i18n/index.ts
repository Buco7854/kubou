import { createI18n } from 'vue-i18n'
import fr from './fr'
import en from './en'

export const SUPPORTED_LANGUAGES = ['fr', 'en'] as const
export type SupportedLanguage = typeof SUPPORTED_LANGUAGES[number]

const savedLanguage = localStorage.getItem('language') || 'fr'

const i18n = createI18n({
  legacy: false,
  locale: savedLanguage,
  fallbackLocale: 'fr',
  messages: { fr, en }
})

export default i18n
