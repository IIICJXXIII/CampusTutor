<template>
  <div class="order-list-page">
    <div class="page-header">
      <h1 class="page-title">我的订单</h1>
    </div>
    
    <!-- 状态筛选 -->
    <div class="filter-tabs">
      <el-radio-group v-model="statusFilter" @change="handleFilterChange">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="-1">待确认</el-radio-button>
        <el-radio-button value="0">待支付</el-radio-button>
        <el-radio-button value="1">待开课</el-radio-button>
        <el-radio-button value="2">进行中</el-radio-button>
        <el-radio-button value="3">已完成</el-radio-button>
        <el-radio-button value="4">已取消</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 订单列表 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>
    
    <div v-else-if="orders.length === 0" class="empty-container">
      <el-empty description="暂无订单">
        <el-button type="primary" @click="goFindTeacher">去找老师</el-button>
      </el-empty>
    </div>
    
    <div v-else class="order-list">
      <div
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        @click="viewDetail(order.id)"
      >
        <div class="order-header">
          <span class="order-no">订单号: {{ order.orderNo }}</span>
          <el-tag :type="getStatusType(order.status)" size="small">
            {{ getStatusText(order.status) }}
          </el-tag>
        </div>
        
        <div class="order-content">
          <div class="tutor-info">
            <el-avatar :size="48" :src="order.tutorAvatar">
              {{ order.tutorName?.charAt(0) }}
            </el-avatar>
            <div class="info">
              <div class="tutor-name">{{ order.tutorName }}</div>
              <div class="subject">{{ order.subject }} · {{ order.grade }}</div>
            </div>
          </div>
          
          <div class="order-info">
            <div class="info-row">
              <span class="label">课时单价</span>
              <span class="value">¥{{ order.unitPrice }}/小时</span>
            </div>
            <div class="info-row">
              <span class="label">总课时</span>
              <span class="value">{{ order.totalHours }}</span>
            </div>
            <div class="info-row">
              <span class="label">授课方式</span>
              <span class="value">{{ order.teachMode === 1 ? '线下上门' : (order.teachMode === 2 ? '线上网课' : '不限') }}</span>
            </div>
          </div>
        </div>
        
        <div class="order-footer">
          <div class="total-price">
            <span class="label">订单金额</span>
            <span class="price">¥{{ order.totalAmount?.toFixed(2) }}</span>
          </div>
          
          <div class="order-actions" @click.stop>
            <template v-if="order.status === -1">
              <el-button size="small" @click="cancelOrder(order)">取消</el-button>
              <el-button v-if="order.demandId" size="small" type="primary" @click="confirmOrder(order)">确认订单</el-button>
              <el-button v-else size="small" type="info" disabled>等待教师确认</el-button>
            </template>
            <template v-else-if="order.status === 0">
              <el-button size="small" @click="cancelOrder(order)">取消</el-button>
              <el-button size="small" type="primary" @click="payOrder(order)">去支付</el-button>
            </template>
            <template v-else-if="order.status === 1">
              <el-button size="small" @click="contactTutor(order)">联系老师</el-button>
              <el-button size="small" type="info" disabled>等待教师开课</el-button>
            </template>
            <template v-else-if="order.status === 2">
              <el-button size="small" @click="viewLessons(order.id)">查看课时</el-button>
              <el-button size="small" type="success" @click="completeOrder(order)">完成订单</el-button>
            </template>
            <template v-else-if="order.status === 3">
              <el-button size="small" @click="viewLessons(order.id)">查看课时</el-button>
              <el-button size="small" type="primary" @click="reviewOrder(order)">去评价</el-button>
            </template>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination-container">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getParentOrders, 
  confirmOrder as confirmOrderApi,
  cancelOrder as cancelOrderApi,
  completeOrder as completeOrderApi 
} from '@shared/api/order'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getStatusType = (status) => {
  const types = { '-1': 'warning', 0: 'danger', 1: 'success', 2: 'primary', 3: 'success', 4: 'info' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { '-1': '待确认', 0: '待支付', 1: '待开课', 2: '进行中', 3: '已完成', 4: '已取消' }
  return texts[status] || '未知'
}

const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value
    }
    if (statusFilter.value !== 'all') {
      params.status = statusFilter.value
    }
    
    const res = await getParentOrders(params)
    if (res.code === 200) {
      orders.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  page.value = 1
  loadOrders()
}

const goFindTeacher = () => {
  router.push('/parent/home')
}

const viewDetail = (id) => {
  router.push(`/parent/orders/${id}`)
}

const viewLessons = (orderId) => {
  router.push(`/parent/lessons?orderId=${orderId}`)
}

const confirmOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确认订单后将进入待支付状态，是否确认？', '确认订单')
    const res = await confirmOrderApi(order.id)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      loadOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认订单失败:', error)
    }
  }
}

const cancelOrder = async (order) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确定取消',
      cancelButtonText: '返回',
      inputPlaceholder: '请输入取消原因'
    })
    const res = await cancelOrderApi(order.id, reason)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      loadOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
}

const payOrder = (order) => {
  router.push(`/parent/orders/${order.id}/pay`)
}

const completeOrder = async (order) => {
  try {
    await ElMessageBox.confirm('确认订单已完成所有课时？完成后将结算费用给老师。', '完成订单')
    const res = await completeOrderApi(order.id)
    if (res.code === 200) {
      ElMessage.success('订单已完成')
      loadOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成订单失败:', error)
    }
  }
}

const reviewOrder = (order) => {
  router.push(`/parent/orders/${order.id}/review`)
}

const contactTutor = (order) => {
  router.push(`/chat/${order.tutorId}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<style lang="scss" scoped>
.order-list-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.filter-tabs {
  margin-bottom: 20px;
  overflow-x: auto;
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #f5f7fa;
  
  .order-no {
    font-size: 13px;
    color: #666;
  }
}

.order-content {
  padding: 20px;
  display: flex;
  gap: 24px;
  
  .tutor-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .tutor-name {
      font-size: 16px;
      font-weight: 600;
    }
    
    .subject {
      font-size: 13px;
      color: #666;
      margin-top: 4px;
    }
  }
  
  .order-info {
    flex: 1;
    display: flex;
    gap: 24px;
    
    .info-row {
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .label {
        font-size: 12px;
        color: #999;
      }
      
      .value {
        font-size: 14px;
        color: #333;
      }
    }
  }
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  
  .total-price {
    .label {
      font-size: 13px;
      color: #666;
      margin-right: 8px;
    }
    
    .price {
      font-size: 20px;
      font-weight: 600;
      color: #f56c6c;
    }
  }
  
  .order-actions {
    display: flex;
    gap: 8px;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
