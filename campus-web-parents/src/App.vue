<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from '@shared/stores'

const userStore = useUserStore()

// 检查登录状态
const token = localStorage.getItem('token')
if (token) {
  userStore.setToken(token)
}
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
