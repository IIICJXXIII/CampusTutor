<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { store } from '../../store.js'; 
import { ChevronLeft, Calendar, Clock, CheckCircle, ShieldCheck, Loader2 } from 'lucide-vue-next';
import { createOrder } from '../../api/order.js';

const router = useRouter();
const route = useRoute();
const step = ref(1); // 1: 选时间, 2: 签合同
const isLoading = ref(false);
const isSigning = ref(false);

// 从路由参数获取教师信息，如果没有则使用默认值
const teacher = ref({
  id: null,
  name: '张老师',
  subject: '初中数学',
  price: 200
});

// 从URL参数初始化教师信息
onMounted(() => {
  if (route.query.teacherId) {
    teacher.value.id = route.query.teacherId;
  }
  if (route.query.teacherName) {
    teacher.value.name = route.query.teacherName;
  }
  if (route.query.subject) {
    teacher.value.subject = route.query.subject;
  }
  if (route.query.price) {
    teacher.value.price = parseInt(route.query.price) || 200;
  }
});

// --- Step 1: 日历与时间数据 ---
const selectedDate = ref(null);
const selectedTime = ref(null);

// 生成未来7天的日期
const generateDays = () => {
  const days = [];
  const today = new Date();
  for (let i = 0; i < 7; i++) {
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    days.push({
      day: dayNames[date.getDay()],
      date: `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`,
      fullDate: date,
      available: date.getDay() === 0 || date.getDay() === 6 || i < 3 // 周末和近3天可预约
    });
  }
  return days;
};

const days = generateDays();

const timeSlots = [
  '09:00 - 10:00', '10:30 - 11:30', '14:00 - 15:00', '19:00 - 20:00'
];

// --- Step 2: 签约数据 ---
const isAgreed = ref(false);

const handleNext = () => {
  if (step.value === 1 && selectedDate.value && selectedTime.value) {
    step.value = 2;
  }
};

