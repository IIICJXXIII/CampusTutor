<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/order'
import { getStudents } from '@/api/demand'
import { useOrderStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()

const step = ref(1) // 1: 选时间, 2: 签合同
const loading = ref(false)
const isAgreed = ref(false)

// 教师信息
const teacher = ref({
  id: null,
  tutorProfileId: null, // 教员档案ID
  name: '张老师',
  subject: '初中数学',
  grade: '',
  price: 200
})

// 学生列表和选中的学生
const studentList = ref([])
const selectedStudentId = ref(null)

// 授课方式选项
const teachModeOptions = [
  { value: 1, label: '上门授课' },
  { value: 2, label: '在线授课' }
]
const selectedTeachMode = ref(1)

// 选择的日期和时间
const selectedDate = ref('')
const selectedTime = ref('')

// 生成未来7天的日期
const days = computed(() => {
  const result = []
  const today = new Date()
  const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    result.push({
      day: dayNames[date.getDay()],
      date: `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`,
      fullDate: date,
      available: date.getDay() === 0 || date.getDay() === 6 || i < 3
    })
  }
  return result
})

// 时间段选项
const timeSlots = [
  '09:00 - 10:00',
  '10:30 - 11:30',
  '14:00 - 15:00',
  '19:00 - 20:00'
]

// 协议条款
const agreementTerms = [
  { title: '课时包规则', content: '本次签约为10课时包，每课时60分钟。' },
  { title: '取消政策', content: '开课前2小时可免费取消，否则扣除1课时费用。' },
  { title: '资金安全', content: '全部费用托管于平台，每次课后确认才结算给教师。' },
  { title: '争议处理', content: '如发生纠纷，平台将在24小时内介入协调。' }
]

// 初始化教师信息和学生列表
onMounted(async () => {
  // 从路由参数获取教员信息
  if (route.query.teacherId) teacher.value.id = route.query.teacherId
  if (route.query.tutorProfileId) teacher.value.tutorProfileId = route.query.tutorProfileId
  if (route.query.teacherName) teacher.value.name = route.query.teacherName
  if (route.query.subject) teacher.value.subject = route.query.subject
  if (route.query.grade) teacher.value.grade = route.query.grade
  if (route.query.price) teacher.value.price = parseInt(route.query.price) || 200
  
  // 获取学生列表
  try {
    const res = await getStudents()
    if (res.data && res.data.length > 0) {
      studentList.value = res.data
      selectedStudentId.value = res.data[0].id // 默认选中第一个
    }
  } catch (error) {
    console.error('获取学生列表失败:', error)
  }
})

// 下一步
const handleNext = () => {
  if (!selectedStudentId.value) {
    ElMessage.warning('请选择学生')
    return
  }
  if (!selectedDate.value || !selectedTime.value) {
    ElMessage.warning('请选择日期和时间段')
    return
  }
  step.value = 2
}

