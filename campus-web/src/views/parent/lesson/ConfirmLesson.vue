<template>
  <div class="confirm-lesson-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">确认课时</h1>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <template v-else-if="lesson">
      <!-- 课时信息卡片 -->
      <div class="lesson-card">
        <div class="card-header">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatDate(lesson.startTime) }}</span>
        </div>
        
        <div class="lesson-info">
          <div class="info-row">
            <span class="label">辅导科目</span>
            <span class="value">{{ lesson.subject }}</span>
          </div>
          <div class="info-row">
            <span class="label">上课时间</span>
            <span class="value">{{ formatTime(lesson.startTime) }} - {{ formatTime(lesson.endTime) }}</span>
          </div>
          <div class="info-row">
            <span class="label">课时时长</span>
            <span class="value">{{ lesson.duration }} 小时</span>
          </div>
          <div class="info-row">
            <span class="label">课时费用</span>
            <span class="value price">¥{{ (lesson.amount || 0).toFixed(2) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 教师信息 -->
      <div class="tutor-card">
        <el-avatar :size="48" :src="lesson.tutorAvatar">
          {{ lesson.tutorName?.charAt(0) }}
        </el-avatar>
        <div class="tutor-info">
          <div class="name">{{ lesson.tutorName }}</div>
          <div class="school">{{ lesson.tutorUniversity }}</div>
        </div>
      </div>
      
      <!-- 确认提示 -->
      <div class="confirm-tips">
        <el-alert
          title="确认须知"
          type="warning"
          :closable="false"
          show-icon
        >
          <template #default>
            <ul class="tips-list">
              <li>请确认课时信息无误后再点击确认</li>
              <li>确认后课时费将结算给教师</li>
              <li>如有异议请点击申诉</li>
            </ul>
          </template>
        </el-alert>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button size="large" type="danger" plain @click="handleDispute">
          申诉
        </el-button>
        <el-button size="large" type="primary" :loading="confirming" @click="handleConfirm">
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
import { ArrowLeft, Calendar } from '@element-plus/icons-vue'
import { getLessonDetail, confirmLesson, disputeLesson } from '@shared/api/teaching'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const confirming = ref(false)
const lesson = ref(null)

const formatDate = (time) => dayjs(time).format('YYYY年MM月DD日')
const formatTime = (time) => dayjs(time).format('HH:mm')

const goBack = () => router.back()

const loadLesson = async () => {
  loading.value = true
  try {
    const res = await getLessonDetail(route.params.id)
    if (res.code === 200) {
      lesson.value = res.data
    }
  } catch (error) {
    console.error('加载课时失败:', error)
  } finally {
    loading.value = false
  }
}

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认该课时信息无误？确认后将结算课时费给老师。', '确认课时')
    confirming.value = true
    const res = await confirmLesson(route.params.id)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      router.back()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认失败:', error)
    }
  } finally {
    confirming.value = false
  }
}

const handleDispute = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请描述申诉原因', '申诉课时', {
      inputPlaceholder: '请输入申诉原因'
    })
    if (!reason) {
      ElMessage.warning('请输入申诉原因')
      return
    }
    const res = await disputeLesson(route.params.id, reason)
    if (res.code === 200) {
      ElMessage.success('申诉已提交')
      router.back()
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
.confirm-lesson-page {
  padding: 20px;
  max-width: 600px;
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

.lesson-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .lesson-info {
    .info-row {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      
      .label {
        color: #666;
      }
      
      .value {
        font-weight: 500;
        
        &.price {
          color: #f56c6c;
          font-size: 18px;
        }
      }
    }
  }
}

.tutor-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .tutor-info {
    .name {
      font-weight: 600;
      margin-bottom: 4px;
    }
    
    .school {
      font-size: 13px;
      color: #666;
    }
  }
}

.confirm-tips {
  margin-bottom: 24px;
  
  .tips-list {
    margin: 8px 0 0;
    padding-left: 20px;
    
    li {
      margin-bottom: 4px;
      font-size: 13px;
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
