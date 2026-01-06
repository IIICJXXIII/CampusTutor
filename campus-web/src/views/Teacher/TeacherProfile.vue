<script setup>
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router'; // 用于获取参数和跳转
import { ChevronLeft, Star, Award, Clock, Calendar } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();

// 1. 模拟根据 ID 获取到的教师详细数据 (FR1.1)
const teacher = {
  id: route.params.id,
  name: '张老师',
  title: '北京师范大学 · 英语硕士',
  tags: ['实名认证', '英语专八', '3年教龄', '幽默风趣'],
  rating: 4.9,
  price: 200,
  // FR1.1 能力证明层
  intro: '擅长引导式教学，主攻初中英语语法与口语。曾帮助3名学生在期末考试中提分30+。',
  // FR1.2 可授课时间
  schedule: ['周六上午 09:00-11:00', '周日晚上 19:00-21:00'] 
};

// 控制“预约弹窗”显示 (FR2.3 试课预约)
const showModal = ref(false);

const handleBook = () => {
  showModal.value = false;
  // 跳转到预约签约页面，传递教师信息
  router.push({
    path: `/booking/${teacher.id}`,
    query: {
      teacherId: teacher.id,
      teacherName: teacher.name,
      subject: '英语', // 可从实际数据获取
      price: teacher.price
    }
  });
};
</script>

<template>
  <div class="min-h-screen bg-white pb-24">
    
    <div class="sticky top-0 bg-white p-4 flex items-center border-b z-10">
      <button @click="router.back()" class="p-1 hover:bg-gray-100 rounded-full">
        <ChevronLeft />
      </button>
      <span class="ml-4 font-bold text-lg">教师详情</span>
    </div>

    <div class="p-6">
      <h1 class="text-2xl font-bold mb-2">{{ teacher.name }}</h1>
      <p class="text-gray-500 mb-4">{{ teacher.title }}</p>
      
      <div class="flex flex-wrap gap-2 mb-6">
        <span v-for="tag in teacher.tags" :key="tag" 
              class="px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-xs font-medium">
          {{ tag }}
        </span>
      </div>

      <div class="flex justify-between bg-gray-50 p-4 rounded-xl mb-6">
        <div class="text-center">
          <div class="font-bold text-lg flex justify-center items-center gap-1">
            4.9 <Star :size="14" class="fill-yellow-400 text-yellow-400"/>
          </div>
          <div class="text-xs text-gray-400">评分</div>
        </div>
        <div class="text-center">
          <div class="font-bold text-lg">98%</div>
          <div class="text-xs text-gray-400">续课率</div>
        </div>
        <div class="text-center">
          <div class="font-bold text-lg">300+</div>
          <div class="text-xs text-gray-400">授课时</div>
        </div>
      </div>

      <div class="mb-6">
        <h3 class="font-bold text-lg mb-2 flex items-center gap-2">
          <Award :size="20" class="text-blue-500"/> 教学经历
        </h3>
        <p class="text-gray-600 text-sm leading-relaxed">{{ teacher.intro }}</p>
      </div>

      <div class="mb-6">
        <h3 class="font-bold text-lg mb-2 flex items-center gap-2">
          <Clock :size="20" class="text-green-500"/> 可约时间
        </h3>
        <div class="space-y-2">
          <div v-for="time in teacher.schedule" :key="time" 
               class="flex items-center gap-2 text-sm text-gray-600 bg-gray-50 p-2 rounded">
            <Calendar :size="16" /> {{ time }}
          </div>
        </div>
      </div>
    </div>

    <div class="fixed bottom-0 left-0 w-full bg-white border-t p-4 flex items-center justify-between shadow-lg">
      <div>
        <span class="text-red-500 font-bold text-2xl">¥{{ teacher.price }}</span>
        <span class="text-gray-400 text-xs">/小时</span>
      </div>
      <button @click="showModal = true" 
              class="bg-blue-600 text-white px-8 py-3 rounded-xl font-bold hover:bg-blue-700 active:scale-95 transition-all">
        立即预约试听
      </button>
    </div>

    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-end sm:items-center justify-center z-50">
      <div class="bg-white w-full sm:w-80 p-6 rounded-t-2xl sm:rounded-2xl animate-slide-up">
        <h3 class="text-xl font-bold mb-4">确认预约信息</h3>
        <div class="space-y-3 mb-6">
          <div class="flex justify-between text-sm"><span>学生:</span> <span class="font-bold">王小明 (三年级)</span></div>
          <div class="flex justify-between text-sm"><span>科目:</span> <span class="font-bold">英语</span></div>
          <div class="flex justify-between text-sm"><span>时段:</span> <span class="text-blue-600">周六上午 09:00</span></div>
        </div>
        <button @click="handleBook" class="w-full bg-blue-600 text-white py-3 rounded-lg font-bold">确认发送请求</button>
        <button @click="showModal = false" class="w-full mt-2 py-3 text-gray-500 text-sm">取消</button>
      </div>
    </div>

  </div>
</template>

<style>
/* 简单的弹窗动画 */
@keyframes slide-up {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.animate-slide-up {
  animation: slide-up 0.3s ease-out;
}
</style>