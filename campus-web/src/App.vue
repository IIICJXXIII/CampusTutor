<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'

const route = useRoute()

// 不使用布局的页面
const noLayoutPaths = ['/login', '/register']
const useLayout = computed(() => !noLayoutPaths.includes(route.path))
</script>

<template>
  <el-config-provider :locale="zhCn">
    <MainLayout v-if="useLayout">
      <router-view />
    </MainLayout>
    <router-view v-else />
  </el-config-provider>
</template>

<script>
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
export default {
  data() {
    return { zhCn }
  }
}
</script>