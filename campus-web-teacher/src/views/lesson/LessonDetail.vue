<template>
  <div v-loading="loading" class="lesson-detail">
    <el-page-header @back="goBack">
      <template #content>课程详情</template>
    </el-page-header>
    
    <div v-if="lesson" class="detail-container">
      <!-- 课程状态 -->
      <div class="status-banner" :class="`status-${lesson.status}`">
        <div class="status-content">
          <h2>{{ getStatusText(lesson.status) }}</h2>
          <p>{{ formatDate(lesson.lessonDate) }} {{ lesson.startTime }} - {{ lesson.endTime }}</p>
        </div>
      </div>
      
      <!-- 学生信息 -->
      <div class="info-card">
        <div class="card-header">
          <h3>学生信息</h3>
        </div>
        <div class="card-body">
          <div class="student-row">
            <el-avatar :size="48" :src="lesson.studentAvatar">
              {{ lesson.studentName?.charAt(0) }}
            </el-avatar>
            <div class="student-info">
              <h4>{{ lesson.studentName }}</h4>
              <p>{{ lesson.grade }} · {{ lesson.subject }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 课程信息 -->
      <div class="info-card">
        <div class="card-header">
          <h3>课程信息</h3>
        </div>
        <div class="card-body">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="上课日期">{{ formatDate(lesson.lessonDate) }}</el-descriptions-item>
            <el-descriptions-item label="上课时间">{{ lesson.startTime }} - {{ lesson.endTime }}</el-descriptions-item>
            <el-descriptions-item label="课程时长">{{ lesson.duration }}小时</el-descriptions-item>
            <el-descriptions-item label="课时费用">¥{{ lesson.fee }}</el-descriptions-item>
            <el-descriptions-item label="上课地点">{{ lesson.address || '线上授课' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <!-- 签到签退记录 -->
      <div v-if="lesson.checkInTime || lesson.checkOutTime" class="info-card">
        <div class="card-header">
          <h3>考勤记录</h3>
        </div>
        <div class="card-body">
          <el-timeline>
            <el-timeline-item v-if="lesson.checkInTime" timestamp="签到" placement="top" color="#67c23a">
              <div class="timeline-content">
                <p>{{ formatDateTime(lesson.checkInTime) }}</p>
                <p v-if="lesson.checkInLocation" class="location">
                  <el-icon><Location /></el-icon>{{ lesson.checkInLocation }}
                </p>
              </div>
            </el-timeline-item>
            <el-timeline-item v-if="lesson.checkOutTime" timestamp="签退" placement="top" color="#409eff">
              <div class="timeline-content">
                <p>{{ formatDateTime(lesson.checkOutTime) }}</p>
                <p v-if="lesson.checkOutContent" class="content">{{ lesson.checkOutContent }}</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
      
      <!-- 课程反馈 -->
      <div v-if="lesson.status === 3" class="info-card">
        <div class="card-header">
          <h3>课程反馈</h3>
        </div>
        <div class="card-body">
          <div v-if="lesson.feedback" class="feedback">
            <p>{{ lesson.feedback }}</p>
          </div>
          <el-empty v-else description="暂无反馈" :image-size="60" />
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button v-if="lesson.status === 1" type="primary" size="large" @click="goToCheckin">
          开始签到
        </el-button>
        <el-button v-if="lesson.status === 2" type="success" size="large" @click="handleCheckOut">
          结束上课
        </el-button>
        <el-button v-if="lesson.status === 4" type="warning" size="large" @click="handleDispute">
          查看争议
        </el-button>
      </div>
    </div>
    
    <!-- 签退弹窗 -->
    <el-dialog v-model="checkoutVisible" title="结束上课" width="500px">
      <el-form :model="checkoutForm" label-width="80px">
        <el-form-item label="课程小结">
          <el-input
            v-model="checkoutForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入本次课程的教学内容和学生表现"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCheckout">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location } from '@element-plus/icons-vue'
import { getLessonDetail, checkOut } from '@shared/api/teaching'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const lesson = ref(null)
const checkoutVisible = ref(false)
const submitting = ref(false)

const checkoutForm = reactive({
  content: ''
})

const getStatusText = (status) => {
  const map = { 1: '待上课', 2: '上课中', 3: '已完成', 4: '有争议' }
  return map[status] || '未知'
}

const formatDate = (date) => dayjs(date).format('YYYY年M月D日')
const formatDateTime = (time) => dayjs(time).format('YYYY-MM-DD HH:mm:ss')

const loadLesson = async () => {
  const lessonId = route.params.id
  loading.value = true
  
  try {
    const res = await getLessonDetail(lessonId)
    if (res.code === 200) {
      lesson.value = res.data
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const goToCheckin = () => {
  router.push(`/checkin?lessonId=${lesson.value.id}`)
}

const handleCheckOut = () => {
  checkoutVisible.value = true
}

const submitCheckout = async () => {
  submitting.value = true
  try {
    const res = await checkOut(lesson.value.id, {
      content: checkoutForm.content
    })
    if (res.code === 200) {
      ElMessage.success('签退成功')
      checkoutVisible.value = false
      loadLesson()
    }
  } catch (error) {
    ElMessage.error(error.message || '签退失败')
  } finally {
    submitting.value = false
  }
}

const handleDispute = () => {
  ElMessage.info('争议处理功能开发中')
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadLesson()
})
</script>

<style lang="scss" scoped>
.lesson-detail {
  max-width: 600px;
  margin: 0 auto;
  
  .detail-container {
    margin-top: 24px;
  }
  
  .status-banner {
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 16px;
    color: #fff;
    
    &.status-1 { background: linear-gradient(135deg, #909399, #c0c4cc); }
    &.status-2 { background: linear-gradient(135deg, #e6a23c, #f5bf6e); }
    &.status-3 { background: linear-gradient(135deg, #67c23a, #95d475); }
    &.status-4 { background: linear-gradient(135deg, #f56c6c, #fab6b6); }
    
    .status-content {
      text-align: center;
      
      h2 {
        font-size: 24px;
        font-weight: 600;
        margin-bottom: 8px;
      }
      
      p {
        font-size: 14px;
        opacity: 0.9;
      }
    }
  }
  
  .info-card {
    background: #fff;
    border-radius: 12px;
    margin-bottom: 16px;
    overflow: hidden;
    
    .card-header {
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
    gap: 12px;
    
    .student-info {
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
  
  .timeline-content {
    p {
      margin-bottom: 4px;
      
      &.location, &.content {
        font-size: 13px;
        color: #909399;
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
  
  .feedback {
    p {
      font-size: 14px;
      color: #606266;
      line-height: 1.8;
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
