<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { MapPin, List, Search, Map as MapIcon, ChevronRight, Navigation } from 'lucide-vue-next';

const router = useRouter();
const viewMode = ref('list'); // 'list' | 'map'

// 模拟学生数据 (带坐标位置)
const students = ref([
  {
    id: 1,
    grade: '小学三年级',
    subject: '数学',
    price: 180,
    tags: ['急需', '1.2km'],
    location: '阳光花园',
    desc: '孩子计算基础薄弱，需要耐心。',
    top: '30%', left: '40%' // 模拟地图相对位置
  },
  {
    id: 2,
    grade: '初中二年级',
    subject: '物理',
    price: 220,
    tags: ['考前冲刺', '3.5km'],
    location: '万达广场',
    desc: '期中考试物理不及格，急需提升。',
    top: '50%', left: '70%'
  },
  {
    id: 3,
    grade: '高中英语',
    subject: '口语',
    price: 250,
    tags: ['线上', '0km'],
    location: '线上教学',
    desc: '准备出国留学，重点练习雅思。',
    top: '60%', left: '20%'
  }
]);

const getTagClass = (tag) => {
  if (tag.includes('km')) return 'bg-gray-100 text-gray-600';
  return 'bg-blue-50 text-brand-blue border-blue-100';
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-24 font-sans flex flex-col">
    
    <div class="bg-white p-3 shadow-sm z-20">
      <div class="flex gap-3 mb-2">
        <div class="flex-1 bg-gray-100 rounded-full flex items-center px-4 py-2 text-gray-400 text-sm">
          <Search :size="16" class="mr-2" />
          <span>搜索生源...</span>
        </div>
        <button class="text-brand-blue font-bold text-sm flex items-center gap-1 bg-blue-50 px-3 rounded-full transition-colors active:scale-95"
                @click="viewMode = viewMode === 'list' ? 'map' : 'list'">
          <component :is="viewMode === 'list' ? MapIcon : List" :size="16" />
          {{ viewMode === 'list' ? '地图' : '列表' }}
        </button>
      </div>
    </div>

    <div v-if="viewMode === 'list'" class="p-3 space-y-3 animate-fade-in overflow-y-auto">
      <div v-for="item in students" :key="item.id" 
           @click="router.push(`/student/${item.id}`)"
           class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 active:scale-[0.98] transition-transform">
        
        <div class="flex justify-between items-start mb-2">
          <h3 class="font-bold text-gray-800 text-lg">{{ item.grade }} · {{ item.subject }}</h3>
          <span class="text-brand-orange font-bold text-lg">¥{{ item.price }}</span>
        </div>

        <div class="flex flex-wrap gap-2 mb-3">
          <span v-for="tag in item.tags" :key="tag" class="px-2 py-0.5 text-xs rounded border" :class="getTagClass(tag)">
            {{ tag }}
          </span>
        </div>

        <p class="text-sm text-gray-500 line-clamp-1 mb-3 bg-gray-50 p-2 rounded">
          "{{ item.desc }}"
        </p>

        <div class="flex justify-between items-center text-xs text-gray-400">
          <span class="flex items-center gap-1"><MapPin :size="12" /> {{ item.location }}</span>
          <span class="flex items-center text-brand-blue font-bold">详情 <ChevronRight :size="14"/></span>
        </div>
      </div>
    </div>

    <div v-else class="relative flex-1 bg-blue-50 overflow-hidden animate-fade-in">
      
      <div class="absolute inset-0 opacity-20 pointer-events-none" 
           style="background-image: radial-gradient(#2563EB 1px, transparent 1px); background-size: 20px 20px;">
      </div>
      <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 flex flex-col items-center z-0">
        <div class="w-16 h-16 bg-brand-blue/20 rounded-full animate-ping absolute"></div>
        <div class="w-4 h-4 bg-brand-blue rounded-full border-2 border-white shadow-lg z-10"></div>
        <span class="mt-1 text-[10px] font-bold bg-white/80 px-1 rounded">我的位置</span>
      </div>

      <div v-for="item in students" :key="item.id"
           class="absolute transition-all duration-500 hover:z-50"
           :style="{ top: item.top, left: item.left }"
           @click="router.push(`/student/${item.id}`)">
        
        <div class="bg-white p-2 rounded-xl shadow-lg border border-brand-orange/30 active:scale-95 cursor-pointer flex items-center gap-2 w-32 group">
          <div class="w-8 h-8 rounded-full bg-brand-orange/10 flex items-center justify-center text-brand-orange font-bold text-xs shrink-0">
            ¥{{ item.price }}
          </div>
          <div class="min-w-0">
            <div class="text-xs font-bold text-gray-800 truncate">{{ item.subject }}</div>
            <div class="text-[10px] text-gray-500 truncate">{{ item.location }}</div>
          </div>
          <div class="absolute -bottom-1.5 left-1/2 -translate-x-1/2 w-3 h-3 bg-white border-b border-r border-brand-orange/30 rotate-45"></div>
        </div>
      </div>

      <div class="absolute bottom-6 left-4 right-4 bg-white/95 backdrop-blur rounded-2xl p-3 shadow-xl flex items-center justify-between">
        <div class="flex items-center gap-3">
           <div class="bg-brand-blue text-white p-2 rounded-lg">
             <Navigation :size="20" />
           </div>
           <div class="text-xs">
             <p class="font-bold text-gray-800">附近发现 3 个需求</p>
             <p class="text-gray-500">定位精准度: <span class="text-green-600">高</span></p>
           </div>
        </div>
        <button class="bg-black text-white text-xs px-4 py-2 rounded-lg font-bold">刷新</button>
      </div>

    </div>

  </div>
</template>