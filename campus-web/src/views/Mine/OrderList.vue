<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores'
import { getParentOrders, getTutorOrders, confirmOrder } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('all')
const loading = ref(false)
const orders = ref([])

// 根据用户角色获取订单
const fetchOrders = async () => {
  loading.value = true
  try {
    const isParent = userStore.isParent
    const res = isParent ? await getParentOrders() : await getTutorOrders()
    
    const orderList = res.data?.records || res.data || []
    
    orders.value = orderList.map(order => ({
      id: order.orderNo || `ORD-${order.id}`,
      orderId: order.id,
      teacher: order.tutorName || '待分配',
      subject: `${order.subject || '课程'} · ${order.totalHours || 10}课时包`,
      amount: order.totalPrice || order.totalAmount,
      rawStatus: order.status, // 保留原始状态用于操作判断
      status: mapOrderStatus(order.status),
      statusText: getStatusTag(order.status),
      progress: order.usedHours != null ? `${order.usedHours}/${order.totalHours || 10} 课时` : null,
      date: order.createTime
    }))
  } catch (error) {
    console.error('获取订单失败:', error)
    orders.value = getMockOrders()
  } finally {
    loading.value = false
  }
}

const mapOrderStatus = (status) => {
  switch (status) {
    case -1: return 'confirm' // 待确认
    case 0: return 'pending'
    case 1: case 2: return 'active'
    case 3: return 'done'
    case 4: case 5: return 'refund'
    default: return 'pending'
  }
}

const getStatusTag = (status) => {
  switch (status) {
    case -1: return '待确认'
    case 0: return '待支付'
    case 1: return '托管中'
    case 2: return '授课中'
    case 3: return '已完成'
    case 4: return '退款中'
    case 5: return '已退款'
    default: return '未知'
  }
}

const getStatusType = (status) => {
  switch (status) {
    case 'confirm': return 'info'
    case 'pending': return 'warning'
    case 'active': return 'primary'
    case 'done': return 'success'
    case 'refund': return 'danger'
    default: return 'info'
  }
}

const getMockOrders = () => [
  {
    id: 'ORD-20250301-01',
    orderId: 1,
    teacher: '张老师',
    subject: '初中数学 · 20课时包',
    amount: 3800,
    rawStatus: 0,
    status: 'pending',
    statusText: '待支付',
    date: '2025-03-01 14:30'
  }
]

onMounted(() => {
  fetchOrders()
})

const tabs = [
  { value: 'all', label: '全部' },
  { value: 'confirm', label: '待确认' },
  { value: 'pending', label: '待支付' },
  { value: 'active', label: '进行中' },
  { value: 'done', label: '已完成' }
]

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

// 家长确认订单
const handleConfirm = async (order) => {
  try {
    await ElMessageBox.confirm(
      '确认后订单将变为待支付状态，您需要支付后才能开始授课。',
      '确认接单',
      { confirmButtonText: '确认', cancelButtonText: '取消', type: 'info' }
    )
    
    const res = await confirmOrder(order.orderId)
    if (res.code === 200) {
      ElMessage.success('已确认，请支付订单')
      fetchOrders() // 刷新列表
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.message || '操作失败')
    }
  }
}

const handleAction = (order) => {
  if (order.status === 'pending') {
    router.push({ path: '/payment', query: { orderId: order.orderId } })
  } else if (order.status === 'active') {
    router.push({ path: '/process/record', query: { orderId: order.orderId } })
  }
}

</script>

