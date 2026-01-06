<script setup>
import { useRouter } from 'vue-router';
import { computed } from 'vue';
import { store } from '../../store.js';
import { 
  Wallet, Clock, ChevronRight, FileText, 
  Settings, LogOut, BookOpen, ShoppingBag 
} from 'lucide-vue-next';

const router = useRouter();

// 获取当前身份（使用 computed 保持响应式）
const isTeacher = computed(() => store.userRole === 'teacher');

// 用户信息（使用 computed 保持响应式）
const user = computed(() => {
  const userInfo = store.userInfo;
  const isT = isTeacher.value;
  return {
    name: userInfo?.nickname || (isT ? '老师' : '家长'),
    id: userInfo?.userId ? (isT ? `T-${userInfo.userId}` : `P-${userInfo.userId}`) : (isT ? 'T-0000' : 'P-0000'),
    avatar: userInfo?.avatar || `https://api.dicebear.com/7.x/${isT ? 'miniavs' : 'adventurer'}/svg?seed=${store.userRole}`,
    balance: isT ? 450 : 0,
    label: isT ? '认证教师' : 'VIP家长'
  };
});

const handleLogout = () => {
  store.logout();
  router.push('/login');
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-24 font-sans">
    
    <div class="pt-10 pb-20 px-6 rounded-b-[40px] shadow-lg transition-colors duration-500"
         :class="isTeacher ? 'bg-gradient-to-br from-green-500 to-teal-600' : 'bg-gradient-to-br from-brand-blue to-blue-600'">
      
      <div class="flex items-center gap-4 text-white">
        <img :src="user.avatar" class="w-16 h-16 rounded-full border-4 border-white/20 bg-white/10" />
        <div>
          <h2 class="text-xl font-bold flex items-center gap-2">
            {{ user.name }}
            <span class="text-[10px] bg-white/20 px-2 py-0.5 rounded-full border border-white/30">
              {{ user.label }}
            </span>
          </h2>
          <p class="text-white/70 text-xs font-mono mt-1">ID: {{ user.id }}</p>
        </div>
      </div>
    </div>

    <div class="mx-4 -mt-12 bg-white rounded-xl shadow-md p-4 flex justify-around text-center mb-6 relative z-10">
      <div>
        <div class="text-xl font-bold text-gray-800">{{ user.balance }}</div>
        <div class="text-xs text-gray-400">{{ isTeacher ? '可提现(元)' : '托管资金' }}</div>
      </div>
      <div class="w-[1px] bg-gray-100"></div>
      <div>
        <div class="text-xl font-bold text-gray-800">12</div>
        <div class="text-xs text-gray-400">剩余课时</div>
      </div>
      <div class="w-[1px] bg-gray-100"></div>
      <div>
        <div class="text-xl font-bold text-gray-800">3</div>
        <div class="text-xs text-gray-400">待办事项</div>
      </div>
    </div>

    <div class="px-4 space-y-3">
      
      <div @click="router.push('/mine/orders')" 
           class="bg-white p-4 rounded-xl shadow-sm flex items-center justify-between cursor-pointer active:scale-[0.98]">
        <div class="flex items-center gap-3">
          <div class="bg-orange-100 p-2 rounded-lg text-brand-orange">
            <ShoppingBag :size="20" />
          </div>
          <span class="font-bold text-gray-700">我的订单 / 合同</span>
        </div>
        <ChevronRight :size="16" class="text-gray-300" />
      </div>

      <template v-if="isTeacher">
        <div @click="router.push('/teacher/resume')" 
             class="bg-white p-4 rounded-xl shadow-sm flex items-center justify-between cursor-pointer active:scale-[0.98]">
          <div class="flex items-center gap-3">
            <div class="bg-green-100 p-2 rounded-lg text-green-600">
              <FileText :size="20" />
            </div>
            <span class="font-bold text-gray-700">简历与资质管理</span>
          </div>
          <ChevronRight :size="16" class="text-gray-300" />
        </div>
      </template>

      <template v-else>
        <div @click="router.push('/parent/wrong-book')" 
             class="bg-white p-4 rounded-xl shadow-sm flex items-center justify-between cursor-pointer active:scale-[0.98]">
          <div class="flex items-center gap-3">
            <div class="bg-purple-100 p-2 rounded-lg text-purple-600">
              <BookOpen :size="20" />
            </div>
            <span class="font-bold text-gray-700">智能错题本 (AI搜题)</span>
          </div>
          <ChevronRight :size="16" class="text-gray-300" />
        </div>
      </template>

      <div @click="router.push('/process/record')" 
           class="bg-white p-4 rounded-xl shadow-sm flex items-center justify-between cursor-pointer active:scale-[0.98]">
        <div class="flex items-center gap-3">
          <div class="bg-blue-100 p-2 rounded-lg text-brand-blue">
            <Clock :size="20" />
          </div>
          <span class="font-bold text-gray-700">课时记录表</span>
        </div>
        <ChevronRight :size="16" class="text-gray-300" />
      </div>

      <button @click="handleLogout" class="w-full bg-white p-4 rounded-xl shadow-sm flex items-center justify-center gap-2 text-red-500 font-bold mt-6">
        <LogOut :size="18" /> 退出登录
      </button>

    </div>
  </div>
</template>