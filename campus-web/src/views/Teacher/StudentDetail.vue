<script setup>
import { useRoute, useRouter } from 'vue-router';
import { ChevronLeft, MapPin, Clock, BookOpen } from 'lucide-vue-next';

const route = useRoute();
const router = useRouter();

// 模拟学生详情数据 (实际应根据 route.params.id 获取)
const student = {
  id: route.params.id,
  grade: '小学三年级',
  subject: '数学',
  price: 150,
  location: '阳光小区 (距您1.5km)',
  tags: ['基础薄弱', '需耐心', '目标提分'],
  desc: '孩子目前数学成绩在60分左右，主要问题是计算粗心，应用题理解困难。希望能找一位有耐心的老师，每周辅导两次作业和预习。',
  schedule: '周六上午、周日晚上'
};

const handleContact = () => {
  alert('已向家长发送沟通请求！');
};
</script>

<template>
  <div class="min-h-screen bg-white pb-safe">
    <div class="p-4 border-b flex items-center sticky top-0 bg-white z-10">
      <button @click="router.back()" class="p-1 hover:bg-gray-100 rounded-full mr-2">
        <ChevronLeft />
      </button>
      <h1 class="font-bold text-lg">需求详情</h1>
    </div>

    <div class="p-6">
      <div class="flex justify-between items-start mb-4">
        <h2 class="text-2xl font-bold">{{ student.grade }} · {{ student.subject }}</h2>
        <span class="text-red-500 font-bold text-xl">¥{{ student.price }}</span>
      </div>

      <div class="flex flex-wrap gap-2 mb-6">
        <span v-for="tag in student.tags" :key="tag" class="bg-orange-50 text-orange-600 px-3 py-1 rounded-full text-sm">
          {{ tag }}
        </span>
      </div>

      <div class="bg-gray-50 rounded-xl p-4 space-y-3 mb-6">
        <div class="flex items-center gap-3 text-gray-600">
          <MapPin :size="18" /> {{ student.location }}
        </div>
        <div class="flex items-center gap-3 text-gray-600">
          <Clock :size="18" /> {{ student.schedule }}
        </div>
        <div class="flex items-center gap-3 text-gray-600">
          <BookOpen :size="18" /> 需要试课
        </div>
      </div>

      <div class="mb-8">
        <h3 class="font-bold text-lg mb-2">家长描述</h3>
        <p class="text-gray-600 leading-relaxed">{{ student.desc }}</p>
      </div>
    </div>

    <div class="fixed bottom-0 w-full p-4 border-t bg-white">
      <button @click="handleContact" class="w-full bg-blue-600 text-white py-3 rounded-xl font-bold shadow-lg active:scale-95 transition-transform">
        立即沟通 / 申请接单
      </button>
    </div>
  </div>
</template>