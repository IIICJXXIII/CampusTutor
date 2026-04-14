<template>
  <router-view />
</template>

<script setup>
import { watchEffect } from 'vue'
import { useUserStore } from '@shared/stores'

const userStore = useUserStore()

// 恢复登录状态
const token = localStorage.getItem('token')
if (token) {
  userStore.setToken(token)
}

// 根据角色动态切换主题
watchEffect(() => {
  const theme = userStore.userRole === 'tutor' ? 'teacher' : 'parent'
  document.documentElement.setAttribute('data-theme', theme)
})
</script>

<style>
#app {
  min-height: 100vh;
}
</style>