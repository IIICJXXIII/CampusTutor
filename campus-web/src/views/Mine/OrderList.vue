<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { store } from '../../store.js';
import { getParentOrders, getTutorOrders } from '../../api/order.js';
import { ChevronLeft, Package, Clock, CheckCircle, AlertCircle, Loader2 } from 'lucide-vue-next';

const router = useRouter();
const activeTab = ref('all'); // all | pending | active | done
const loading = ref(false);
const orders = ref([]);

// 根据用户角色获取订单
const fetchOrders = async () => {
  loading.value = true;
  try {
    const isParent = store.userRole === 'parent';
    const res = isParent ? await getParentOrders() : await getTutorOrders();
    
    // 后端返回分页格式，数据在 records 字段中
    const orderList = res.data?.records || res.data || [];
    
    // 转换后端数据格式为前端格式
    orders.value = orderList.map(order => ({
      id: order.orderNo || `ORD-${order.id}`,
      orderId: order.id,
      teacher: order.tutorName || '待分配',
      subject: `${order.subject || '课程'} · ${order.totalLessons || 10}课时包`,
      amount: order.totalPrice || order.totalAmount,
      status: mapOrderStatus(order.status),
      progress: order.remainLessons != null ? `${(order.totalLessons || 10) - order.remainLessons}/${order.totalLessons || 10} 课时` : null,
      date: order.createTime,
      tags: [getStatusTag(order.status)]
    }));
  } catch (error) {
    console.error('获取订单失败:', error);
    // 使用模拟数据
    orders.value = getMockOrders();
  } finally {
    loading.value = false;
  }
};

// 后端状态码映射到前端状态
const mapOrderStatus = (status) => {
  // 0-待支付, 1-已支付(托管中), 2-进行中, 3-已完成, 4-退款中, 5-已退款
  switch (status) {
    case 0: return 'pending';
    case 1: case 2: return 'active';
    case 3: return 'done';
    case 4: case 5: return 'refund';
    default: return 'pending';
  }
};

const getStatusTag = (status) => {
  switch (status) {
    case 0: return '待支付';
    case 1: return '托管中';
    case 2: return '授课中';
    case 3: return '已完成';
    case 4: return '退款中';
    case 5: return '已退款';
    default: return '未知';
  }
};

// 模拟订单数据 (后端未连接时使用)
const getMockOrders = () => [
  {
    id: 'ORD-20250301-01',
    orderId: 1,
    teacher: '张老师',
    subject: '初中数学 · 20课时包',
    amount: 3800,
    status: 'pending',
    date: '2025-03-01 14:30',
    tags: ['待支付']
  },
  {
    id: 'ORD-20250215-09',
    orderId: 2,
    teacher: '李同学',
    subject: '小学奥数 · 10课时包',
    amount: 1500,
    status: 'active',
    progress: '6/10 课时',
    date: '2025-02-15 09:00',
    tags: ['托管中']
  },
  {
    id: 'ORD-20241210-33',
    orderId: 3,
    teacher: '王老师',
    subject: '高中物理 · 考前冲刺',
    amount: 2000,
    status: 'done',
    date: '2024-12-10',
    tags: ['已结清']
  }
];

onMounted(() => {
  fetchOrders();
});

// 状态样式映射
const statusColors = {
  pending: 'text-brand-orange bg-orange-50 border-orange-100',
  active: 'text-brand-blue bg-blue-50 border-blue-100',
  done: 'text-green-600 bg-green-50 border-green-100',
  refund: 'text-red-600 bg-red-50 border-red-100'
};

// 按钮操作
const handleAction = (order) => {
  if (order.status === 'pending') {
    router.push({ path: '/payment', query: { orderId: order.orderId } });
  } else if (order.status === 'active') {
    router.push({ path: '/process/record', query: { orderId: order.orderId } });
  }
};

// 筛选后的订单
const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value;
  return orders.value.filter(o => o.status === activeTab.value);
});
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
      <!-- 加载中 -->
      <div v-if="loading" class="text-center py-20">
        <Loader2 :size="32" class="mx-auto mb-4 animate-spin text-brand-blue" />
        <p class="text-gray-400">加载中...</p>
      </div>

      <div v-else-if="filteredOrders.length > 0" v-for="item in filteredOrders" :key="item.id"
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
      
      <div v-else-if="!loading && filteredOrders.length === 0" class="text-center py-20 text-gray-400">
        <Package :size="48" class="mx-auto mb-4 opacity-20" />
        <p>暂无相关订单</p>
      </div>
    </div>
  </div>
</template>