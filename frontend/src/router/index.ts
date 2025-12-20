import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import CreateQuizView from '../views/CreateQuizView.vue'
import QuestionBankView from '../views/QuestionBankView.vue'
import QuizDetailView from '../views/QuizDetailView.vue'
import LobbyView from '../views/LobbyView.vue'
import GameView from '../views/GameView.vue'
import QuizListView from '../views/QuizListView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/quizzes',
      name: 'quizzes',
      component: QuizListView
    },
    {
      path: '/create-quiz',
      name: 'create-quiz',
      component: CreateQuizView
    },
    {
      path: '/questions',
      name: 'questions',
      component: QuestionBankView
    },
    {
      path: '/quiz/:id',
      name: 'quiz-detail',
      component: QuizDetailView
    },
    {
      path: '/lobby/:pin',
      name: 'lobby',
      component: LobbyView
    },
    {
      path: '/game/:id',
      name: 'game',
      component: GameView
    }
  ]
})

export default router
