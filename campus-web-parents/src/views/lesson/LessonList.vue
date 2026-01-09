<template>
  <div class="lesson-list-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">课时记录</h1>
    </div>
    
    <!-- 状态筛选 -->
    <div class="filter-tabs">
      <el-radio-group v-model="statusFilter" @change="handleFilterChange">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="1">待确认</el-radio-button>
        <el-radio-button value="2">已确认</el-radio-button>
        <el-radio-button value="3">申诉中</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 课时列表 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>
    
    <div v-else-if="lessons.length === 0" class="empty-container">
      <el-empty description="暂无课时记录" />
    </div>
    
    <div v-else class="lesson-list">
      <div
        v-for="lesson in lessons"
        :key="lesson.id"
        class="lesson-card"
        @click="viewDetail(lesson.id)"
      >
        <div class="lesson-header">
          <div class="lesson-date">
            <el-icon><Calendar /></el-icon>
            {{ formatDate(lesson.startTime) }}
          </div>
          <el-tag :type="getStatusType(lesson.status)" size="small">
            {{ getStatusText(lesson.status) }}
          </el-tag>
        </div>
        
        <div class="lesson-content">
          <div class="tutor-info">
            <el-avatar :size="40" :src="lesson.tutorAvatar">
              {{ lesson.tutorName?.charAt(0) }}
            </el-avatar>
            <div class="info">
              <div class="tutor-name">{{ lesson.tutorName }}</div>
              <div class="subject">{{ lesson.subject }}</div>
            </div>
          </div>
          
          <div class="lesson-time">
            <div class="time-item">
              <span class="label">上课时间</span>
              <span class="value">{{ formatTime(lesson.startTime) }}</span>
            </div>
            <div class="time-item">
              <span class="label">下课时间</span>
              <span class="value">{{ lesson.endTime ? formatTime(lesson.endTime) : '--' }}</span>
            </div>
            <div class="time-item">
              <span class="label">课时时长</span>
              <span class="value">{{ lesson.duration || '--' }}小时</span>
            </div>
          </div>
        </div>
        
        <div class="lesson-footer">
          <div class="lesson-fee">
            课时费: <strong>¥{{ (lesson.amount || 0).toFixed(2) }}</strong>
          </div>
          
          <div class="lesson-actions" @click.stop>
            <template v-if="lesson.status === 1">
              <el-button size="small" type="danger" plain @click="disputeLesson(lesson)">
                申诉
              </el-button>
              <el-button size="small" type="primary" @click="confirmLesson(lesson)">
                确认课时
              </el-button>
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
        @current-change="loadLessons"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Calendar } from '@element-plus/icons-vue'
import { getMyLessons, getOrderLessons, confirmLesson as confirmApi, disputeLesson as disputeApi } from '@shared/api/teaching'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const lessons = ref([])
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const orderId = route.query.orderId

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '上课中', 1: '待确认', 2: '已确认', 3: '申诉中' }
  return texts[status] || '未知'
}

const formatDate = (time) => {
  return dayjs(time).format('YYYY-MM-DD')
}

const formatTime = (time) => {
  return dayjs(time).format('HH:mm')
}

const loadLessons = async () => {
  loading.value = true
  try {
    let res
    if (orderId) {
      res = await getOrderLessons(orderId)
    } else {
      const params = {
        page: page.value,
        size: pageSize.value
      }
      if (statusFilter.value !== 'all') {
        params.status = statusFilter.value
      }
      res = await getMyLessons(params)
    }
    
    if (res.code === 200) {
      if (orderId) {
        lessons.value = res.data || []
        total.value = lessons.value.length
      } else {
        lessons.value = res.data?.records || []
        total.value = res.data?.total || 0
      }
    }
  } catch (error) {
    console.error('加载课时列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  page.value = 1
  loadLessons()
}

const goBack = () => {
  router.back()
}

const viewDetail = (id) => {
  router.push(`/lessons/${id}`)
}

const confirmLesson = async (lesson) => {
  try {
    await ElMessageBox.confirm(
      `确认该课时信息无误？确认后将结算课时费给老师。`,
      '确认课时'
    )
    const res = await confirmApi(lesson.id)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      loadLessons()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认课时失败:', error)
    }
  }
}

const disputeLesson = async (lesson) => {
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
    
    const res = await disputeApi(lesson.id, reason)
    if (res.code === 200) {
      ElMessage.success('申诉已提交，请等待处理')
      loadLessons()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('申诉失败:', error)
    }
  }
}

onMounted(() => {
  loadLessons()
})
</script>

<style lang="scss" scoped>
.lesson-list-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
  }
}

.filter-tabs {
  margin-bottom: 20px;
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.lesson-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.lesson-card {
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

.lesson-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #f5f7fa;
  
  .lesson-date {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 500;
  }
}

.lesson-content {
  padding: 20px;
  display: flex;
  gap: 24px;
  
  .tutor-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .tutor-name {
      font-weight: 600;
    }
    
    .subject {
      font-size: 13px;
      color: #666;
      margin-top: 2px;
    }
  }
  
  .lesson-time {
    flex: 1;
    display: flex;
    gap: 24px;
    
    .time-item {
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

.lesson-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  
  .lesson-fee {
    color: #666;
    
    strong {
      color: #f56c6c;
      font-size: 18px;
    }
  }
  
  .lesson-actions {
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
