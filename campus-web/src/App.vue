<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import { store } from './store.js'; // 引入 store
import { Search, FileEdit, User, ShieldCheck, FileText } from 'lucide-vue-next'; // 引入新图标

const route = useRoute();

// 白名单路由（不显示底部导航）
const hideNavPaths = ['/login', '/register'];

// 是否显示底部导航 (登录页、注册页不显示)
const showNav = computed(() => {
  // 未登录时不显示导航
  if (!store.isLoggedIn()) return false;
  // 白名单页面不显示导航
  return !hideNavPaths.includes(route.path);
});

// 当前角色
const currentRole = computed(() => store.userRole);
// 认证状态
const isCertified = computed(() => store.isCertified);
</script>

<template>
  <router-view />

  <div v-if="showNav" class="fixed bottom-0 left-0 w-full bg-white border-t border-gray-200 flex justify-around py-3 pb-safe shadow-[0_-5px_15px_rgba(0,0,0,0.05)] z-50">
    
    <template v-if="currentRole === 'teacher'">
      <router-link to="/teacher/students" class="flex flex-col items-center gap-1 text-[10px] transition-colors" 
        :class="route.path.includes('students') ? 'text-brand-blue font-bold' : 'text-gray-400'">
        <Search :size="24" />
        <span>找学生</span>
      </router-link>

      <router-link v-if="!isCertified" to="/teacher/auth" class="flex flex-col items-center gap-1 text-[10px] transition-colors"
        :class="route.path.includes('auth') ? 'text-brand-blue font-bold' : 'text-gray-400'">
        <ShieldCheck :size="24" />
        <span>资质认证</span>
      </router-link>

      <router-link v-else to="/teacher/resume" class="flex flex-col items-center gap-1 text-[10px] transition-colors"
        :class="route.path.includes('resume') ? 'text-brand-blue font-bold' : 'text-gray-400'">
        <FileText :size="24" />
        <span>我的简历</span>
      </router-link>
    </template>

    <template v-else>
      <router-link to="/parent/demand" class="flex flex-col items-center gap-1 text-[10px] transition-colors" 
        :class="route.path.includes('demand') ? 'text-brand-blue font-bold' : 'text-gray-400'">
        <FileEdit :size="24" />
        <span>发需求</span>
      </router-link>

      <router-link to="/teacher/list" class="flex flex-col items-center gap-1 text-[10px] transition-colors"
        :class="route.path.includes('teacher') ? 'text-brand-blue font-bold' : 'text-gray-400'">
        <Search :size="24" />
        <span>找老师</span>
      </router-link>
    </template>

    <router-link to="/mine" class="flex flex-col items-center gap-1 text-[10px] transition-colors"
      :class="route.path.includes('mine') ? 'text-brand-blue font-bold' : 'text-gray-400'">
      <User :size="24" />
      <span>我的</span>
    </router-link>

  </div>
</template>