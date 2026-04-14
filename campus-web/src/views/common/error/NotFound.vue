<template>
  <div class="not-found">
    <el-empty description="页面不存在">
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