<template>
  <div class="order-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">我的订单</h1>
      <p class="page-subtitle">查看和管理所有家教订单</p>
    </div>

    <!-- 标签筛选 -->
    <div class="tab-bar">
      <el-radio-group v-model="activeTab" size="default">
        <el-radio-button 
          v-for="tab in tabs" 
          :key="tab.value" 
          :value="tab.value"
        >
          {{ tab.label }}
        </el-radio-button>
      </el-radio-group>
    </div>

    <!-- 订单列表 -->
    <div class="order-list" v-loading="loading" element-loading-text="加载中...">
      <el-empty 
        v-if="!loading && filteredOrders.length === 0" 
        description="暂无相关订单"
      >
        <el-button type="primary" @click="router.push('/parent/demand')">
          发布需求
        </el-button>
      </el-empty>

      <el-card 
        v-for="item in filteredOrders" 
        :key="item.id"
        class="order-card"
        shadow="hover"
      >
        <!-- 订单头部 -->
        <div class="order-header">
          <span class="order-id">{{ item.id }}</span>
          <el-tag :type="getStatusType(item.status)" size="small">
            {{ item.statusText }}
          </el-tag>
        </div>

        <!-- 订单内容 -->
        <div class="order-body">
          <div class="order-icon">
            <el-icon :size="28"><ShoppingCart /></el-icon>
          </div>
          <div class="order-info">
            <h3 class="order-subject">{{ item.subject }}</h3>
            <p class="order-teacher">教师：{{ item.teacher }}</p>
            <p class="order-date">{{ item.date }}</p>
          </div>
          <div class="order-price">
            <div class="price-value">¥{{ item.amount }}</div>
            <div v-if="item.progress" class="price-progress">{{ item.progress }}</div>
          </div>
        </div>

        <!-- 订单操作 -->
        <div class="order-actions">
          <el-button size="small">查看合同</el-button>
          <el-button 
            v-if="item.status === 'confirm'" 
            type="primary" 
            size="small"
            @click="handleConfirm(item)"
          >
            确认接单
          </el-button>
          <el-button 
            v-if="item.status === 'pending'" 
            type="warning" 
            size="small"
            @click="handleAction(item)"
          >
            去支付
          </el-button>
          <el-button 
            v-if="item.status === 'active'" 
            type="primary" 
            size="small"
            @click="handleAction(item)"
          >
            查看进度
          </el-button>
          <el-button 
            v-if="item.status === 'done'" 
            type="success" 
            size="small" 
            plain
          >
            评价
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.order-list-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: 80px;
}

.page-header {
  background: linear-gradient(135deg, $warning-color 0%, #f97316 100%);
  padding: $spacing-xl $spacing-lg;
  color: #fff;

  .page-title {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 4px;
  }

  .page-subtitle {
    font-size: 14px;
    opacity: 0.9;
  }
}

.tab-bar {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: $shadow-sm;

  :deep(.el-radio-group) {
    width: 100%;
    display: flex;
    
    .el-radio-button {
      flex: 1;
      
      .el-radio-button__inner {
        width: 100%;
      }
    }
  }
}

.order-list {
  padding: $spacing-lg;
  min-height: 300px;
}

.order-card {
  margin-bottom: $spacing-md;
  border-radius: 12px;

  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: $spacing-sm;
    border-bottom: 1px solid $border-light;
    margin-bottom: $spacing-md;

    .order-id {
      font-size: 12px;
      color: $text-muted;
      font-family: monospace;
    }
  }

  .order-body {
    display: flex;
    gap: $spacing-md;
    margin-bottom: $spacing-md;

    .order-icon {
      width: 56px;
      height: 56px;
      background: $bg-light;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: $text-muted;
    }

    .order-info {
      flex: 1;

      .order-subject {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: 4px;
      }

      .order-teacher {
        font-size: 13px;
        color: $text-secondary;
        margin-bottom: 2px;
      }

      .order-date {
        font-size: 12px;
        color: $text-muted;
      }
    }

    .order-price {
      text-align: right;

      .price-value {
        font-size: 20px;
        font-weight: 700;
        color: $text-primary;
      }

      .price-progress {
        font-size: 12px;
        color: $primary-color;
        margin-top: 4px;
      }
    }
  }

  .order-actions {
    display: flex;
    justify-content: flex-end;
    gap: $spacing-sm;
    padding-top: $spacing-sm;
    border-top: 1px solid $border-light;
  }
}
</style>