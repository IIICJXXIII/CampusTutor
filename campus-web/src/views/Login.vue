<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../store.js';
import { login } from '../api/auth.js';
import { GraduationCap, ArrowRight, Eye, EyeOff, Loader2 } from 'lucide-vue-next';

const router = useRouter();

// 表单数据
const phone = ref('13800000001');
const password = ref('123456');
const showPassword = ref(false);
const isAgreed = ref(true);
const loading = ref(false);
const errorMsg = ref('');

// 登录逻辑
const handleLogin = async () => {
  if (!isAgreed.value) {
    errorMsg.value = '请先同意用户协议';
    return;
  }
  
  if (!phone.value || !password.value) {
    errorMsg.value = '请输入手机号和密码';
    return;
  }
  
  loading.value = true;
  errorMsg.value = '';
  
  try {
    // 调用后端登录接口
    const res = await login({
      phone: phone.value,
      password: password.value,
      loginType: 'password'
    });
    
    // 保存登录信息
    store.setLoginInfo(res.data);
    
    // 根据角色跳转
    const role = res.data.role;
    if (role === 2) {
      // 家长
      router.push('/parent/demand');
    } else if (role === 1) {
      // 教员 - 检查认证状态后跳转
      store.setCertification(true); // 后续可从接口获取认证状态
      router.push('/teacher/students');
    } else {
      // 管理员或其他
      router.push('/');
    }
  } catch (error) {
    console.error('登录失败:', error);
    errorMsg.value = error.message || '登录失败，请检查账号密码';
  } finally {
    loading.value = false;
  }
};

// 演示模式快捷登录 (当后端未启动时使用)
const demoLogin = (demoPhone) => {
  phone.value = demoPhone;
  
  // 模拟登录逻辑
  if (demoPhone === '13800000001') {
    store.setRole('parent');
    router.push('/parent/demand');
  } else if (demoPhone === '13800000002') {
    store.setRole('teacher');
    store.setCertification(true);
    router.push('/teacher/students');
  } else if (demoPhone === '13800000003') {
    store.setRole('teacher');
    store.setCertification(false);
    router.push('/teacher/auth');
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

      <!-- 错误提示 -->
      <div v-if="errorMsg" class="mt-4 p-3 bg-red-50 text-red-600 text-sm rounded-lg">
        {{ errorMsg }}
      </div>

      <button @click="handleLogin" 
              :disabled="loading"
              class="w-full bg-brand-blue text-white py-4 rounded-xl font-bold shadow-lg shadow-blue-200 mt-6 active:scale-95 transition-transform flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed">
        <Loader2 v-if="loading" :size="18" class="animate-spin" />
        <template v-else>立即登录 <ArrowRight :size="18" /></template>
      </button>

      <div class="mt-4 flex items-center justify-center gap-2">
        <input type="checkbox" v-model="isAgreed" class="w-4 h-4 accent-brand-blue rounded" />
        <span class="text-xs text-gray-400">
          我已阅读并同意 <span class="text-brand-blue">《用户协议》</span>
        </span>
      </div>

      <div class="mt-8 p-4 bg-gray-50 rounded-xl text-xs text-gray-500 space-y-2">
        <p class="font-bold mb-2 text-gray-800">🛠 演示账号速查 (点击快速填入)：</p>
        <p @click="demoLogin('13800000001')" class="cursor-pointer hover:bg-gray-100 p-1 rounded">
          <span class="text-brand-blue font-bold">13800000001</span> : 家长端 (默认)
        </p>
        <p @click="demoLogin('13800000002')" class="cursor-pointer hover:bg-gray-100 p-1 rounded">
          <span class="text-brand-orange font-bold">13800000002</span> : 教师端 (成熟)
        </p>
        <p @click="demoLogin('13800000003')" class="cursor-pointer hover:bg-gray-100 p-1 rounded">
          <span class="text-gray-600 font-bold">13800000003</span> : 教师端 (去认证)
        </p>
      </div>
    </div>

  </div>
</template>