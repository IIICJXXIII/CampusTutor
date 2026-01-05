<script setup>
import { useRouter } from 'vue-router';
import { GraduationCap, Users } from 'lucide-vue-next';

const router = useRouter();

// 模拟登录逻辑：根据选择的角色，跳转到不同页面
const loginAs = (role) => {
  // 1. 把身份存一下，方便后面 App.vue 判断显示哪个菜单
  localStorage.setItem('userRole', role);
  
  // 2. 跳转
  if (role === 'parent') {
    router.push('/parent/demand'); // 家长 -> 去发需求
  } else {
    router.push('/teacher/students'); // 老师 -> 去找学生 (马上建这个页)
  }
};
</script>

<template>
  <div class="min-h-screen bg-gradient-to-b from-blue-500 to-blue-600 flex flex-col justify-center items-center p-6 text-white">
    <div class="mb-10 text-center">
      <h1 class="text-4xl font-bold mb-2">易家教平台</h1>
      <p class="opacity-80">连接好老师与好学生</p>
    </div>

    <div class="w-full max-w-sm space-y-4">
      <button @click="loginAs('parent')" 
              class="w-full bg-white text-blue-600 p-6 rounded-2xl shadow-lg flex items-center gap-4 active:scale-95 transition-transform">
        <div class="bg-blue-100 p-3 rounded-full">
          <Users :size="32" class="text-blue-600" />
        </div>
        <div class="text-left">
          <h3 class="text-xl font-bold">我是家长</h3>
          <p class="text-sm text-gray-400">我要找老师、发需求</p>
        </div>
      </button>

      <button @click="loginAs('teacher')" 
              class="w-full bg-white text-blue-600 p-6 rounded-2xl shadow-lg flex items-center gap-4 active:scale-95 transition-transform">
        <div class="bg-green-100 p-3 rounded-full">
          <GraduationCap :size="32" class="text-green-600" />
        </div>
        <div class="text-left">
          <h3 class="text-xl font-bold text-gray-800">我是老师</h3>
          <p class="text-sm text-gray-400">我要找学生、做家教</p>
        </div>
      </button>
    </div>
  </div>
</template>