// 签约
const handleSign = async () => {
  if (!isAgreed.value) {
    ElMessage.warning('请先阅读并同意协议')
    return
  }

  loading.value = true
  try {
    const totalHours = 10
    const totalAmount = teacher.value.price * totalHours
    
    // 获取选中的学生信息
    const selectedStudent = studentList.value.find(s => s.id === selectedStudentId.value)
    const grade = selectedStudent?.grade || teacher.value.grade || '初一'
    
    // 构建符合后端 CreateOrderRequest 的请求体
    const orderData = {
      studentId: selectedStudentId.value,
      tutorProfileId: parseInt(teacher.value.tutorProfileId || teacher.value.id),
      demandId: route.query.demandId ? parseInt(route.query.demandId) : null,
      subject: teacher.value.subject,
      grade: grade,
      teachMode: selectedTeachMode.value,
      unitPrice: teacher.value.price,
      totalHours: totalHours,
      remark: `首课时间: ${selectedDate.value} ${selectedTime.value}`
    }

    let orderId
    try {
      const res = await createOrder(orderData)
      orderId = res.data
      ElMessage.success('订单创建成功！请前往支付')
    } catch (error) {
      console.error('创建订单失败:', error)
      ElMessage.error(error.response?.data?.message || '创建订单失败')
      loading.value = false
      return
    }

    // 添加到本地store
    orderStore.addOrder({
      id: orderId,
      teacher: teacher.value.name,
      subject: `${teacher.value.subject} · ${totalHours}课时包`,
      amount: totalAmount,
      status: 'pending',
      date: new Date().toLocaleString(),
      tags: ['待支付']
    })

    router.push({ path: '/payment', query: { orderId } })
  } catch (error) {
    ElMessage.error(error.message || '签约失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="booking-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button 
        text 
        @click="step === 1 ? router.back() : step = 1"
        class="back-btn"
      >
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">{{ step === 1 ? '预约试课' : '三方协议签署' }}</h1>
    </div>

    <!-- Step 1: 选择时间 -->
    <div v-if="step === 1" class="step-content">
      <!-- 教师信息卡片 -->
      <el-card class="teacher-card" shadow="never">
        <div class="teacher-info">
          <div class="info-left">
            <h3 class="teacher-name">{{ teacher.name }}</h3>
            <p class="teacher-subject">{{ teacher.subject }} · 试课申请</p>
          </div>
          <span class="teacher-price">¥{{ teacher.price }}</span>
        </div>
      </el-card>

      <!-- 选择学生 -->
      <el-card class="select-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>选择学生</span>
          </div>
        </template>
        <el-select 
          v-model="selectedStudentId" 
          placeholder="请选择学生"
          style="width: 100%"
          v-if="studentList.length > 0"
        >
          <el-option 
            v-for="student in studentList" 
            :key="student.id" 
            :label="`${student.studentName} (${student.grade})`"
            :value="student.id"
          />
        </el-select>
        <el-empty v-else description="暂无学生信息，请先添加学生" :image-size="60">
          <el-button type="primary" size="small" @click="router.push('/parent/profile')">
            去添加学生
          </el-button>
        </el-empty>
      </el-card>

      <!-- 选择授课方式 -->
      <el-card class="select-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Location /></el-icon>
            <span>授课方式</span>
          </div>
        </template>
        <el-radio-group v-model="selectedTeachMode" style="width: 100%">
          <el-radio-button 
            v-for="mode in teachModeOptions" 
            :key="mode.value" 
            :value="mode.value"
            style="flex: 1"
          >
            {{ mode.label }}
          </el-radio-button>
        </el-radio-group>
      </el-card>

      <!-- 选择日期 -->
      <el-card class="select-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Calendar /></el-icon>
            <span>选择日期</span>
            <el-tag type="warning" size="small">可授课日标黄</el-tag>
          </div>
        </template>

        <div class="date-grid">
          <div 
            v-for="d in days" 
            :key="d.date"
            class="date-item"
            :class="{
              'available': d.available,
              'selected': selectedDate === d.date,
              'disabled': !d.available
            }"
            @click="d.available && (selectedDate = d.date)"
          >
            <span class="day-name">{{ d.day }}</span>
            <span class="day-date">{{ d.date }}</span>
          </div>
        </div>
      </el-card>

      <!-- 选择时段 -->
      <el-card class="select-card" shadow="never">
        <template #header>
          <div class="card-header">
            <el-icon><Clock /></el-icon>
            <span>选择时段</span>
          </div>
        </template>

        <div class="time-grid">
          <div 
            v-for="time in timeSlots" 
            :key="time"
            class="time-item"
            :class="{ 'selected': selectedTime === time }"
            @click="selectedTime = time"
          >
            {{ time }}
          </div>
        </div>
      </el-card>

      <!-- 底部按钮 -->
      <div class="bottom-action">
        <el-button 
          type="primary" 
          size="large"
          :disabled="!selectedDate || !selectedTime"
          @click="handleNext"
        >
          下一步：确认协议
        </el-button>
      </div>
    </div>

    <!-- Step 2: 签署协议 -->
    <div v-else class="step-content" v-loading="loading">
      <!-- 协议头部 -->
      <div class="agreement-header">
        <el-icon :size="48" color="#67c23a"><CircleCheck /></el-icon>
        <h2>易家教平台授课协议</h2>
        <p>为了保障您的权益，请仔细阅读以下条款</p>
      </div>

      <!-- 协议内容 -->
      <el-card class="agreement-card" shadow="never">
        <el-collapse accordion>
          <el-collapse-item 
            v-for="(term, index) in agreementTerms" 
            :key="index" 
            :title="term.title"
            :name="index"
          >
            <p>{{ term.content }}</p>
          </el-collapse-item>
        </el-collapse>
      </el-card>

      <!-- 订单摘要 -->
      <el-card class="summary-card" shadow="never">
        <h4 class="summary-title">订单摘要</h4>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="授课教师">{{ teacher.name }}</el-descriptions-item>
          <el-descriptions-item label="课程科目">{{ teacher.subject }}</el-descriptions-item>
          <el-descriptions-item label="首课时间">{{ selectedDate }} {{ selectedTime }}</el-descriptions-item>
          <el-descriptions-item label="课时包">10课时</el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ teacher.price }}/课时</el-descriptions-item>
          <el-descriptions-item label="总计">
            <span class="total-amount">¥{{ teacher.price * 10 }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 同意协议 -->
      <div class="agree-section">
        <el-checkbox v-model="isAgreed">
          我已阅读并同意
          <el-link type="primary">《易家教服务协议》</el-link>
        </el-checkbox>
      </div>

      <!-- 签约按钮 -->
      <div class="bottom-action">
        <el-button 
          type="primary" 
          size="large"
          :disabled="!isAgreed"
          :loading="loading"
          @click="handleSign"
        >
          <el-icon class="mr-1"><Check /></el-icon>
          确认签约并支付
        </el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.booking-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: 100px;
}

