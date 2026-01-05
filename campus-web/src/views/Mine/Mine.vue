<script setup>
import { useRouter } from 'vue-router';
import { Wallet, Clock, ChevronRight, FileText, Settings, LogOut } from 'lucide-vue-next';

const router = useRouter();

// 简单的身份判断
const userRole = localStorage.getItem('userRole') || 'parent';
const isTeacher = userRole === 'teacher';

const user = {
  name: isTeacher ? '张老师' : '王小明家长', // 根据身份显示不同名字
  avatar: `https://api.dicebear.com/7.x/${isTeacher ? 'miniavs' : 'adventurer'}/svg?seed=123`,
  balance: 1500,
  hours: 12
};

const handleLogout = () => {
  localStorage.removeItem('userRole'); // 清除身份
  router.push('/login'); // 回到登录页
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-24">
    <div class="bg-blue-600 p-6 pt-10 text-white rounded-b-3xl shadow-lg transition-all"
         :class="isTeacher ? 'bg-green-600' : 'bg-blue-600'"> <div class="flex items-center gap-4">
        <img :src="user.avatar" class="w-16 h-16 rounded-full border-2 border-white/50 bg-white" />
        <div>
          <h2 class="text-xl font-bold">{{ user.name }}</h2>
          <p class="text-white/80 text-sm">{{ isTeacher ? '认证教师' : '普通会员' }}</p>
        </div>
      </div>
      <div class="flex justify-between mt-8 px-4">
        <div class="text-center">
          <div class="text-2xl font-bold">{{ user.balance }}</div>
          <div class="text-xs opacity-80">余额</div>
        </div>
        <div class="w-[1px] bg-white/30 h-8 self-center"></div>
        <div class="text-center">
          <div class="text-2xl font-bold">{{ user.hours }}</div>
          <div class="text-xs opacity-80">课时</div>
        </div>
        <div class="w-[1px] bg-white/30 h-8 self-center"></div>
        <div class="text-center">
          <div class="text-2xl font-bold">5.0</div>
          <div class="text-xs opacity-80">评分</div>
        </div>
      </div>
    </div>

    <div class="p-4 space-y-3 -mt-4">
      
      <div v-if="isTeacher" @click="router.push('/teacher/resume')" 
           class="bg-white p-4 rounded-xl shadow-sm flex items-center justify-between cursor-pointer active:scale-[0.98]">
        <div class="flex items-center gap-3">
          <div class="bg-green-100 p-2 rounded-lg"><FileText :size="20" class="text-green-600" /></div>
          <span class="font-medium text-gray-700">编辑个人简历 / 资质</span>
        </div>
        <ChevronRight :size="16" class="text-gray-300" />
      </div>

      <div v-else class="bg-white p-4 rounded-xl shadow-sm flex items-center justify-between cursor-pointer active:scale-[0.98]">
        <div class="flex items-center gap-3">
          <div class="bg-orange-100 p-2 rounded-lg"><Wallet :size="20" class="text-orange-600" /></div>
          <span class="font-medium text-gray-700">我的钱包</span>
        </div>
        <ChevronRight :size="16" class="text-gray-300" />
      </div>

      <div @click="router.push('/process/record')" class="bg-white p-4 rounded-xl shadow-sm flex items-center justify-between cursor-pointer active:scale-[0.98]">
        <div class="flex items-center gap-3">
          <div class="bg-blue-100 p-2 rounded-lg"><Clock :size="20" class="text-blue-600" /></div>
          <span class="font-medium text-gray-700">课时记录</span>
        </div>
        <ChevronRight :size="16" class="text-gray-300" />
      </div>

      <button @click="handleLogout" class="w-full bg-white p-4 rounded-xl shadow-sm flex items-center justify-center gap-2 text-red-500 font-medium mt-6">
        <LogOut :size="18" /> 退出登录 / 切换身份
      </button>

    </div>
  </div>
</template>