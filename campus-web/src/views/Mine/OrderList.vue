<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ChevronLeft, Package, Clock, CheckCircle, AlertCircle } from 'lucide-vue-next';

const router = useRouter();
const activeTab = ref('all'); // all | pending | active | done

// 模拟订单数据 (对应文档 orders 表结构)
const orders = ref([
  {
    id: 'ORD-20250301-01',
    teacher: '张老师',
    subject: '初中数学 · 20课时包',
    amount: 3800,
    status: 'pending', // 待支付
    date: '2025-03-01 14:30',
    tags: ['待支付']
  },
  {
    id: 'ORD-20250215-09',
    teacher: '李同学',
    subject: '小学奥数 · 10课时包',
    amount: 1500,
    status: 'active', // 进行中 (资金托管中)
    progress: '6/10 课时',
    date: '2025-02-15 09:00',
    tags: ['托管中', '授课中']
  },
  {
    id: 'ORD-20241210-33',
    teacher: '王老师',
    subject: '高中物理 · 考前冲刺',
    amount: 2000,
    status: 'done', // 已完成
    date: '2024-12-10',
    tags: ['已结清', '五星好评']
  }
]);

// 状态样式映射
const statusColors = {
  pending: 'text-brand-orange bg-orange-50 border-orange-100',
  active: 'text-brand-blue bg-blue-50 border-blue-100',
  done: 'text-green-600 bg-green-50 border-green-100'
};

// 按钮操作
const handleAction = (order) => {
  if (order.status === 'pending') {
    router.push('/payment'); // 去支付
  } else if (order.status === 'active') {
    router.push('/process/record'); // 去看课表
  }
};
</script>

<template>
  <div class="min-h-screen bg-brand-gray pb-safe font-sans">
    
    <div class="bg-white p-4 sticky top-0 z-10 border-b flex items-center justify-center relative">
      <button @click="router.back()" class="absolute left-4 p-1 hover:bg-gray-100 rounded-full">
        <ChevronLeft />
      </button>
      <h1 class="font-bold text-lg">我的订单</h1>
    </div>

    <div class="bg-white px-4 pt-2 flex justify-between text-sm text-gray-500 sticky top-[60px] z-10 shadow-sm">
      <span v-for="tab in ['all', 'pending', 'active', 'done']" :key="tab"
            @click="activeTab = tab"
            class="pb-3 px-2 border-b-2 transition-colors cursor-pointer capitalize"
            :class="activeTab === tab ? 'border-brand-blue text-brand-blue font-bold' : 'border-transparent'">
        {{ tab === 'all' ? '全部' : tab === 'pending' ? '待支付' : tab === 'active' ? '进行中' : '已完成' }}
      </span>
    </div>

    <div class="p-4 space-y-4">
      <div v-for="item in orders.filter(o => activeTab === 'all' || o.status === activeTab)" :key="item.id"
           class="bg-white rounded-xl p-4 shadow-sm border border-gray-100 active:scale-[0.99] transition-transform">
        
        <div class="flex justify-between items-start mb-3 pb-3 border-b border-gray-50">
          <div class="text-xs text-gray-400 font-mono">
            {{ item.id }}
          </div>
          <span class="text-xs px-2 py-1 rounded border font-medium" :class="statusColors[item.status]">
            {{ item.tags[0] }}
          </span>
        </div>

        <div class="flex items-start gap-4 mb-4">
          <div class="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center text-gray-400">
            <Package :size="24" />
          </div>
          <div class="flex-1">
            <h3 class="font-bold text-gray-800">{{ item.subject }}</h3>
            <p class="text-sm text-gray-500 mt-1">教师：{{ item.teacher }}</p>
            <p class="text-xs text-gray-400 mt-1">{{ item.date }}</p>
          </div>
          <div class="text-right">
            <div class="text-lg font-bold text-gray-800">¥{{ item.amount }}</div>
            <div v-if="item.progress" class="text-xs text-brand-blue mt-1">{{ item.progress }}</div>
          </div>
        </div>

        <div class="flex justify-end gap-3 pt-2">
          <button class="px-4 py-2 text-xs font-bold text-gray-600 bg-gray-50 rounded-lg border border-gray-200">
            查看合同
          </button>
          
          <button v-if="item.status === 'pending'" @click="handleAction(item)"
                  class="px-6 py-2 text-xs font-bold text-white bg-brand-orange rounded-lg shadow-sm">
            去支付
          </button>
          
          <button v-if="item.status === 'active'" @click="handleAction(item)"
                  class="px-6 py-2 text-xs font-bold text-white bg-brand-blue rounded-lg shadow-sm">
            查看进度
          </button>
        </div>

      </div>
      
      <div v-if="orders.length === 0" class="text-center py-20 text-gray-400">
        <Package :size="48" class="mx-auto mb-4 opacity-20" />
        <p>暂无相关订单</p>
      </div>
    </div>
  </div>
</template>