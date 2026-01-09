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
const isTeacher = computed(() => userStore.isTutor)
const isParent = computed(() => userStore.isParent)

// 当前月份
const now = new Date()
const currentYear = ref(now.getFullYear())
const currentMonthIndex = ref(now.getMonth())
const currentMonth = computed(() => `${currentYear.value}年${currentMonthIndex.value + 1}月`)

// 课时记录列表
const teachingRecords = ref([])

// 获取当月的日历数据（完整一个月）
const calendarDays = computed(() => {
  const year = currentYear.value
  const month = currentMonthIndex.value
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const firstDayWeek = new Date(year, month, 1).getDay() // 0-6, 0=周日
  const today = now.getDate()
  const isCurrentMonth = year === now.getFullYear() && month === now.getMonth()
  
  // 获取本月有课的日期
  const lessonDays = new Map() // day => status (0=待确认, 1=已确认)
  teachingRecords.value.forEach(record => {
    if (record.startTime) {
      const recordDate = new Date(record.startTime)
      if (recordDate.getFullYear() === year && recordDate.getMonth() === month) {
        const day = recordDate.getDate()
        // 保留最高优先级状态
        const existing = lessonDays.get(day)
        if (existing === undefined || record.status === 0) {
          lessonDays.set(day, record.status)
        }
      }
    }
  })
  
  const days = []
  
  // 填充月初空白
  for (let i = 0; i < firstDayWeek; i++) {
    days.push({ day: '', status: 'empty' })
  }
  
  // 填充日期
  for (let i = 1; i <= daysInMonth; i++) {
    let status = 'none'
    if (isCurrentMonth && i === today) {
      status = 'today'
    }
    if (lessonDays.has(i)) {
      status = lessonDays.get(i) === 1 ? 'done' : 'pending'
    }
    days.push({ day: i, status })
  }
  
  return days
})

// 待处理课程列表（状态=0的课时）
const pendingLessons = computed(() => {
  return teachingRecords.value
    .filter(r => r.status === 0)
    .map(r => ({
      id: r.id,
      orderId: r.orderId,
      subject: r.subject || '课程',
      grade: r.grade || '',
      lessonIndex: r.lessonIndex,
      tutorName: r.tutorName || '教师',
      studentName: r.studentName || '学生',
      startTime: r.startTime ? formatDateTime(r.startTime) : '待开始',
      endTime: r.endTime ? formatDateTime(r.endTime) : null,
      hasCheckedIn: !!r.clockInImg,
      clockInImg: r.clockInImg,
      clockInTime: r.startTime ? formatDateTime(r.startTime) : null,
      contentSummary: r.contentSummary,
      homeworkAssigned: r.homeworkAssigned
    }))
})

