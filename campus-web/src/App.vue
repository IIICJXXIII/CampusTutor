<script setup>
import { ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { FileText, Users, User, Search, FileEdit } from 'lucide-vue-next';

const route = useRoute();
const currentRole = ref('parent');
const showNav = ref(true);

watch(route, (newPath) => {
  showNav.value = newPath.path !== '/login';
  const savedRole = localStorage.getItem('userRole');
  if (savedRole) currentRole.value = savedRole;
}, { immediate: true });
</script>

<template>
  <router-view />

  <div v-if="showNav" class="fixed bottom-0 left-0 w-full bg-white border-t border-gray-200 flex justify-around py-3 pb-safe shadow-lg z-50">
    
    <template v-if="currentRole === 'teacher'">
      <router-link to="/teacher/students" class="flex flex-col items-center gap-1 text-xs transition-colors" 
        :class="route.path.includes('students') ? 'text-blue-600' : 'text-gray-400'">
        <Search :size="24" />
        <span>找学生</span>
      </router-link>

      <router-link to="/teacher/resume" class="flex flex-col items-center gap-1 text-xs transition-colors"
        :class="route.path.includes('resume') ? 'text-blue-600' : 'text-gray-400'">
        <FileEdit :size="24" />
        <span>发布信息</span>
      </router-link>
    </template>

    <template v-else>
      <router-link to="/parent/demand" class="flex flex-col items-center gap-1 text-xs transition-colors" 
        :class="route.path.includes('demand') ? 'text-blue-600' : 'text-gray-400'">
        <FileEdit :size="24" />
        <span>发需求</span>
      </router-link>

      <router-link to="/teacher/list" class="flex flex-col items-center gap-1 text-xs transition-colors"
        :class="route.path.includes('teacher') ? 'text-blue-600' : 'text-gray-400'">
        <Search :size="24" />
        <span>找老师</span>
      </router-link>
    </template>

    <router-link to="/mine" class="flex flex-col items-center gap-1 text-xs transition-colors"
      :class="route.path.includes('mine') ? 'text-blue-600' : 'text-gray-400'">
      <User :size="24" />
      <span>我的</span>
    </router-link>

  </div>
</template>