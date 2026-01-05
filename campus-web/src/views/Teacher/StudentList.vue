<script setup>
import { useRouter } from 'vue-router';
import { MapPin, BookOpen, Clock, ChevronRight } from 'lucide-vue-next';

const router = useRouter();

// 模拟学生需求列表 (对应文档 FR2.1 家长发布的数据)
const students = [
  {
    id: 1,
    grade: '小学三年级',
    subject: '数学',
    price: 150,
    location: '阳光小区 (距您 1.5km)',
    tags: ['基础薄弱', '需耐心'],
    desc: '孩子数学基础较弱，计算容易出错，希望能找有耐心的老师辅导作业。',
    time: '周六上午'
  },
  {
    id: 2,
    grade: '初中二年级',
    subject: '物理',
    price: 200,
    location: '万达广场 (距您 3.0km)',
    tags: ['目标提分', '严厉型'],
    desc: '期中考试想提升20分，希望老师严格一点，主攻力学部分。',
    time: '周日晚上'
  },
  {
    id: 3,
    grade: '高中一年级',
    subject: '英语口语',
    price: 250,
    location: '线上教学',
    tags: ['留学准备', '全英教学'],
    desc: '准备出国，需要练习口语对话，希望老师有雅思教学经验。',
    time: '工作日晚上'
  },
  {
    id: 4,
    grade: '小学六年级',
    subject: '全科辅导',
    price: 180,
    location: '幸福家园 (距您 0.8km)',
    tags: ['小升初', '作业辅导'],
    desc: '针对小升初考试进行全科复习梳理，查漏补缺。',
    time: '周末全天'
  }
];

// 跳转到详情页
const goToDetail = (id) => {
  router.push(`/student/${id}`);
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-24">
    <div class="bg-white p-4 sticky top-0 border-b z-10 shadow-sm">
      <h1 class="text-center font-bold text-lg text-gray-800">最新家教需求 (找学生)</h1>
    </div>

    <div class="p-4 space-y-4">
      <div v-for="item in students" :key="item.id" 
           @click="goToDetail(item.id)"
           class="bg-white p-4 rounded-xl shadow-[0_2px_8px_rgba(0,0,0,0.04)] border border-gray-100 cursor-pointer active:scale-[0.98] transition-all hover:shadow-md hover:border-blue-200">
        
        <div class="flex justify-between items-start mb-2">
          <h3 class="font-bold text-lg text-gray-800 flex items-center gap-2">
            {{ item.grade }} · {{ item.subject }}
          </h3>
          <span class="text-red-500 font-bold text-lg">¥{{ item.price }}<span class="text-xs text-gray-400 font-normal">/h</span></span>
        </div>

        <div class="flex flex-wrap gap-2 mb-3">
          <span v-for="tag in item.tags" :key="tag" 
                class="px-2 py-0.5 bg-blue-50 text-blue-600 text-xs rounded border border-blue-100">
            {{ tag }}
          </span>
        </div>

        <div class="space-y-2 text-sm text-gray-500 mb-3 bg-gray-50 p-3 rounded-lg">
          <div class="flex items-center gap-2">
            <MapPin :size="14" class="text-gray-400"/> {{ item.location }}
          </div>
          <div class="flex items-center gap-2">
            <Clock :size="14" class="text-gray-400"/> {{ item.time }}
          </div>
          <div class="flex items-start gap-2">
            <BookOpen :size="14" class="text-gray-400 mt-0.5 min-w-[14px]"/> 
            <span class="line-clamp-1">{{ item.desc }}</span>
          </div>
        </div>

        <div class="flex justify-between items-center pt-2 border-t border-gray-100">
          <span class="text-xs text-gray-400">刚刚发布</span>
          <div class="flex items-center text-blue-600 text-sm font-bold gap-1">
            查看详情 <ChevronRight :size="16" />
          </div>
        </div>

      </div>
    </div>
  </div>
</template>