// 格式化日期时间
const formatDateTime = (dt) => {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getMonth()+1}/${d.getDate()} ${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}`
}

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

// 切换月份
const prevMonth = () => {
  if (currentMonthIndex.value === 0) {
    currentYear.value--
    currentMonthIndex.value = 11
  } else {
    currentMonthIndex.value--
  }
}

const nextMonth = () => {
  if (currentMonthIndex.value === 11) {
    currentYear.value++
    currentMonthIndex.value = 0
  } else {
    currentMonthIndex.value++
  }
}

// 教师打卡
const handleCheckIn = async (lesson) => {
  try {
    // 开发阶段：不强制要求地理位置，使用默认坐标
    const latitude = 39.9042
    const longitude = 116.4074
    
    await checkIn({
      orderId: lesson.orderId,
      latitude,
      longitude,
      photoUrl: 'dev-auto-checkin'
    })
    ElMessage.success('打卡成功！')
    fetchRecords()
  } catch (error) {
    ElMessage.error('打卡失败：' + (error.response?.data?.message || error.message))
  }
}

// 家长确认课时
const handleConfirm = async (lesson) => {
  try {
    await confirmLesson(lesson.id)
    ElMessage.success('确认成功！资金已释放')
    fetchRecords()
  } catch (error) {
    ElMessage.error('确认失败：' + error.message)
  }
}

// 获取日期样式
const getDayClass = (status) => {
  const classMap = {
    done: 'day-done',
    pending: 'day-pending',
    today: 'day-today',
    none: 'day-none',
    empty: 'day-empty'
  }
  return classMap[status] || ''
}

const weekDays = ['日', '一', '二', '三', '四', '五', '六']
</script>

<template>
  <div class="class-record-page" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">{{ isTeacher ? '我的课表' : '孩子课表' }}</h1>
      <p class="page-subtitle">{{ isTeacher ? '管理您的教学安排' : '查看孩子的上课记录' }}</p>
    </div>

    <!-- 日历区域 -->
    <el-card class="calendar-card" shadow="never">
      <template #header>
        <div class="calendar-header">
          <el-button text @click="prevMonth"><el-icon><ArrowLeft /></el-icon></el-button>
          <div class="month-title">
            <el-icon><Calendar /></el-icon>
            <span>{{ currentMonth }}</span>
          </div>
          <el-button text @click="nextMonth"><el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <div class="legend">
          <span class="legend-item"><span class="dot dot-success"></span>已完成</span>
          <span class="legend-item"><span class="dot dot-primary"></span>待确认</span>
          <span class="legend-item"><span class="dot dot-today"></span>今日</span>
        </div>
      </template>

      <!-- 星期标题 -->
      <div class="calendar-week-header">
        <div v-for="w in weekDays" :key="w" class="week-day">{{ w }}</div>
      </div>
      
      <!-- 日期网格 -->
      <div class="calendar-grid">
        <div 
          v-for="(d, i) in calendarDays" 
          :key="i" 
          class="day-cell"
          :class="getDayClass(d.status)"
        >
          {{ d.day }}
        </div>
      </div>
    </el-card>

    <!-- 待处理课程 -->
    <div class="lessons-section">
      <h3 class="section-title">待处理课程 ({{ pendingLessons.length }})</h3>
      
      <el-empty v-if="pendingLessons.length === 0" description="暂无待处理课程">
        <el-button type="primary" @click="router.push('/mine/orders')">查看订单</el-button>
      </el-empty>
      
      <div v-else class="lesson-list">
        <el-card v-for="lesson in pendingLessons" :key="lesson.id" class="lesson-card" shadow="hover">
          <div class="lesson-header">
            <div class="lesson-info">
              <h3 class="lesson-subject">{{ lesson.grade }} {{ lesson.subject }} · 第{{ lesson.lessonIndex }}节</h3>
              <p class="lesson-time"><el-icon><Clock /></el-icon> {{ lesson.startTime }}</p>
            </div>
            <el-tag :type="lesson.hasCheckedIn ? 'success' : 'warning'" size="small">
              {{ lesson.hasCheckedIn ? '已打卡' : '待打卡' }}
            </el-tag>
          </div>
          
          <div class="lesson-detail">
            <span v-if="isTeacher">学生：{{ lesson.studentName }}</span>
            <span v-else>教师：{{ lesson.tutorName }}</span>
          </div>

          <!-- 打卡信息 -->
          <div v-if="lesson.hasCheckedIn" class="checkin-info">
            <el-avatar :size="40" :src="lesson.clockInImg" shape="square" />
            <div class="checkin-text">
              <p class="checkin-title">教师已打卡</p>
              <p class="checkin-desc">{{ lesson.clockInTime }}</p>
            </div>
          </div>

          <div class="lesson-actions">
            <!-- 教师操作 -->
            <template v-if="isTeacher">
              <el-button 
                v-if="!lesson.hasCheckedIn"
                type="primary" 
                @click="handleCheckIn(lesson)"
              >
                <el-icon><Location /></el-icon> 打卡上课
              </el-button>
              <el-tag v-else type="info">等待家长确认</el-tag>
            </template>
            
            <!-- 家长操作 -->
            <template v-else>
              <el-button 
                v-if="lesson.hasCheckedIn"
                type="warning" 
                @click="handleConfirm(lesson)"
              >
                <el-icon><Check /></el-icon> 确认课时
              </el-button>
              <el-tag v-else type="info">等待老师打卡</el-tag>
            </template>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.class-record-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.page-header {
  background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
  padding: 24px 16px;
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

.calendar-card {
  margin: 16px;
  border-radius: 16px;

  .calendar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .month-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 600;
    }
  }

  .legend {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 12px;
    font-size: 12px;
    color: #909399;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      &.dot-success { background: #67c23a; }
      &.dot-primary { background: #409eff; }
      &.dot-today { background: #e6a23c; }
    }
  }
}

.calendar-week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-size: 12px;
  color: #909399;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  padding: 8px 0;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s;

  &.day-done {
    background: rgba(103, 194, 58, 0.2);
    color: #67c23a;
    font-weight: 600;
  }

  &.day-pending {
    background: rgba(64, 158, 255, 0.2);
    color: #409eff;
    font-weight: 600;
  }

  &.day-today {
    background: #e6a23c;
    color: #fff;
    font-weight: 700;
  }

  &.day-none {
    color: #c0c4cc;
  }

  &.day-empty {
    visibility: hidden;
  }
}

.lessons-section {
  padding: 0 16px;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }
}

.lesson-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.lesson-card {
  border-radius: 12px;

  .lesson-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 8px;

    .lesson-subject {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }

    .lesson-time {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #909399;
      margin-top: 4px;
    }
  }

  .lesson-detail {
    font-size: 13px;
    color: #606266;
    margin-bottom: 12px;
  }

  .checkin-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 12px;

    .checkin-title {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }

    .checkin-desc {
      font-size: 12px;
      color: #909399;
    }
  }

  .lesson-actions {
    display: flex;
    gap: 8px;
  }
}
</style>
