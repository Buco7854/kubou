<script setup lang="ts">
</script>

<template>
  <div class="text-center">
    <h1 class="text-4xl font-bold text-gray-900 mb-4">Welcome to Kubou</h1>
    <p class="text-xl text-gray-600 mb-8">The ultimate quiz platform for learning and fun.</p>
    <div class="space-x-4">
      <router-link to="/create-quiz" class="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700">
        Create a Quiz
      </router-link>
      <router-link to="/login" class="inline-flex items-center px-6 py-3 border border-gray-300 shadow-sm text-base font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50">
        Join a Game
      </router-link>
    </div>
  </div>
</template>
