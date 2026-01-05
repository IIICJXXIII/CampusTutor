<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ChevronLeft, Camera, Plus, Tag, Search } from 'lucide-vue-next';

const router = useRouter();
const isUploading = ref(false);

// 模拟错题数据 (对应文档 wrong_questions 表)
const questions = ref([
  {
    id: 1,
    subject: '数学',
    img: 'https://api.dicebear.com/7.x/shapes/svg?seed=math1', // 模拟题目图
    tags: ['二次函数', '抛物线'],
    date: '3月1日',
    mastered: false
  },
  {
    id: 2,
    subject: '英语',
    img: 'https://api.dicebear.com/7.x/shapes/svg?seed=eng1',
    tags: ['虚拟语气', '语法填空'],
    date: '2月28日',
    mastered: true
  }
]);

// 模拟拍照上传+OCR
const handleUpload = () => {
  isUploading.value = true;
  // 模拟延时
  setTimeout(() => {
    questions.value.unshift({
      id: Date.now(),
      subject: '物理',
      img: 'https://api.dicebear.com/7.x/shapes/svg?seed=phy' + Date.now(),
      tags: ['力学', 'OCR自动识别'], // 演示亮点
      date: '刚刚',
      mastered: false
    });
    isUploading.value = false;
    alert('OCR识别完成！已自动归类标签。');
  }, 1500);
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-safe font-sans">
    
    <div class="bg-white p-4 sticky top-0 z-10 border-b flex items-center justify-between">
      <button @click="router.back()" class="p-1 hover:bg-gray-100 rounded-full">
        <ChevronLeft />
      </button>
      <h1 class="font-bold text-lg">智能错题本</h1>
      <button class="p-1 text-gray-400"><Search :size="20" /></button>
    </div>

    <button @click="handleUpload" 
            class="fixed bottom-8 right-6 w-14 h-14 bg-brand-blue text-white rounded-full shadow-xl flex items-center justify-center z-20 active:scale-90 transition-transform">
      <Camera v-if="!isUploading" :size="24" />
      <div v-else class="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
    </button>

    <div class="px-4 py-3 flex gap-2 overflow-x-auto no-scrollbar">
      <span class="px-4 py-1.5 bg-brand-blue text-white text-xs font-bold rounded-full whitespace-nowrap">全部</span>
      <span v-for="s in ['数学', '英语', '物理', '化学']" :key="s" class="px-4 py-1.5 bg-white text-gray-600 text-xs font-bold rounded-full whitespace-nowrap shadow-sm">
        {{ s }}
      </span>
    </div>

    <div class="p-4 grid grid-cols-2 gap-3">
      <div v-for="q in questions" :key="q.id" class="bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100 group">
        <div class="h-32 bg-gray-100 relative overflow-hidden">
          <img :src="q.img" class="w-full h-full object-cover opacity-80 group-hover:scale-105 transition-transform" />
          <div v-if="q.mastered" class="absolute inset-0 bg-green-500/20 flex items-center justify-center">
            <span class="bg-green-600 text-white text-xs px-2 py-1 rounded font-bold">已掌握</span>
          </div>
        </div>
        
        <div class="p-3">
          <div class="flex justify-between items-center mb-2">
            <span class="font-bold text-gray-800 text-sm">{{ q.subject }}</span>
            <span class="text-xs text-gray-400">{{ q.date }}</span>
          </div>
          <div class="flex flex-wrap gap-1">
            <span v-for="t in q.tags" :key="t" class="px-1.5 py-0.5 bg-gray-50 text-gray-500 text-[10px] rounded border border-gray-200">
              #{{ t }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="questions.length < 3" class="text-center text-xs text-gray-400 mt-4">
      点击右下角相机，体验 AI 搜题功能
    </div>

  </div>
</template>