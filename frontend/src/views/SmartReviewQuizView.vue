<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useSmartReviewStore } from '../stores/smartReview'

const router = useRouter()
const smartReviewStore = useSmartReviewStore()
const quiz = computed(() => smartReviewStore.getSmartReviewQuiz)

const currentQuestionIndex = ref(0)
const selectedAnswerIndex = ref<number | null>(null)
const showResult = ref(false)
const isCorrect = ref(false)
const score = ref(0)

const currentQuestion = computed(() => {
  if (!quiz.value || !quiz.value.questions) return null
  return quiz.value.questions[currentQuestionIndex.value]
})

const isLastQuestion = computed(() => {
  if (!quiz.value || !quiz.value.questions) return true
  return currentQuestionIndex.value === quiz.value.questions.length - 1
})

const submitAnswer = () => {
  if (selectedAnswerIndex.value === null || !currentQuestion.value) return

  isCorrect.value = selectedAnswerIndex.value === currentQuestion.value.correctAnswerIndex
  if (isCorrect.value) {
    score.value++
  }
  showResult.value = true
}

const nextQuestion = () => {
  showResult.value = false
  selectedAnswerIndex.value = null
  if (!isLastQuestion.value) {
    currentQuestionIndex.value++
  } else {
    // Finish quiz
    alert(`Quiz terminé ! Score : ${score.value} / ${quiz.value?.questions.length}`)
    router.push('/')
  }
}

onMounted(() => {
  if (!quiz.value) {
    // If no quiz is found in store (e.g. page refresh), go back home
    router.push('/')
  }
})
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex flex-col items-center justify-center p-4" v-if="quiz && currentQuestion">
    <div class="max-w-2xl w-full bg-white rounded-xl shadow-lg p-8">
      <div class="mb-6 flex justify-between items-center">
        <h2 class="text-2xl font-bold text-gray-800">{{ quiz.title }}</h2>
        <span class="text-gray-500">Question {{ currentQuestionIndex + 1 }} / {{ quiz.questions.length }}</span>
      </div>

      <div class="mb-8">
        <h3 class="text-xl font-medium text-gray-900 mb-4">{{ currentQuestion.text }}</h3>

        <div class="space-y-3">
          <button
            v-for="(option, index) in currentQuestion.options"
            :key="index"
            @click="!showResult && (selectedAnswerIndex = index)"
            :class="[
              'w-full text-left p-4 rounded-lg border-2 transition-all',
              showResult
                ? (index === currentQuestion.correctAnswerIndex
                    ? 'bg-green-100 border-green-500 text-green-800'
                    : (index === selectedAnswerIndex ? 'bg-red-100 border-red-500 text-red-800' : 'bg-gray-50 border-gray-200'))
                : (selectedAnswerIndex === index
                    ? 'bg-indigo-50 border-indigo-500 text-indigo-700'
                    : 'bg-white border-gray-200 hover:border-indigo-300')
            ]"
            :disabled="showResult"
          >
            {{ option }}
          </button>
        </div>
      </div>

      <div class="flex justify-end">
        <button
          v-if="!showResult"
          @click="submitAnswer"
          :disabled="selectedAnswerIndex === null"
          class="px-6 py-3 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
        >
          Valider
        </button>
        <button
          v-else
          @click="nextQuestion"
          class="px-6 py-3 bg-indigo-600 text-white rounded-lg font-bold hover:bg-indigo-700 transition"
        >
          {{ isLastQuestion ? 'Terminer' : 'Question Suivante' }}
        </button>
      </div>

      <div v-if="showResult" class="mt-4 p-4 rounded-lg text-center font-bold" :class="isCorrect ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
        {{ isCorrect ? 'Correct ! 🎉' : 'Incorrect 😔' }}
      </div>
    </div>
  </div>
</template>
