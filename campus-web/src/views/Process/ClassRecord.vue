<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ChevronLeft, Calendar as CalIcon, MapPin, Camera, CheckCircle, Clock } from 'lucide-vue-next';

const router = useRouter();
// 模拟当前用户身份 (实际从 localStorage 拿)
const userRole = localStorage.getItem('userRole') || 'parent';

// --- 1. 日历数据 (文档 cite: 99) ---
const currentMonth = '2025年3月';
const calendarDays = [
  { day: 1, status: 'done' }, { day: 2, status: 'done' }, { day: 3, status: 'none' },
  { day: 4, status: 'cancel' }, { day: 5, status: 'today' }, { day: 6, status: 'pending' },
  { day: 7, status: 'pending' }
];

// 状态样式映射
const statusStyles = {
  done: 'bg-green-100 text-green-600 border-green-200', // 已完成
  pending: 'bg-blue-50 text-blue-600 border-blue-100', // 待上
  cancel: 'bg-gray-100 text-gray-400 border-gray-200', // 取消
  today: 'bg-brand-orange text-white shadow-md', // 今日
  none: 'text-gray-300'
};

// --- 2. 今日课程数据 ---
const todayClass = ref({
  id: 101,
  subject: '初中数学 (一对一)',
  time: '19:00 - 21:00',
  location: '幸福小区3号楼',
  teacher: '张老师',
  status: 'ready', // ready(未开始) | checkin(已打卡) | confirmed(已确认)
  checkinImg: null
});

// --- 交互逻辑 (文档 cite: 102, 103) ---

// 教师打卡
const handleCheckIn = () => {
  if (!confirm('是否确认打卡？\n系统将记录当前位置：幸福小区 (误差10m)')) return;
  
  // 模拟拍照上传
  todayClass.value.status = 'checkin';
  todayClass.value.checkinImg = 'https://api.dicebear.com/7.x/shapes/svg?seed=checkin'; 
  alert('打卡成功！等待家长确认。');
};

// 家长确认
const handleConfirm = () => {
  todayClass.value.status = 'confirmed';
  alert('确认成功！资金已释放给老师。');
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-safe font-sans">
    
    <div class="bg-white p-4 sticky top-0 z-10 border-b flex items-center justify-between">
      <button @click="router.back()" class="p-1 hover:bg-gray-100 rounded-full">
        <ChevronLeft />
      </button>
      <h1 class="font-bold text-lg">我的课表</h1>
      <div class="w-8"></div> </div>

    <div class="bg-white p-4 mb-4 shadow-sm">
      <div class="flex justify-between items-center mb-4">
        <h2 class="font-bold text-lg flex items-center gap-2">
          {{ currentMonth }} <CalIcon :size="16" class="text-gray-400"/>
        </h2>
        <div class="flex gap-2 text-xs text-gray-500">
          <span class="flex items-center gap-1"><div class="w-2 h-2 bg-green-500 rounded-full"></div>完成</span>
          <span class="flex items-center gap-1"><div class="w-2 h-2 bg-brand-blue rounded-full"></div>待上</span>
        </div>
      </div>
      
      <div class="flex justify-between">
        <div v-for="d in calendarDays" :key="d.day" 
             class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold border transition-all"
             :class="statusStyles[d.status] || 'text-gray-800 border-transparent'">
          {{ d.day }}
        </div>
      </div>
    </div>

    <div class="px-4">
      <h3 class="font-bold text-gray-500 mb-2 text-sm">今日课程 (3月5日)</h3>
      
      <div class="bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100">
        <div class="bg-brand-blue p-4 text-white flex justify-between items-start">
          <div>
            <h2 class="text-xl font-bold">{{ todayClass.subject }}</h2>
            <p class="text-blue-100 text-sm mt-1 flex items-center gap-1">
              <Clock :size="14" /> {{ todayClass.time }}
            </p>
          </div>
          <span class="bg-white/20 px-2 py-1 rounded text-xs">
            {{ todayClass.status === 'confirmed' ? '已结算' : '进行中' }}
          </span>
        </div>

        <div class="p-5 space-y-4">
          <div class="flex items-center gap-3 text-gray-600">
            <MapPin :size="20" class="text-brand-orange" />
            <span>{{ todayClass.location }}</span>
          </div>
          
          <div v-if="todayClass.status !== 'ready'" class="bg-gray-50 p-3 rounded-lg border border-dashed border-gray-300 flex items-center gap-3">
             <div class="w-12 h-12 bg-gray-200 rounded overflow-hidden">
               <img v-if="todayClass.checkinImg" :src="todayClass.checkinImg" class="w-full h-full object-cover" />
               <Camera v-else class="m-auto mt-3 text-gray-400" :size="20" />
             </div>
             <div class="text-xs text-gray-500">
               <p class="font-bold text-gray-800">教师已打卡</p>
               <p>18:55 打卡于 幸福小区 (GPS核验通过)</p>
             </div>
          </div>

          <div v-if="userRole === 'teacher'">
             <button v-if="todayClass.status === 'ready'" @click="handleCheckIn"
                     class="w-full bg-brand-blue text-white py-3 rounded-xl font-bold active:scale-95 transition-transform flex justify-center items-center gap-2">
               <MapPin :size="18" /> 上课打卡
             </button>
             <button v-else disabled class="w-full bg-gray-100 text-gray-400 py-3 rounded-xl font-bold cursor-not-allowed">
               已完成打卡
             </button>
          </div>

          <div v-else>
            <button v-if="todayClass.status === 'checkin'" @click="handleConfirm"
                    class="w-full bg-brand-orange text-white py-3 rounded-xl font-bold shadow-lg active:scale-95 transition-transform flex justify-center items-center gap-2">
              <CheckCircle :size="18" /> 确认课时 (支付结算)
            </button>
            <div v-else-if="todayClass.status === 'confirmed'" class="text-center text-green-600 font-bold py-2 border border-green-100 bg-green-50 rounded-xl">
              <CheckCircle :size="16" class="inline mb-0.5"/> 已确认，资金已释放
            </div>
            <div v-else class="text-center text-gray-400 text-sm py-2">
              等待老师打卡...
            </div>
          </div>

        </div>
      </div>
    </div>

  </div>
</template>