// 核心：生成订单逻辑
const handleSign = async () => {
  if (!isAgreed.value || isSigning.value) return;
  
  isSigning.value = true;

  try {
    const lessonCount = 10; // 10课时包
    const totalAmount = teacher.value.price * lessonCount;
    
    // 调用后端API创建订单
    const orderData = {
      tutorId: teacher.value.id || 1, // 教师ID
      demandId: 1, // 需求ID，实际应从参数传入
      lessonCount: lessonCount,
      unitPrice: teacher.value.price,
      totalAmount: totalAmount,
      scheduledTime: selectedDate.value + ' ' + selectedTime.value.split(' - ')[0]
    };

    let orderId;
    try {
      const res = await createOrder(orderData);
      orderId = res.data;
    } catch (e) {
      // 如果API调用失败，使用本地模拟
      orderId = 'ORD-' + Date.now().toString().slice(-6);
    }

    // 同时存入本地状态（兼容本地演示）
    const newOrder = {
      id: orderId,
      teacher: teacher.value.name,
      subject: `${teacher.value.subject} · ${lessonCount}课时包`,
      amount: totalAmount,
      status: 'pending',
      date: new Date().toLocaleString(),
      tags: ['待支付'],
      location: '线上教学'
    };
    store.addOrder(newOrder);

    alert('签约成功！订单已生成，请前往支付。');
    
    router.push({
      path: '/payment',
      query: { orderId: orderId }
    });
  } catch (error) {
    console.error('签约失败:', error);
    alert('签约失败，请重试');
  } finally {
    isSigning.value = false;
  }
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-safe font-sans">
    
    <div class="bg-white p-4 sticky top-0 z-10 border-b flex items-center">
      <button @click="step === 1 ? router.back() : step = 1" class="p-1 hover:bg-gray-100 rounded-full mr-2">
        <ChevronLeft />
      </button>
      <h1 class="font-bold text-lg flex-1 text-center pr-6">
        {{ step === 1 ? '预约试课' : '三方协议签署' }}
      </h1>
    </div>

    <div v-if="step === 1" class="p-4 space-y-4 animate-fade-in">
      
      <div class="bg-white p-4 rounded-xl flex justify-between items-center shadow-sm">
        <div>
          <h3 class="font-bold text-lg">{{ teacher.name }}</h3>
          <p class="text-sm text-gray-500">{{ teacher.subject }} · 试课申请</p>
        </div>
        <span class="font-bold text-brand-orange text-xl">¥{{ teacher.price }}</span>
      </div>

      <div class="bg-white p-4 rounded-xl shadow-sm">
        <h3 class="font-bold text-sm mb-3 flex items-center gap-2">
          <Calendar :size="18" class="text-brand-blue"/> 选择日期 (可授课日标黄)
        </h3>
        <div class="grid grid-cols-4 gap-2">
          <div v-for="d in days" :key="d.date"
               @click="d.available ? selectedDate = d.date : null"
               class="flex flex-col items-center py-3 rounded-lg border-2 transition-all cursor-pointer"
               :class="[
                 d.available ? 'bg-orange-50/50 border-orange-100 text-gray-800' : 'bg-gray-50 border-transparent text-gray-300 cursor-not-allowed',
                 selectedDate === d.date ? '!border-brand-orange bg-orange-100' : ''
               ]">
            <span class="text-xs">{{ d.day }}</span>
            <span class="font-bold text-sm">{{ d.date }}</span>
          </div>
        </div>
      </div>

      <div class="bg-white p-4 rounded-xl shadow-sm">
        <h3 class="font-bold text-sm mb-3 flex items-center gap-2">
          <Clock :size="18" class="text-brand-blue"/> 选择时段
        </h3>
        <div class="grid grid-cols-2 gap-3">
          <div v-for="time in timeSlots" :key="time"
               @click="selectedTime = time"
               class="py-3 px-4 rounded-lg border text-center text-sm font-medium transition-all cursor-pointer"
               :class="selectedTime === time ? 'bg-blue-600 text-white border-blue-600' : 'border-gray-200 text-gray-600 hover:border-blue-300'"> 
            {{ time }}
          </div>
        </div>
      </div>

      <div class="fixed bottom-0 left-0 w-full p-4 bg-white border-t">
        <button @click="handleNext" 
                :disabled="!selectedDate || !selectedTime"
                class="w-full bg-black text-white py-3 rounded-xl font-bold shadow-lg disabled:opacity-50 disabled:cursor-not-allowed">
          下一步：确认协议
        </button>
      </div>
    </div>

    <div v-if="step === 2" class="p-4 space-y-4 animate-fade-in pb-32">
      
      <div class="text-center py-4">
        <ShieldCheck :size="48" class="text-green-500 mx-auto mb-2" />
        <h2 class="text-xl font-bold text-gray-800">易家教平台授课协议</h2>
        <p class="text-xs text-gray-400">为了保障您的权益，请仔细阅读以下条款</p>
      </div>

      <div class="bg-white p-6 rounded-xl shadow-sm border border-gray-100 h-80 overflow-y-auto text-sm text-gray-600 leading-relaxed">
        <p class="mb-4">
          <strong>甲方（学生家长）：</strong>当前用户<br>
          <strong>乙方（授课教师）：</strong>{{ teacher.name }}<br>
          <strong>丙方（平台方）：</strong>易家教平台
        </p>
        
        <p class="mb-2">根据《中华人民共和国民法典》及相关法律法规，三方达成如下协议：</p>
        
        <p class="mb-2">
          1. <span class="text-blue-600 font-bold">服务内容与费用</span>：乙方为甲方子女提供{{ teacher.subject }}辅导服务，费用标准为 <span class="text-red-500 font-bold">{{ teacher.price }}元/小时</span>。
        </p>
        
        <p class="mb-2">
          2. <span class="text-blue-600 font-bold">资金托管保障</span>：所有课时费将托管于丙方平台，待每节课结束后，经甲方确认（或24小时自动确认）后释放给乙方。
        </p>

        <p class="mb-2">
          3. <span class="text-blue-600 font-bold">退款规则</span>：试课首节不满意可全额退款；后续课程如需终止，未消耗课时费将在3个工作日内原路退回。
        </p>

        <p>4. 双方应严格遵守预约时间，违约方需承担相应责任...</p>
        <div class="h-20"></div> 
      </div>

      <div class="fixed bottom-0 left-0 w-full p-4 bg-white border-t shadow-[0_-5px_15px_rgba(0,0,0,0.05)]">
        <label class="flex items-center gap-2 mb-4 cursor-pointer">
          <input type="checkbox" v-model="isAgreed" class="w-5 h-5 accent-brand-blue rounded" />
          <span class="text-sm text-gray-600">我已阅读并同意 <span class="text-blue-600">《三方授课协议》</span></span>
        </label>
        
        <button @click="handleSign" 
                :disabled="!isAgreed || isSigning"
                class="w-full py-3 rounded-xl font-bold text-white shadow-lg transition-all flex items-center justify-center gap-2"
                :class="isAgreed && !isSigning ? 'bg-brand-blue hover:bg-blue-700' : 'bg-gray-300 cursor-not-allowed'"> 
          <Loader2 v-if="isSigning" :size="18" class="animate-spin" />
          {{ isSigning ? '签约中...' : '确认签约 (生成订单)' }}
        </button>
      </div>

    </div>

  </div>
</template>
