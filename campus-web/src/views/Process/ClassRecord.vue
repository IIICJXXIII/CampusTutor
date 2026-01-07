<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import { getMyTeachingRecords, checkIn, confirmLesson } from '@/api/teaching'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

// 用户角色
const userRole = computed(() => userStore.userRole || 'parent')

// 当前月份
const now = new Date()
const currentMonth = ref(`${now.getFullYear()}年${now.getMonth() + 1}月`)

// 课时记录列表
const teachingRecords = ref([])

// 日历数据 - 根据真实数据生成
const calendarDays = computed(() => {
  const days = []
  const today = now.getDate()
  const daysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate()
  
  // 获取本月有课的日期
  const lessonDays = new Set()
  const confirmedDays = new Set()
  teachingRecords.value.forEach(record => {
    if (record.startTime) {
      const recordDate = new Date(record.startTime)
      if (recordDate.getMonth() === now.getMonth()) {
        const day = recordDate.getDate()
        if (record.status === 1) {
          confirmedDays.add(day)
        } else {
          lessonDays.add(day)
        }
      }
    }
  })
  
  for (let i = 1; i <= Math.min(daysInMonth, 7); i++) {
    let status = 'none'
    if (i === today) {
      status = 'today'
    } else if (confirmedDays.has(i)) {
      status = 'done'
    } else if (lessonDays.has(i)) {
      status = 'pending'
    }
    days.push({ day: i, status })
  }
  return days
})

// 状态配置
const statusConfig = {
  done: { type: 'success', label: '已完成' },
  pending: { type: 'primary', label: '待上课' },
  cancel: { type: 'info', label: '已取消' },
  today: { type: 'warning', label: '今日' },
  none: { type: '', label: '' }
}

// 今日课程数据 - 从真实数据中获取最近的课程
const todayClass = computed(() => {
  if (teachingRecords.value.length === 0) {
    return null
  }
  // 找到最近一条未确认的课时记录
  const pendingRecord = teachingRecords.value.find(r => r.status === 0)
  if (pendingRecord) {
    return {
      id: pendingRecord.id,
      orderId: pendingRecord.orderId,
      subject: `第${pendingRecord.lessonIndex}节课`,
      time: pendingRecord.startTime ? new Date(pendingRecord.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : '待开始',
      location: '上课地点',
      status: pendingRecord.endTime ? 'checkin' : 'ready',
      checkinImg: pendingRecord.clockInImg,
      checkinTime: pendingRecord.startTime ? new Date(pendingRecord.startTime).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : null,
      checkinLocation: pendingRecord.clockInLat ? 'GPS已核验' : null,
      contentSummary: pendingRecord.contentSummary,
      homeworkAssigned: pendingRecord.homeworkAssigned
    }
  }
  return null
})

// 获取我的课时记录
const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getMyTeachingRecords()
    teachingRecords.value = res.data || []
  } catch (error) {
    console.error('获取课时记录失败:', error)
    teachingRecords.value = []
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchRecords()
})

// 教师打卡
const handleCheckIn = async () => {
  ElMessage.info('正在获取位置信息...')
  
  // 获取GPS位置
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        try {
          // 这里需要orderId，暂时使用最近一个待上课的订单
          // 实际场景应该从订单列表选择
          ElMessage.warning('请从订单列表中选择要打卡的课程')
        } catch (error) {
          ElMessage.error('打卡失败：' + error.message)
        }
      },
      () => {
        ElMessage.error('无法获取位置信息，请检查GPS权限')
      }
    )
  } else {
    ElMessage.error('浏览器不支持定位功能')
  }
}

// 家长确认
const handleConfirm = async () => {
  if (!todayClass.value?.id) {
    ElMessage.warning('没有待确认的课时')
    return
  }
  
  try {
    await confirmLesson(todayClass.value.id)
    ElMessage.success('确认成功！资金已释放给老师')
    fetchRecords() // 刷新数据
  } catch (error) {
    ElMessage.error('确认失败：' + error.message)
  }
}

// 获取日期样式
const getDayClass = (status) => {
  const classMap = {
    done: 'day-done',
    pending: 'day-pending',
    cancel: 'day-cancel',
    today: 'day-today',
    none: 'day-none'
  }
  return classMap[status] || ''
}
</script>

