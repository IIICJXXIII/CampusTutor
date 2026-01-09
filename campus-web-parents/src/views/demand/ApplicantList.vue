<template>
  <div class="applicant-list-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">申请列表</h1>
    </div>
    
    <!-- 需求信息 -->
    <div v-if="demand" class="demand-summary">
      <div class="demand-title">{{ demand.title }}</div>
      <div class="demand-info">
        <span>{{ demand.subject }}</span>
        <span>{{ demand.grade }}</span>
        <span>{{ demand.salary }}元/小时</span>
      </div>
    </div>
    
    <!-- 申请人列表 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>
    
    <div v-else-if="applicants.length === 0" class="empty-container">
      <el-empty description="暂无老师申请" />
    </div>
    
    <div v-else class="applicant-list">
      <div
        v-for="applicant in applicants"
        :key="applicant.id"
        class="applicant-card"
      >
        <div class="applicant-header" @click="viewTutor(applicant.tutorUserId)">
          <el-avatar :size="56" :src="applicant.avatar">
            {{ applicant.name?.charAt(0) }}
          </el-avatar>
          <div class="applicant-info">
            <div class="name">
              {{ applicant.name }}
              <el-tag v-if="applicant.verified" type="success" size="small">已认证</el-tag>
            </div>
            <div class="school">{{ applicant.university }} · {{ applicant.major }}</div>
            <div class="tags">
              <el-tag 
                v-for="subject in (applicant.subjects || []).slice(0, 3)" 
                :key="subject"
                size="small"
                type="info"
              >
                {{ subject }}
              </el-tag>
            </div>
          </div>
          <div class="rating">
            <el-rate :model-value="applicant.rating || 5" disabled />
            <span class="rating-text">{{ (applicant.rating || 5).toFixed(1) }}分</span>
          </div>
        </div>
        
        <div v-if="applicant.message" class="apply-message">
          <div class="message-label">申请留言</div>
          <div class="message-content">{{ applicant.message }}</div>
        </div>
        
        <div class="applicant-footer">
          <div class="apply-time">{{ formatTime(applicant.applyTime) }}申请</div>
          <div class="applicant-actions">
            <el-button size="small" @click="chatWithTutor(applicant)">
              <el-icon><ChatDotRound /></el-icon>
              聊一聊
            </el-button>
            <el-button size="small" type="primary" @click="selectTutor(applicant)">
              选择TA
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ChatDotRound } from '@element-plus/icons-vue'
import { getDemandDetail } from '@shared/api/demand'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const demand = ref(null)
const applicants = ref([])

const formatTime = (time) => {
  return dayjs(time).fromNow()
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDemandDetail(route.params.id)
    if (res.code === 200) {
      demand.value = res.data
      applicants.value = res.data?.applicants || []
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const viewTutor = (userId) => {
  router.push(`/teachers/${userId}`)
}

const chatWithTutor = (applicant) => {
  router.push(`/chat/${applicant.tutorUserId}`)
}

const selectTutor = async (applicant) => {
  try {
    await ElMessageBox.confirm(
      `确定选择 ${applicant.name} 作为辅导老师吗？选择后将进入签约流程。`,
      '选择老师',
      { confirmButtonText: '确定选择', cancelButtonText: '再看看' }
    )
    // TODO: 调用创建订单接口
    ElMessage.success('选择成功')
    // router.push(`/orders/create?demandId=${route.params.id}&tutorId=${applicant.tutorUserId}`)
  } catch (error) {
    // cancelled
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.applicant-list-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
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

.demand-summary {
  background: linear-gradient(135deg, var(--el-color-primary) 0%, var(--el-color-primary-light-3) 100%);
  color: #fff;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  
  .demand-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 8px;
  }
  
  .demand-info {
    font-size: 14px;
    opacity: 0.9;
    
    span {
      margin-right: 16px;
    }
  }
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.applicant-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.applicant-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.applicant-header {
  display: flex;
  gap: 16px;
  cursor: pointer;
  
  .applicant-info {
    flex: 1;
    
    .name {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 4px;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .school {
      font-size: 14px;
      color: #666;
      margin-bottom: 8px;
    }
    
    .tags {
      display: flex;
      gap: 6px;
    }
  }
  
  .rating {
    text-align: right;
    
    .rating-text {
      display: block;
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }
  }
}

.apply-message {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  
  .message-label {
    font-size: 12px;
    color: #999;
    margin-bottom: 6px;
  }
  
  .message-content {
    font-size: 14px;
    color: #333;
    line-height: 1.6;
  }
}

.applicant-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  
  .apply-time {
    font-size: 13px;
    color: #999;
  }
  
  .applicant-actions {
    display: flex;
    gap: 8px;
  }
}
</style>
