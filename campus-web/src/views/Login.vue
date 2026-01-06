<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../store.js';
import { login } from '../api/auth.js';
import { getTutorProfile } from '../api/tutor.js';
import { GraduationCap, ArrowRight, Eye, EyeOff, Loader2 } from 'lucide-vue-next';

const router = useRouter();

// 表单数据
const account = ref('');
const password = ref('');
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
  if (!account.value || !password.value) {
    errorMsg.value = '请输入账号或手机号和密码';
    return;
  }
  loading.value = true;
  errorMsg.value = '';
  try {
    // 调用后端登录接口，兼容手机号/账号
    const res = await login({
      account: account.value,
      password: password.value,
      loginType: 'password'
    });
    store.setLoginInfo(res.data);
    // 根据角色跳转
    const role = res.data.role;
    if (role === 2) {
      // 家长端：跳转到发布需求页
      router.push('/parent/demand');
    } else if (role === 1) {
      // 教师端：检查认证状态决定跳转目标
      try {
        const profileRes = await getTutorProfile();
        // certStatus: 0-未认证, 1-待审核, 2-已认证
        const certStatus = profileRes.data?.certStatus;
        if (certStatus === 2) {
          store.setCertification(true);
          router.push('/teacher/students');
        } else {
          store.setCertification(false);
          router.push('/teacher/auth');
        }
      } catch (profileError) {
        // 获取档案失败（可能是新教师），跳转到认证页
        console.warn('获取教师档案失败，跳转到认证页:', profileError);
        store.setCertification(false);
        router.push('/teacher/auth');
      }
    } else {
      router.push('/mine');
    }
  } catch (error) {
    console.error('登录失败:', error);
    errorMsg.value = error.message || '登录失败，请检查账号或密码';
  } finally {
    loading.value = false;
  }
};

// ...如需保留 demoLogin，可仅在开发环境显示
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
            <input v-model="account" type="text" class="w-full outline-none text-lg font-bold text-gray-800 placeholder-gray-300" placeholder="请输入手机号或账号" />
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

      <div class="text-center text-sm text-gray-500 mt-6">
        还没有账号？
        <span class="text-brand-blue font-bold cursor-pointer" @click="router.push('/register')">去注册</span>
      </div>
    </div>

  </div>
</template>