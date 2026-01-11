<template>
  <div v-loading="loading" class="order-detail">
    <el-page-header @back="goBack">
      <template #content>订单详情</template>
    </el-page-header>
    
    <div v-if="order" class="detail-container">
      <!-- 订单状态 -->
      <div class="status-section">
        <div class="status-icon" :class="`status-${order.status}`">
          <el-icon :size="32">
            <component :is="getStatusIcon(order.status)" />
          </el-icon>
        </div>
        <div class="status-info">
          <h2>{{ getStatusText(order.status) }}</h2>
          <p>{{ getStatusDesc(order.status) }}</p>
        </div>
      </div>
      
      <!-- 学生信息 -->
      <div class="info-card">
        <div class="card-header">
          <h3>学生信息</h3>
        </div>
        <div class="card-body">
          <div class="student-row">
            <el-avatar :size="56" :src="order.studentAvatar">
              {{ order.studentName?.charAt(0) }}
            </el-avatar>
            <div class="student-info">
              <h4>{{ order.studentName }}</h4>
              <p>{{ order.grade }} · {{ order.subject }}</p>
            </div>
            <el-button type="primary" link @click="goToChat">
              <el-icon><ChatDotRound /></el-icon>联系家长
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 订单信息 -->
      <div class="info-card">
        <div class="card-header">
          <h3>订单信息</h3>
        </div>
        <div class="card-body">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatTime(order.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="课时费">¥{{ order.hourlyRate }}/小时</el-descriptions-item>
            <el-descriptions-item label="约定课时">{{ order.totalHours }}小时</el-descriptions-item>
            <el-descriptions-item label="订单总额">
              <span class="price">¥{{ order.totalAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="已完成课时">{{ order.completedHours || 0 }}小时</el-descriptions-item>
            <el-descriptions-item label="上课地点" :span="2">{{ order.address || '待定' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ order.remark || '无' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <!-- 课程记录 -->
      <div class="info-card">
        <div class="card-header">
          <h3>课程记录</h3>
          <el-button v-if="order.status === 2" type="primary" link @click="goToLessons">
            查看全部
          </el-button>
        </div>
        <div class="card-body">
          <div v-if="lessons.length" class="lesson-list">
            <div v-for="lesson in lessons.slice(0, 3)" :key="lesson.id" class="lesson-item">
              <div class="lesson-date">
                <span class="day">{{ formatDay(lesson.lessonDate) }}</span>
                <span class="month">{{ formatMonth(lesson.lessonDate) }}</span>
              </div>
              <div class="lesson-info">
                <p class="time">{{ lesson.startTime }} - {{ lesson.endTime }}</p>
                <p class="duration">{{ lesson.duration }}小时</p>
              </div>
              <el-tag :type="getLessonStatusType(lesson.status)" size="small">
                {{ getLessonStatusText(lesson.status) }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无课程记录" :image-size="60" />
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button v-if="order.status === 1" type="primary" size="large" @click="confirmOrder">
          确认接单
        </el-button>
        <el-button v-if="order.status === 2" type="success" size="large" @click="goToCheckin">
          开始上课
        </el-button>
        <el-button v-if="[1, 2].includes(order.status)" size="large" @click="cancelOrder">
          取消订单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Check, Close, ChatDotRound } from '@element-plus/icons-vue'
import { getOrderDetail, confirmOrder as confirmOrderApi, cancelOrder as cancelOrderApi } from '@shared/api/order'
import { getOrderLessons } from '@shared/api/teaching'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const order = ref(null)
const lessons = ref([])

const getStatusIcon = (status) => {
  const map = { 1: Clock, 2: Clock, 3: Check, 4: Close }
  return map[status] || Clock
}

const getStatusText = (status) => {
  const map = { 1: '待确认', 2: '进行中', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

const getStatusDesc = (status) => {
  const map = {
    1: '家长已创建订单，请确认是否接单',
    2: '订单进行中，请按时上课',
    3: '订单已完成，感谢您的付出',
    4: '订单已取消'
  }
  return map[status] || ''
}

const getLessonStatusType = (status) => {
  const map = { 1: 'info', 2: 'warning', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}

const getLessonStatusText = (status) => {
  const map = { 1: '待上课', 2: '上课中', 3: '已完成', 4: '有争议' }
  return map[status] || '未知'
}

const formatTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm')
const formatDay = (date) => dayjs(date).format('DD')
const formatMonth = (date) => dayjs(date).format('M月')

const loadOrder = async () => {
  const orderId = route.params.id
  loading.value = true
  
  try {
    const res = await getOrderDetail(orderId)
    if (res.code === 200) {
      order.value = res.data
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadLessons = async () => {
  const orderId = route.params.id
  try {
    const res = await getOrderLessons(orderId)
    if (res.code === 200) {
      lessons.value = res.data || []
    }
  } catch (error) {
    console.error('加载课程失败', error)
  }
}

const confirmOrder = async () => {
  try {
    await ElMessageBox.confirm('确认接受此订单吗？', '确认接单')
    const res = await confirmOrderApi(order.value.id)
    if (res.code === 200) {
      ElMessage.success('接单成功')
      loadOrder()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const cancelOrder = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      inputPlaceholder: '请输入取消原因'
    })
    
    const res = await cancelOrderApi(order.value.id, { reason: value })
    if (res.code === 200) {
      ElMessage.success('已取消')
      loadOrder()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const goToChat = () => {
  router.push(`/chat/${order.value.parentId}`)
}

const goToLessons = () => {
  router.push(`/lessons?orderId=${order.value.id}`)
}

const goToCheckin = () => {
  router.push(`/checkin?orderId=${order.value.id}`)
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadOrder()
  loadLessons()
})
</script>

<style lang="scss" scoped>
.order-detail {
  max-width: 800px;
  margin: 0 auto;
  
  .detail-container {
    margin-top: 24px;
  }
  
  .status-section {
    display: flex;
    align-items: center;
    gap: 16px;
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 16px;
    
    .status-icon {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      
      &.status-1 { background: #e6a23c; }
      &.status-2 { background: #409eff; }
      &.status-3 { background: #67c23a; }
      &.status-4 { background: #909399; }
    }
    
    .status-info {
      h2 {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 4px;
      }
      
      p {
        font-size: 14px;
        color: #606266;
      }
    }
  }
  
  .info-card {
    background: #fff;
    border-radius: 12px;
    margin-bottom: 16px;
    overflow: hidden;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px;
      border-bottom: 1px solid #ebeef5;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
      }
    }
    
    .card-body {
      padding: 24px;
    }
  }
  
  .student-row {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .student-info {
      flex: 1;
      
      h4 {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 4px;
      }
      
      p {
        font-size: 14px;
        color: #606266;
      }
    }
  }
  
  .price {
    color: #f56c6c;
    font-weight: 600;
  }
  
  .lesson-list {
    .lesson-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .lesson-date {
        width: 48px;
        text-align: center;
        
        .day {
          display: block;
          font-size: 20px;
          font-weight: 600;
        }
        
        .month {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .lesson-info {
        flex: 1;
        margin-left: 16px;
        
        .time {
          font-size: 14px;
          font-weight: 500;
        }
        
        .duration {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
  
  .action-bar {
    display: flex;
    justify-content: center;
    gap: 16px;
    padding: 24px 0;
  }
}
</style>
