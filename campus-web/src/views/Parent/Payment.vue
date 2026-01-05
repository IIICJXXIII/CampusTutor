<script setup>
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { store } from '../../store.js';
import { ChevronLeft, ShieldCheck, CheckCircle, Loader2, MapPin, Wallet, Lock } from 'lucide-vue-next';

const router = useRouter();
const route = useRoute();
const status = ref('pending');

// 获取订单数据
const orderId = route.query.orderId;
const currentOrder = computed(() => {
  return store.orders.find(o => o.id === orderId) || {
    id: 'ORD-DEMO', 
    teacher: '演示老师', 
    subject: '演示课程', 
    total: 3800,
    price: 4000, 
    discount: 200,
    location: '幸福小区3号楼 (您发布的地址)' // ★ 关键：展示地点
  };
});

const payAmount = computed(() => currentOrder.value.amount || currentOrder.value.total);

const handlePay = () => {
  status.value = 'processing';
  setTimeout(() => {
    status.value = 'success';
    if (orderId) store.updateOrderStatus(orderId, 'active');
  }, 2000);
};
</script>

<template>
  <div class="min-h-screen bg-gray-100 pb-safe font-sans flex flex-col">
    
    <div class="bg-white p-4 sticky top-0 z-10 border-b flex items-center justify-center">
      <button v-if="status === 'pending'" @click="router.back()" class="absolute left-4 p-1">
        <ChevronLeft />
      </button>
      <h1 class="font-bold text-lg">收银台</h1>
    </div>

    <div v-if="status !== 'success'" class="flex-1 p-4 space-y-4">
      
      <div class="text-center py-4">
        <p class="text-sm text-gray-500">支付剩余时间</p>
        <p class="text-2xl font-bold font-mono text-gray-800">14:59</p>
      </div>

      <div class="bg-white rounded-xl overflow-hidden shadow-sm">
        <div class="p-6 text-center border-b border-gray-50 border-dashed">
          <p class="text-xs text-gray-400 mb-1">易家教 - 资金托管账户</p>
          <div class="text-4xl font-bold text-gray-900">¥{{ payAmount }}</div>
        </div>
        
        <div class="p-4 space-y-3 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-500">课程商品</span>
            <span class="font-bold">{{ currentOrder.subject }}</span>
          </div>
          <div class="flex justify-between">
             <span class="text-gray-500">授课教师</span>
             <span>{{ currentOrder.teacher }}</span>
          </div>
          <div class="flex justify-between items-start">
             <span class="text-gray-500 shrink-0">授课地点</span>
             <span class="text-right flex items-center gap-1">
               <MapPin :size="12" class="text-gray-400"/> {{ currentOrder.location || '线上教学' }}
             </span>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl p-4 shadow-sm space-y-4">
        <h3 class="text-sm font-bold text-gray-400">选择支付方式</h3>
        
        <div class="flex items-center justify-between py-2">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 bg-[#07C160] rounded flex items-center justify-center text-white">
              <Wallet :size="20" /> </div>
            <div>
              <p class="font-bold text-gray-800">微信支付</p>
              <p class="text-[10px] text-gray-400">亿万用户的选择，安全快捷</p>
            </div>
          </div>
          <div class="w-5 h-5 rounded-full border-4 border-brand-blue"></div>
        </div>
      </div>
      
      <div class="flex items-center justify-center gap-1 text-xs text-gray-400 mt-6">
        <ShieldCheck :size="14" class="text-green-500" />
        平台全程资金托管，确认课时后结算
      </div>
    </div>

    <div v-else class="flex-1 flex flex-col items-center justify-center bg-white p-8 text-center animate-fade-in">
      <div class="w-20 h-20 bg-green-500 rounded-full flex items-center justify-center mb-6 shadow-xl shadow-green-200">
        <CheckCircle :size="48" class="text-white" />
      </div>
      <h2 class="text-2xl font-bold text-gray-900 mb-2">支付成功</h2>
      <p class="text-gray-500 text-sm">订单金额已冻结至托管账户</p>
      
      <div class="mt-8 bg-gray-50 p-4 rounded-xl w-full border border-gray-100">
        <div class="flex items-center gap-3 text-left">
          <div class="bg-white p-2 rounded-full shadow-sm">
            <Lock :size="20" class="text-brand-blue"/>
          </div>
          <div>
            <p class="font-bold text-sm text-gray-800">资金托管中</p>
            <p class="text-xs text-gray-400">每次课后家长确认，资金分批到账</p>
          </div>
        </div>
      </div>
      
      <div class="mt-auto w-full space-y-3">
        <button @click="router.replace('/process/record')" class="w-full bg-brand-blue text-white py-3 rounded-xl font-bold">
          查看课表
        </button>
        <button @click="router.replace('/mine/orders')" class="w-full text-gray-500 py-3 text-sm">
          返回订单列表
        </button>
      </div>
    </div>

    <div v-if="status !== 'success'" class="bg-white p-4 border-t sticky bottom-0">
      <button v-if="status === 'pending'" @click="handlePay" 
              class="w-full bg-[#07C160] text-white py-3 rounded-xl font-bold shadow-lg active:scale-95 transition-transform">
        立即支付 ¥{{ payAmount }}
      </button>
      <button v-else disabled class="w-full bg-gray-300 text-white py-3 rounded-xl font-bold flex items-center justify-center gap-2">
        <Loader2 class="animate-spin" /> 安全处理中...
      </button>
    </div>

  </div>
</template>