.page-header {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  display: flex;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid $border-color;

  .back-btn {
    margin-right: $spacing-sm;
  }

  .page-title {
    flex: 1;
    text-align: center;
    font-size: 18px;
    font-weight: 600;
    padding-right: 32px;
  }
}

.step-content {
  padding: $spacing-lg;
}

.teacher-card {
  border-radius: 12px;
  margin-bottom: $spacing-lg;

  .teacher-info {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .info-left {
      .teacher-name {
        font-size: 18px;
        font-weight: 600;
        color: $text-primary;
      }

      .teacher-subject {
        font-size: 14px;
        color: $text-muted;
        margin-top: 4px;
      }
    }

    .teacher-price {
      font-size: 24px;
      font-weight: 700;
      color: $warning-color;
    }
  }
}

.select-card {
  border-radius: 12px;
  margin-bottom: $spacing-lg;

  .card-header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-weight: 600;
  }
}

.date-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: $spacing-sm;

  .date-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: $spacing-md;
    border-radius: 12px;
    border: 2px solid transparent;
    cursor: pointer;
    transition: all 0.3s;

    &.available {
      background: rgba($warning-color, 0.05);
      border-color: rgba($warning-color, 0.1);
    }

    &.selected {
      background: rgba($warning-color, 0.1);
      border-color: $warning-color;
    }

    &.disabled {
      background: $bg-light;
      color: $text-muted;
      cursor: not-allowed;
    }

    .day-name {
      font-size: 12px;
      color: $text-muted;
    }

    .day-date {
      font-size: 14px;
      font-weight: 600;
      margin-top: 4px;
    }
  }
}

.time-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-md;

  .time-item {
    padding: $spacing-md;
    border: 1px solid $border-color;
    border-radius: 12px;
    text-align: center;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: $primary-color;
    }

    &.selected {
      background: $primary-color;
      border-color: $primary-color;
      color: #fff;
    }
  }
}

.agreement-header {
  text-align: center;
  padding: $spacing-xl 0;

  h2 {
    font-size: 20px;
    font-weight: 600;
    margin: $spacing-md 0 $spacing-xs;
  }

  p {
    font-size: 12px;
    color: $text-muted;
  }
}

.agreement-card {
  border-radius: 12px;
  margin-bottom: $spacing-lg;
}

.summary-card {
  border-radius: 12px;
  margin-bottom: $spacing-lg;

  .summary-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: $spacing-md;
  }

  .total-amount {
    font-size: 18px;
    font-weight: 700;
    color: $danger-color;
  }
}

.agree-section {
  padding: $spacing-md 0;
}

.bottom-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid $border-color;

  .el-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
  }
}
</style>