<template>
  <div class="class-record-page" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">我的课表</h1>
    </div>

    <!-- 日历区域 -->
    <el-card class="calendar-card" shadow="never">
      <template #header>
        <div class="calendar-header">
          <div class="month-title">
            <el-icon><Calendar /></el-icon>
            <span>{{ currentMonth }}</span>
          </div>
          <div class="legend">
            <span class="legend-item">
              <span class="dot dot-success"></span>
              完成
            </span>
            <span class="legend-item">
              <span class="dot dot-primary"></span>
              待上
            </span>
          </div>
        </div>
      </template>

      <div class="calendar-days">
        <div 
          v-for="d in calendarDays" 
          :key="d.day" 
          class="day-item"
          :class="getDayClass(d.status)"
        >
          {{ d.day }}
        </div>
      </div>
    </el-card>

    <!-- 今日课程 -->
    <div class="today-section">
      <h3 class="section-title">待处理课程</h3>
      
      <!-- 无待处理课程 -->
      <el-empty v-if="!todayClass" description="暂无待处理课程">
        <el-button type="primary" @click="router.push('/mine/orders')">查看订单</el-button>
      </el-empty>
      
      <el-card v-else class="lesson-card" shadow="hover">
        <!-- 课程头部 -->
        <div class="lesson-header">
          <div class="lesson-info">
            <h2 class="lesson-subject">{{ todayClass.subject }}</h2>
            <p class="lesson-time">
              <el-icon><Clock /></el-icon>
              {{ todayClass.time }}
            </p>
          </div>
          <el-tag :type="todayClass.status === 'confirmed' ? 'success' : 'primary'">
            {{ todayClass.status === 'confirmed' ? '已结算' : '进行中' }}
          </el-tag>
        </div>

        <!-- 课程详情 -->
        <div class="lesson-detail">
          <div class="detail-item">
            <el-icon><Location /></el-icon>
            <span>{{ todayClass.location }}</span>
          </div>
          
          <div class="detail-item">
            <el-icon><User /></el-icon>
            <span>{{ userRole === 'teacher' ? `学生: ${todayClass.student}` : `教师: ${todayClass.teacher}` }}</span>
          </div>
        </div>

        <!-- 打卡信息 -->
        <div v-if="todayClass.status !== 'ready'" class="checkin-info">
          <div class="checkin-image">
            <el-avatar :size="48" :src="todayClass.checkinImg" shape="square" />
          </div>
          <div class="checkin-text">
            <p class="checkin-title">教师已打卡</p>
            <p class="checkin-desc">
              {{ todayClass.checkinTime }} 打卡于 {{ todayClass.checkinLocation }}
            </p>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="lesson-actions">
          <!-- 教师视角 -->
          <template v-if="userRole === 'teacher'">
            <el-button 
              v-if="todayClass.status === 'ready'"
              type="primary" 
              size="large"
              class="action-btn"
              @click="handleCheckIn"
            >
              <el-icon class="mr-1"><Location /></el-icon>
              上课打卡
            </el-button>
            <el-button 
              v-else 
              disabled 
              size="large"
              class="action-btn"
            >
              已完成打卡
            </el-button>
          </template>

          <!-- 家长视角 -->
          <template v-else>
            <el-button 
              v-if="todayClass.status === 'checkin'"
              type="warning" 
              size="large"
              class="action-btn"
              @click="handleConfirm"
            >
              <el-icon class="mr-1"><CircleCheck /></el-icon>
              确认课时 (支付结算)
            </el-button>
            <el-result 
              v-else-if="todayClass.status === 'confirmed'"
              icon="success"
              title="已确认，资金已释放"
              class="result-mini"
            />
            <el-text v-else type="info" class="waiting-text">
              等待老师打卡...
            </el-text>
          </template>
        </div>
      </el-card>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.class-record-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: $spacing-xl;
}

.page-header {
  background: linear-gradient(135deg, $primary-color 0%, #667eea 100%);
  padding: $spacing-xl $spacing-lg;
  color: #fff;

  .page-title {
    font-size: 24px;
    font-weight: 700;
  }
}

.calendar-card {
  margin: $spacing-lg;
  border-radius: 16px;

  .calendar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .month-title {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      font-size: 18px;
      font-weight: 600;
    }

    .legend {
      display: flex;
      gap: $spacing-md;
      font-size: 12px;
      color: $text-muted;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 4px;
      }

      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;

        &.dot-success { background: $success-color; }
        &.dot-primary { background: $primary-color; }
      }
    }
  }

  .calendar-days {
    display: flex;
    justify-content: space-between;

    .day-item {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      font-weight: 600;
      border: 2px solid transparent;
      transition: all 0.3s;

      &.day-done {
        background: rgba($success-color, 0.1);
        color: $success-color;
        border-color: rgba($success-color, 0.2);
      }

      &.day-pending {
        background: rgba($primary-color, 0.1);
        color: $primary-color;
        border-color: rgba($primary-color, 0.2);
      }

      &.day-cancel {
        background: $bg-light;
        color: $text-muted;
        border-color: $border-color;
      }

      &.day-today {
        background: $warning-color;
        color: #fff;
        box-shadow: $shadow-md;
      }

      &.day-none {
        color: #d1d5db;
      }
    }
  }
}

.today-section {
  padding: 0 $spacing-lg;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: $text-muted;
    margin-bottom: $spacing-md;
  }
}

.lesson-card {
  border-radius: 16px;

  .lesson-header {
    background: linear-gradient(135deg, $primary-color 0%, #667eea 100%);
    margin: -20px -20px $spacing-lg -20px;
    padding: $spacing-lg;
    border-radius: 16px 16px 0 0;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    color: #fff;

    .lesson-info {
      .lesson-subject {
        font-size: 20px;
        font-weight: 700;
        margin-bottom: 4px;
      }

      .lesson-time {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 14px;
        opacity: 0.9;
      }
    }

    :deep(.el-tag) {
      background: rgba(255, 255, 255, 0.2);
      border: none;
      color: #fff;
    }
  }

  .lesson-detail {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
    margin-bottom: $spacing-lg;

    .detail-item {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      color: $text-secondary;
      font-size: 14px;
    }
  }

  .checkin-info {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    padding: $spacing-md;
    background: $bg-light;
    border-radius: 12px;
    border: 1px dashed $border-color;
    margin-bottom: $spacing-lg;

    .checkin-text {
      .checkin-title {
        font-size: 14px;
        font-weight: 600;
        color: $text-primary;
      }

      .checkin-desc {
        font-size: 12px;
        color: $text-muted;
        margin-top: 2px;
      }
    }
  }

  .lesson-actions {
    .action-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 12px;
    }

    .result-mini {
      padding: $spacing-sm 0;

      :deep(.el-result__title) {
        font-size: 14px;
        margin-top: $spacing-xs;
      }
    }

    .waiting-text {
      display: block;
      text-align: center;
      padding: $spacing-md;
    }
  }
}
</style>
