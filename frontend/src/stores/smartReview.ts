import { defineStore } from 'pinia'
import type { QuizDTO } from '../types/quiz'

export const useSmartReviewStore = defineStore('smartReview', {
  state: () => ({
    smartReviewQuiz: null as QuizDTO | null,
  }),
  actions: {
    setSmartReviewQuiz(quiz: QuizDTO) {
      this.smartReviewQuiz = quiz
    },
    clearSmartReviewQuiz() {
      this.smartReviewQuiz = null
    },
  },
  getters: {
    getSmartReviewQuiz: (state) => state.smartReviewQuiz,
  },
})
