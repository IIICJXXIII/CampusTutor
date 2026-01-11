<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@shared/stores'

const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  // 检查登录状态
  if (!userStore.isLoggedIn && router.currentRoute.value.meta.requiresAuth !== false) {
    router.push('/login')
  }
})
</script>

<style lang="scss">
// 教师端全局样式覆盖
</style>
