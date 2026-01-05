<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ChevronLeft, Clock, MapPin, CheckCircle, AlertCircle } from 'lucide-vue-next';

const router = useRouter();
const activeTab = ref('upcoming'); // upcoming (待上课) | history (历史)

// 模拟数据：待上课列表
const upcomingClasses = ref([
  {
    id: 101,
    teacher: '张老师',
    subject: '初中英语',
    time: '今天 19:00 - 21:00',
    location: '线上教学',
    status: 'ready' // ready: 可打卡
  },
  {
    id: 102,
    teacher: '李同学',
    subject: '小学奥数',
    time: '周六 09:00 - 11:00',
    location: '幸福小区3号楼',
    status: 'wait' // wait: 未开始
  }
]);

// 模拟数据：历史记录
const historyClasses = ref([
  {
    id: 201,
    teacher: '张老师',
    subject: '初中英语',
    date: '2023-10-01 19:00',
    duration: '2小时',
    status: 'confirmed', // 已确认
    price: 400
  },
  {
    id: 202,
    teacher: '张老师',
    subject: '初中英语',
    date: '2023-09-28 19:00',
    duration: '2小时',
    status: 'pending', // 待家长确认
    price: 400
  }
]);

const handleCheckIn = (id) => {
  if (confirm('确认开始上课打卡吗？系统将记录当前GPS位置。')) {
    alert('打卡成功！开始记录课时。');
  }
};
</script>

<template>
  <div class="min-h-screen bg-gray-50 pb-24"> <div class="bg-white p-4 sticky top-0 z-10 border-b flex items-center">
      <button @click="router.back()" class="p-1 hover:bg-gray-100 rounded-full mr-2">
        <ChevronLeft />
      </button>
      <h1 class="font-bold text-lg flex-1 text-center pr-6">课时记录</h1>
    </div>

    <div class="p-4">
      <div class="flex bg-white p-1 rounded-xl shadow-sm border">
        <button 
          @click="activeTab = 'upcoming'"
          class="flex-1 py-2 text-sm font-bold rounded-lg transition-all"
          :class="activeTab === 'upcoming' ? 'bg-blue-600 text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'">
          待上课 / 打卡
        </button>
        <button 
          @click="activeTab = 'history'"
          class="flex-1 py-2 text-sm font-bold rounded-lg transition-all"
          :class="activeTab === 'history' ? 'bg-blue-600 text-white shadow-md' : 'text-gray-500 hover:bg-gray-50'">
          历史记录
        </button>
      </div>
    </div>

    <div class="px-4 space-y-4">
      
      <div v-if="activeTab === 'upcoming'">
        <div v-for="item in upcomingClasses" :key="item.id" 
             class="bg-white p-5 rounded-2xl shadow-sm border border-gray-100">
          
          <div class="flex justify-between items-start mb-3">
            <div>
              <h3 class="font-bold text-lg text-gray-800">{{ item.subject }}</h3>
              <p class="text-sm text-gray-500 mt-1">授课教师：{{ item.teacher }}</p>
            </div>
            <span class="px-2 py-1 bg-blue-50 text-blue-600 text-xs font-bold rounded">
              {{ item.status === 'ready' ? '即将开始' : '未开始' }}
            </span>
          </div>

          <div class="space-y-2 mb-4">
            <div class="flex items-center gap-2 text-sm text-gray-600">
              <Clock :size="16" class="text-blue-500" /> {{ item.time }}
            </div>
            <div class="flex items-center gap-2 text-sm text-gray-600">
              <MapPin :size="16" class="text-green-500" /> {{ item.location }}
            </div>
          </div>

          <button v-if="item.status === 'ready'" 
                  @click="handleCheckIn(item.id)"
                  class="w-full bg-blue-600 text-white py-3 rounded-xl font-bold hover:bg-blue-700 active:scale-95 transition-transform flex justify-center items-center gap-2">
            <MapPin :size="18" /> 上课打卡
          </button>
          <button v-else disabled class="w-full bg-gray-100 text-gray-400 py-3 rounded-xl font-bold cursor-not-allowed">
            不在打卡时间
          </button>
        </div>
      </div>

      <div v-if="activeTab === 'history'">
        <div v-for="item in historyClasses" :key="item.id" class="bg-white p-4 rounded-xl border border-gray-100 flex justify-between items-center">
          <div>
            <h4 class="font-bold text-gray-800">{{ item.subject }} - {{ item.teacher }}</h4>
            <p class="text-xs text-gray-400 mt-1">{{ item.date }} | {{ item.duration }}</p>
          </div>
          <div class="text-right">
            <div class="font-bold text-gray-800">-{{ item.price }}元</div>
            <div class="text-xs mt-1 flex items-center justify-end gap-1"
                 :class="item.status === 'confirmed' ? 'text-green-500' : 'text-orange-500'">
              <span v-if="item.status === 'confirmed'"><CheckCircle :size="12"/> 已确认</span>
              <span v-else><AlertCircle :size="12"/> 待确认</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>