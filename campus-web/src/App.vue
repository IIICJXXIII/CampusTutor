<template>
  <router-view />
</template>

<script setup>
import { watchEffect } from 'vue'
import { useUserStore } from '@shared/stores'

const userStore = useUserStore()

watchEffect(() => {
  const theme = userStore.userRole === 'tutor' ? 'teacher' : 'parent'
  document.documentElement.setAttribute('data-theme', theme)
})
</script>

<style>
#app {
  min-height: 100vh;
  min-height: -webkit-fill-available;
  min-height: 100dvh;
}
</style>
