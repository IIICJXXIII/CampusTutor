<template>
  <div class="lesson-detail-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">课时详情</h1>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>
    
    <template v-else-if="lesson">
      <!-- 状态卡片 -->
      <div class="status-card" :class="'status-' + lesson.status">
        <div class="status-text">{{ getStatusText(lesson.status) }}</div>
        <div class="status-desc">{{ getStatusDesc(lesson.status) }}</div>
      </div>
      
      <!-- 课时信息 -->
      <div class="info-card">
        <h3 class="card-title">课时信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="辅导科目">
            <el-tag>{{ lesson.subject }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="课时费用">
            <span class="price">¥{{ (lesson.fee || 0).toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="上课日期">
            {{ formatDate(lesson.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="上课时间">
            {{ formatTime(lesson.startTime) }} - {{ lesson.endTime ? formatTime(lesson.endTime) : '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="课时时长">
            {{ lesson.duration || '--' }} 小时
          </el-descriptions-item>
          <el-descriptions-item label="授课方式">
            {{ lesson.teachingMode || '线下' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <!-- 教师信息 -->
      <div class="info-card">
        <h3 class="card-title">教师信息</h3>
        <div class="tutor-info" @click="viewTutor">
          <el-avatar :size="56" :src="lesson.tutorAvatar">
            {{ lesson.tutorName?.charAt(0) }}
          </el-avatar>
          <div class="info">
            <div class="name">{{ lesson.tutorName }}</div>
            <div class="school">{{ lesson.tutorUniversity }}</div>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <!-- 打卡记录 -->
      <div class="info-card">
        <h3 class="card-title">打卡记录</h3>
        <div class="check-record">
          <div class="check-item">
            <div class="check-label">
              <el-icon><Clock /></el-icon>
              上课打卡
            </div>
            <div class="check-time">{{ formatDateTime(lesson.checkInTime) }}</div>
            <div v-if="lesson.checkInLocation" class="check-location">
              <el-icon><Location /></el-icon>
              {{ lesson.checkInLocation }}
            </div>
          </div>
          <div class="check-divider"></div>
          <div class="check-item">
            <div class="check-label">
              <el-icon><Clock /></el-icon>
              下课打卡
            </div>
            <div class="check-time">{{ lesson.checkOutTime ? formatDateTime(lesson.checkOutTime) : '未打卡' }}</div>
            <div v-if="lesson.checkOutLocation" class="check-location">
              <el-icon><Location /></el-icon>
              {{ lesson.checkOutLocation }}
            </div>
          </div>
        </div>
      </div>
      
      <!-- 申诉记录 -->
      <div v-if="lesson.status === 4 && lesson.dispute" class="info-card">
        <h3 class="card-title">申诉记录</h3>
        <div class="dispute-info">
          <div class="dispute-reason">
            <span class="label">申诉原因：</span>
            {{ lesson.dispute.reason }}
          </div>
          <div class="dispute-time">
            申诉时间：{{ formatDateTime(lesson.dispute.createTime) }}
          </div>
          <div v-if="lesson.dispute.reply" class="dispute-reply">
            <span class="label">处理结果：</span>
            {{ lesson.dispute.reply }}
          </div>
        </div>
      </div>
      
      <!-- 底部操作 -->
      <div v-if="lesson.status === 2" class="action-bar">
        <el-button size="large" type="danger" plain @click="disputeLesson">
          申诉
        </el-button>
        <el-button size="large" type="primary" @click="confirmLesson">
          确认课时
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Clock, Location } from '@element-plus/icons-vue'
import { getLessonDetail, confirmLesson as confirmApi, disputeLesson as disputeApi } from '@shared/api/teaching'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const lesson = ref(null)

const getStatusText = (status) => {
  const texts = { 0: '待上课', 1: '上课中', 2: '待确认', 3: '已确认', 4: '申诉中', 5: '已解决', 6: '已过期' }
  return texts[status] || '未知'
}

const getStatusDesc = (status) => {
  const descs = {
    0: '课程预约成功，等待老师上课',
    1: '老师正在上课中',
    2: '请确认课时信息无误后点击确认',
    3: '课时已确认，费用已结算',
    4: '申诉处理中，请耐心等待',
    5: '申诉已解决',
    6: '课时超时已自动确认'
  }
  return descs[status] || ''
}

const formatDate = (time) => {
  return dayjs(time).format('YYYY年MM月DD日')
}

const formatTime = (time) => {
  return dayjs(time).format('HH:mm')
}

const formatDateTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const loadLesson = async () => {
  loading.value = true
  try {
    const res = await getLessonDetail(route.params.id)
    if (res.code === 200) {
      lesson.value = res.data
    }
  } catch (error) {
    console.error('加载课时详情失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const viewTutor = () => {
  router.push(`/parent/teachers/${lesson.value.tutorUserId}`)
}

const confirmLesson = async () => {
  try {
    await ElMessageBox.confirm(
      '确认该课时信息无误？确认后将结算课时费给老师。',
      '确认课时'
    )
    const res = await confirmApi(route.params.id)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      loadLesson()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认课时失败:', error)
    }
  }
}

const disputeLesson = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '请描述申诉原因，我们会尽快处理',
      '申诉课时',
      { inputPlaceholder: '请输入申诉原因' }
    )
    
    if (!reason) {
      ElMessage.warning('请输入申诉原因')
      return
    }
    
    const res = await disputeApi(route.params.id, reason)
    if (res.code === 200) {
      ElMessage.success('申诉已提交')
      loadLesson()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('申诉失败:', error)
    }
  }
}

onMounted(() => {
  loadLesson()
})
</script>

<style lang="scss" scoped>
.lesson-detail-page {
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
  padding: 24px;
  border-radius: 12px;
  margin-bottom: 16px;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); // 0-待上课

  &.status-1 {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); // 上课中
  }

  &.status-2 {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); // 待确认
  }

  &.status-3 {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); // 已确认
  }

  &.status-4 {
    background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); // 申诉中
  }

  &.status-5 {
    background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); // 已解决
  }

  &.status-6 {
    background: linear-gradient(135deg, #bdc3c7 0%, #2c3e50 100%); // 已过期
  }
  
  .status-text {
    font-size: 24px;
    font-weight: 600;
  }
  
  .status-desc {
    font-size: 14px;
    opacity: 0.9;
    margin-top: 8px;
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
}

.price {
  color: #f56c6c;
  font-weight: 600;
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

.check-record {
  display: flex;
  gap: 24px;
  
  .check-item {
    flex: 1;
    
    .check-label {
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 500;
      margin-bottom: 8px;
    }
    
    .check-time {
      font-size: 16px;
      margin-bottom: 4px;
    }
    
    .check-location {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #666;
    }
  }
  
  .check-divider {
    width: 1px;
    background: #f0f0f0;
  }
}

.dispute-info {
  padding: 12px;
  background: #fff7e6;
  border-radius: 8px;
  
  .dispute-reason {
    margin-bottom: 8px;
    
    .label {
      font-weight: 500;
    }
  }
  
  .dispute-time {
    font-size: 13px;
    color: #666;
    margin-bottom: 8px;
  }
  
  .dispute-reply {
    padding-top: 12px;
    border-top: 1px solid #ffe7ba;
    
    .label {
      font-weight: 500;
    }
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
