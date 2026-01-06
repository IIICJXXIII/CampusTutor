<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { sendCode, register } from '../api/auth.js';
import { store } from '../store.js';
import { ArrowRight, Loader2 } from 'lucide-vue-next';

const router = useRouter();

const phone = ref('');
const password = ref('');
const code = ref('');
const nickname = ref('');
const role = ref(2); // 2-家长,1-教师
const loading = ref(false);
const sending = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

const handleSendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    errorMsg.value = '请填写正确的手机号';
    return;
  }
  errorMsg.value = '';
  sending.value = true;
  try {
    await sendCode(phone.value);
    successMsg.value = '验证码已发送';
  } catch (e) {
    errorMsg.value = e.message || '验证码发送失败';
  } finally {
    sending.value = false;
  }
};

const handleRegister = async () => {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    errorMsg.value = '请填写正确的手机号';
    return;
  }
  if (!password.value) {
    errorMsg.value = '请填写密码';
    return;
  }
  if (!code.value) {
    errorMsg.value = '请填写验证码';
    return;
  }
  loading.value = true;
  errorMsg.value = '';
  successMsg.value = '';
  try {
    const res = await register({
      phone: phone.value,
      password: password.value,
      code: code.value,
      role: role.value,
      nickname: nickname.value || undefined
    });
    store.setLoginInfo(res.data);
    successMsg.value = '注册成功，正在跳转...';
    if (res.data.role === 1) {
      router.push('/teacher/auth');
    } else {
      router.push('/parent/demand');
    }
  } catch (e) {
    errorMsg.value = e.message || '注册失败，请重试';
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="min-h-screen bg-white flex flex-col font-sans">
    <div class="h-56 bg-brand-blue rounded-b-[32px] flex flex-col items-center justify-center text-white relative overflow-hidden">
      <div class="absolute top-0 right-0 w-56 h-56 bg-white/10 rounded-full -mr-12 -mt-12"></div>
      <div class="z-10 text-center">
        <h1 class="text-2xl font-bold tracking-widest">注册账号</h1>
        <p class="text-blue-100 text-xs mt-1">成为家长或教师，开启智能匹配</p>
      </div>
    </div>

    <div class="flex-1 px-8 pt-8 pb-12 space-y-6">
      <div class="space-y-1">
        <label class="text-xs font-bold text-gray-400">手机号</label>
        <div class="border-b border-gray-200 py-2">
          <input v-model="phone" type="tel" class="w-full outline-none text-lg font-bold text-gray-800 placeholder-gray-300" placeholder="请输入手机号" />
        </div>
      </div>

      <div class="space-y-1">
        <label class="text-xs font-bold text-gray-400">密码</label>
        <div class="border-b border-gray-200 py-2">
          <input v-model="password" type="password" class="w-full outline-none text-lg font-bold text-gray-800 placeholder-gray-300" placeholder="至少6位字母或数字" />
        </div>
      </div>

      <div class="space-y-1">
        <label class="text-xs font-bold text-gray-400">验证码</label>
        <div class="flex items-center gap-3 border-b border-gray-200 py-2">
          <input v-model="code" type="text" class="flex-1 outline-none text-lg font-bold text-gray-800 placeholder-gray-300" placeholder="请输入短信验证码" />
          <button @click="handleSendCode" :disabled="sending" class="text-brand-blue text-sm font-bold disabled:opacity-50">
            <Loader2 v-if="sending" :size="16" class="animate-spin inline" />
            <span v-else>获取验证码</span>
          </button>
        </div>
      </div>

      <div class="space-y-1">
        <label class="text-xs font-bold text-gray-400">昵称 (可选)</label>
        <div class="border-b border-gray-200 py-2">
          <input v-model="nickname" type="text" class="w-full outline-none text-lg font-bold text-gray-800 placeholder-gray-300" placeholder="方便家长或老师识别" />
        </div>
      </div>

      <div class="space-y-2">
        <label class="text-xs font-bold text-gray-400">身份</label>
        <div class="flex gap-3">
          <button @click="role = 2" :class="role === 2 ? 'bg-brand-blue text-white' : 'bg-gray-100 text-gray-600'" class="px-4 py-2 rounded-lg font-bold flex-1">家长</button>
          <button @click="role = 1" :class="role === 1 ? 'bg-brand-blue text-white' : 'bg-gray-100 text-gray-600'" class="px-4 py-2 rounded-lg font-bold flex-1">教师</button>
        </div>
      </div>

      <div v-if="errorMsg" class="mt-2 p-3 bg-red-50 text-red-600 text-sm rounded-lg">{{ errorMsg }}</div>
      <div v-if="successMsg" class="mt-2 p-3 bg-green-50 text-green-600 text-sm rounded-lg">{{ successMsg }}</div>

      <button @click="handleRegister" :disabled="loading" class="w-full bg-brand-blue text-white py-4 rounded-xl font-bold shadow-lg shadow-blue-200 mt-2 active:scale-95 transition-transform flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed">
        <Loader2 v-if="loading" :size="18" class="animate-spin" />
        <template v-else>立即注册 <ArrowRight :size="18" /></template>
      </button>

      <div class="text-center text-sm text-gray-500">
        已有账号？
        <span class="text-brand-blue font-bold cursor-pointer" @click="router.push('/login')">去登录</span>
      </div>
    </div>
  </div>
</template>
