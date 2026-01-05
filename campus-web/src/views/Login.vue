<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../store.js'; // 引入全局状态
import { GraduationCap, ArrowRight, Eye, EyeOff } from 'lucide-vue-next';

const router = useRouter();

// 表单数据
const phone = ref('13800000001'); // 默认填个家长号方便测试
const password = ref('123456');
const showPassword = ref(false);
const isAgreed = ref(true);

// 登录逻辑
const handleLogin = () => {
  if (!isAgreed.value) return alert('请先同意用户协议');

  // === 模拟后端验证逻辑 ===
  
  // 场景 1: 家长账号
  if (phone.value === '13800000001') {
    store.setRole('parent');
    router.push('/parent/demand'); // 去发需求/找老师
  } 
  // 场景 2: 成熟教师账号
  else if (phone.value === '13800000002') {
    store.setRole('teacher');
    router.push('/teacher/students'); // 去找学生
  }
  // 场景 3: 新注册教师 (未认证) -> 演示认证流程
  else if (phone.value === '13800000003') {
    store.setRole('teacher');
    alert('检测到您是新注册教师，请先完成资质认证。');
    router.push('/teacher/resume'); // 跳转到认证页
  }
  // 其他
  else {
    alert('演示账号说明：\n138...01 (家长)\n138...02 (成熟教师)\n138...03 (新教师-去认证)');
  }
};
</script>

<template>
  <div class="min-h-screen bg-white flex flex-col font-sans">
    
    <div class="h-64 bg-brand-blue rounded-b-[40px] flex flex-col items-center justify-center text-white relative overflow-hidden">
      <div class="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -mr-16 -mt-16"></div>
      <div class="z-10 text-center animate-fade-in">
        <div class="w-20 h-20 bg-white rounded-2xl mx-auto mb-4 flex items-center justify-center shadow-lg">
          <GraduationCap :size="40" class="text-brand-blue" />
        </div>
        <h1 class="text-2xl font-bold tracking-widest">易家教</h1>
        <p class="text-blue-100 text-xs mt-1">连接好老师与好学生</p>
      </div>
    </div>

    <div class="flex-1 px-8 pt-10">
      <h2 class="text-xl font-bold text-gray-800 mb-6">账号登录</h2>
      
      <div class="space-y-6">
        <div class="space-y-1">
          <label class="text-xs font-bold text-gray-400">手机号 / 账号</label>
          <div class="border-b border-gray-200 py-2">
            <input v-model="phone" type="tel" class="w-full outline-none text-lg font-bold text-gray-800 placeholder-gray-300" placeholder="请输入手机号" />
          </div>
        </div>

        <div class="space-y-1">
          <label class="text-xs font-bold text-gray-400">密码</label>
          <div class="border-b border-gray-200 py-2 flex items-center">
            <input :type="showPassword ? 'text' : 'password'" v-model="password" class="flex-1 outline-none text-lg font-bold text-gray-800 placeholder-gray-300" placeholder="请输入密码" />
            <button @click="showPassword = !showPassword" class="text-gray-400">
              <component :is="showPassword ? EyeOff : Eye" :size="20" />
            </button>
          </div>
        </div>
      </div>

      <button @click="handleLogin" 
              class="w-full bg-brand-blue text-white py-4 rounded-xl font-bold shadow-lg shadow-blue-200 mt-10 active:scale-95 transition-transform flex items-center justify-center gap-2">
        立即登录 <ArrowRight :size="18" />
      </button>

      <div class="mt-4 flex items-center justify-center gap-2">
        <input type="checkbox" v-model="isAgreed" class="w-4 h-4 accent-brand-blue rounded" />
        <span class="text-xs text-gray-400">
          我已阅读并同意 <span class="text-brand-blue">《用户协议》</span>
        </span>
      </div>

      <div class="mt-10 p-4 bg-gray-50 rounded-xl text-xs text-gray-500 space-y-1">
        <p class="font-bold mb-2 text-gray-800">🛠 演示账号速查：</p>
        <p><span class="text-brand-blue font-bold">13800000001</span> : 家长端 (默认)</p>
        <p><span class="text-brand-orange font-bold">13800000002</span> : 教师端 (成熟)</p>
        <p><span class="text-gray-600 font-bold">13800000003</span> : 教师端 (去认证)</p>
      </div>
    </div>

  </div>
</template>