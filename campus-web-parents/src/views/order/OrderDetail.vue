<template>
  <div class="order-detail-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">订单详情</h1>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>
    
    <template v-else-if="order">
      <!-- 状态卡片 -->
      <div class="status-card" :class="'status-' + order.status">
        <div class="status-icon">
          <el-icon v-if="order.status === 2"><Loading /></el-icon>
          <el-icon v-else-if="order.status === 3"><CircleCheck /></el-icon>
          <el-icon v-else><Clock /></el-icon>
        </div>
        <div class="status-info">
          <div class="status-text">{{ getStatusText(order.status) }}</div>
          <div class="status-desc">{{ getStatusDesc(order.status) }}</div>
        </div>
      </div>
      
      <!-- 教师信息 -->
      <div class="info-card">
        <h3 class="card-title">教师信息</h3>
        <div class="tutor-info" @click="viewTutor">
          <el-avatar :size="56" :src="order.tutorAvatar">
            {{ order.tutorName?.charAt(0) }}
          </el-avatar>
          <div class="info">
            <div class="name">{{ order.tutorName }}</div>
            <div class="school">{{ order.tutorUniversity }} · {{ order.tutorMajor }}</div>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <!-- 订单信息 -->
      <div class="info-card">
        <h3 class="card-title">订单信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单编号" :span="2">
            {{ order.orderNo }}
            <el-button link type="primary" size="small" @click="copyOrderNo">复制</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="辅导科目">
            <el-tag>{{ order.subject }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="学生年级">
            {{ order.grade }}
          </el-descriptions-item>
          <el-descriptions-item label="课时单价">
            <span class="price">¥{{ order.hourlyRate }}/小时</span>
          </el-descriptions-item>
          <el-descriptions-item label="上课频率">
            {{ order.frequency }}
          </el-descriptions-item>
          <el-descriptions-item label="每次时长">
            {{ order.duration }}小时
          </el-descriptions-item>
          <el-descriptions-item label="授课方式">
            {{ order.teachingMode }}
          </el-descriptions-item>
          <el-descriptions-item v-if="order.address" label="上课地址" :span="2">
            {{ order.district }} {{ order.address }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(order.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="order.payTime" label="支付时间">
            {{ formatDate(order.payTime) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 费用明细 -->
      <div class="info-card">
        <h3 class="card-title">费用明细</h3>
        <div class="fee-list">
          <div class="fee-item">
            <span class="label">预估课时费</span>
            <span class="value">¥{{ (order.estimatedAmount || 0).toFixed(2) }}</span>
          </div>
          <div class="fee-item">
            <span class="label">平台服务费</span>
            <span class="value">¥{{ (order.serviceFee || 0).toFixed(2) }}</span>
          </div>
          <div class="fee-item total">
            <span class="label">订单总额</span>
            <span class="value">¥{{ (order.totalAmount || 0).toFixed(2) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 课时记录 -->
      <div v-if="order.status >= 2" class="info-card">
        <div class="card-title-row">
          <h3 class="card-title">课时记录</h3>
          <el-button type="primary" link @click="viewAllLessons">
            查看全部
          </el-button>
        </div>
        
        <div v-if="lessons.length > 0" class="lesson-list">
          <div v-for="lesson in lessons.slice(0, 3)" :key="lesson.id" class="lesson-item">
            <div class="lesson-date">{{ formatDate(lesson.startTime) }}</div>
            <div class="lesson-duration">{{ lesson.duration }}小时</div>
            <el-tag :type="getLessonStatusType(lesson.status)" size="small">
              {{ getLessonStatusText(lesson.status) }}
            </el-tag>
          </div>
        </div>
        <el-empty v-else description="暂无课时记录" :image-size="60" />
      </div>
      
      <!-- 底部操作 -->
      <div class="action-bar">
        <template v-if="order.status === 0">
          <el-button size="large" @click="cancelOrder">取消订单</el-button>
          <el-button size="large" type="primary" @click="confirmOrder">确认订单</el-button>
        </template>
        <template v-else-if="order.status === 1">
          <el-button size="large" @click="cancelOrder">取消订单</el-button>
          <el-button size="large" type="primary" @click="goToPay">立即支付</el-button>
        </template>
        <template v-else-if="order.status === 2">
          <el-button size="large" @click="contactTutor">联系老师</el-button>
          <el-button size="large" type="success" @click="completeOrder">完成订单</el-button>
        </template>
        <template v-else-if="order.status === 3 && !order.reviewed">
          <el-button size="large" type="primary" @click="goToReview">去评价</el-button>
        </template>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Loading, CircleCheck, Clock } from '@element-plus/icons-vue'
import { 
  getOrderDetail, 
  confirmOrder as confirmOrderApi,
  cancelOrder as cancelOrderApi,
  completeOrder as completeOrderApi 
} from '@shared/api/order'
import { getOrderLessons } from '@shared/api/teaching'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const order = ref(null)
const lessons = ref([])

const getStatusText = (status) => {
  const texts = { 0: '待确认', 1: '待支付', 2: '进行中', 3: '已完成', 4: '已取消' }
  return texts[status] || '未知'
}

const getStatusDesc = (status) => {
  const descs = {
    0: '请确认订单信息，确认后进入待支付状态',
    1: '请在24小时内完成支付，超时订单将自动取消',
    2: '订单进行中，老师会按约定时间上课',
    3: '订单已完成，感谢您的使用',
    4: '订单已取消'
  }
  return descs[status] || ''
}

const getLessonStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getLessonStatusText = (status) => {
  const texts = { 0: '待上课', 1: '上课中', 2: '已确认', 3: '申诉中' }
  return texts[status] || '未知'
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const loadOrder = async () => {
  loading.value = true
  try {
    const [orderRes, lessonsRes] = await Promise.all([
      getOrderDetail(route.params.id),
      getOrderLessons(route.params.id).catch(() => ({ data: [] }))
    ])
    
    if (orderRes.code === 200) {
      order.value = orderRes.data
    }
    lessons.value = lessonsRes.data || []
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const viewTutor = () => {
  router.push(`/teachers/${order.value.tutorUserId}`)
}

const copyOrderNo = async () => {
  try {
    await navigator.clipboard.writeText(order.value.orderNo)
    ElMessage.success('复制成功')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const viewAllLessons = () => {
  router.push(`/lessons?orderId=${route.params.id}`)
}

const confirmOrder = async () => {
  try {
    await ElMessageBox.confirm('确认订单后将进入待支付状态', '确认订单')
    const res = await confirmOrderApi(route.params.id)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      loadOrder()
    }
  } catch (e) { /* cancelled */ }
}

const cancelOrder = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单')
    const res = await cancelOrderApi(route.params.id, reason)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      router.back()
    }
  } catch (e) { /* cancelled */ }
}

const goToPay = () => {
  router.push(`/orders/${route.params.id}/pay`)
}

const contactTutor = () => {
  router.push(`/chat/${order.value.tutorUserId}`)
}

const completeOrder = async () => {
  try {
    await ElMessageBox.confirm('确认完成订单？完成后将结算费用给老师', '完成订单')
    const res = await completeOrderApi(route.params.id)
    if (res.code === 200) {
      ElMessage.success('订单已完成')
      loadOrder()
    }
  } catch (e) { /* cancelled */ }
}

const goToReview = () => {
  router.push(`/orders/${route.params.id}/review`)
}

onMounted(() => {
  loadOrder()
})
</script>

<style lang="scss" scoped>
.order-detail-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  padding-bottom: 100px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
  }
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 12px;
}

.status-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 16px;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  &.status-1 {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
  
  &.status-2 {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
  
  &.status-3 {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }
  
  &.status-4 {
    background: linear-gradient(135deg, #a8a8a8 0%, #636363 100%);
  }
  
  .status-icon {
    font-size: 40px;
  }
  
  .status-text {
    font-size: 20px;
    font-weight: 600;
  }
  
  .status-desc {
    font-size: 14px;
    opacity: 0.9;
    margin-top: 4px;
  }
}

.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }
  
  .card-title-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .card-title {
      margin: 0;
    }
  }
}

.tutor-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  
  .info {
    flex: 1;
    
    .name {
      font-size: 16px;
      font-weight: 600;
    }
    
    .school {
      font-size: 13px;
      color: #666;
      margin-top: 4px;
    }
  }
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.fee-list {
  .fee-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    &.total {
      font-weight: 600;
      
      .value {
        font-size: 20px;
        color: #f56c6c;
      }
    }
    
    .label {
      color: #666;
    }
  }
}

.lesson-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lesson-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  
  .lesson-date {
    flex: 1;
    font-weight: 500;
  }
  
  .lesson-duration {
    color: #666;
  }
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: #fff;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  gap: 16px;
  justify-content: center;
  
  .el-button {
    min-width: 140px;
  }
}
</style>
