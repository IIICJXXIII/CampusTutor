<script setup>
import { useRouter } from 'vue-router';
import { ChevronLeft, MapPin, Clock, ShieldCheck, MessageCircle } from 'lucide-vue-next';

const router = useRouter();

// 模拟详情数据
const demand = {
  name: '王女士',
  subject: '小学三年级 · 数学',
  price: 180,
  frequency: '每周2次',
  location: '阳光花园 (距您1.2km)',
  desc: '孩子目前三年级，计算基础比较薄弱，做题粗心。希望找一位有耐心的大学生老师，最好是理科专业的，能带孩子整理错题。',
  tags: ['需要耐心', '基础巩固', '提供零食'],
  idVerified: true
};

const handleContact = () => {
  alert('沟通请求已发送！请等待家长回复。');
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-safe font-sans">
    
    <div class="bg-white p-4 sticky top-0 z-10 border-b flex items-center justify-between">
      <button @click="router.back()" class="p-1 hover:bg-gray-100 rounded-full">
        <ChevronLeft />
      </button>
      <h1 class="font-bold text-lg">需求详情</h1>
      <button class="text-gray-400"><MessageCircle /></button>
    </div>

    <div class="p-4 space-y-4">
      
      <div class="bg-white rounded-xl p-5 shadow-sm">
        <div class="flex justify-between items-start mb-4">
          <div>
            <h2 class="text-xl font-bold text-gray-900">{{ demand.subject }}</h2>
            <div class="flex items-center gap-2 mt-1 text-sm text-gray-500">
              <span class="font-bold text-gray-700">{{ demand.name }}</span>
              <span v-if="demand.idVerified" class="text-green-600 bg-green-50 px-1.5 py-0.5 rounded text-[10px] border border-green-100 flex items-center gap-0.5">
                <ShieldCheck :size="10"/> 已实名
              </span>
            </div>
          </div>
          <div class="text-right">
            <div class="text-2xl font-bold text-brand-orange">¥{{ demand.price }}</div>
            <div class="text-xs text-gray-400">/小时</div>
          </div>
        </div>

        <div class="flex flex-wrap gap-2 pt-3 border-t border-gray-50">
          <span v-for="tag in demand.tags" :key="tag" class="bg-gray-100 text-gray-600 text-xs px-2 py-1 rounded">
            {{ tag }}
          </span>
        </div>
      </div>

      <div class="bg-white rounded-xl p-5 shadow-sm space-y-4">
        <div class="flex items-start gap-3">
          <div class="w-8 h-8 rounded-full bg-blue-50 flex items-center justify-center text-brand-blue shrink-0">
            <Clock :size="16" />
          </div>
          <div>
            <h3 class="font-bold text-sm text-gray-800">授课时间</h3>
            <p class="text-sm text-gray-500 mt-0.5">{{ demand.frequency }}</p>
          </div>
        </div>

        <div class="flex items-start gap-3">
          <div class="w-8 h-8 rounded-full bg-orange-50 flex items-center justify-center text-brand-orange shrink-0">
            <MapPin :size="16" />
          </div>
          <div>
            <h3 class="font-bold text-sm text-gray-800">授课地点</h3>
            <p class="text-sm text-gray-500 mt-0.5">{{ demand.location }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl p-5 shadow-sm">
        <h3 class="font-bold text-gray-800 mb-2">家长描述</h3>
        <p class="text-sm text-gray-600 leading-relaxed">
          {{ demand.desc }}
        </p>
      </div>
    </div>

    <div class="fixed bottom-0 w-full bg-white p-4 border-t">
      <button @click="handleContact" 
              class="w-full bg-black text-white py-3 rounded-xl font-bold shadow-lg active:scale-95 transition-transform">
        立即沟通
      </button>
    </div>

  </div>
</template>