<template>
  <div class="teacher-detail-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">教员档案</h1>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>
    
    <template v-else-if="tutor">
      <!-- 基本信息卡片 -->
      <div class="profile-card">
        <div class="profile-header">
          <el-avatar :size="96" :src="tutor.avatar">
            {{ tutor.name?.charAt(0) }}
          </el-avatar>
          <div class="profile-info">
            <h2 class="name">
              {{ tutor.name }}
              <el-tag v-if="tutor.verified" type="success">已认证</el-tag>
            </h2>
            <div class="school">{{ tutor.university }} · {{ tutor.major }}</div>
            <div class="tags">
              <el-tag 
                v-for="subject in tutor.subjects" 
                :key="subject"
                type="info"
              >
                {{ subject }}
              </el-tag>
            </div>
          </div>
          <div class="profile-stats">
            <div class="stat-item">
              <span class="value">{{ (tutor.rating || 5).toFixed(1) }}</span>
              <span class="label">评分</span>
            </div>
            <div class="stat-item">
              <span class="value">{{ tutor.totalHours || 0 }}</span>
              <span class="label">授课时长</span>
            </div>
            <div class="stat-item">
              <span class="value">{{ tutor.studentCount || 0 }}</span>
              <span class="label">学生数</span>
            </div>
          </div>
        </div>
        
        <div class="price-info">
          <span class="price">¥{{ tutor.minPrice || 60 }} - {{ tutor.maxPrice || 120 }}</span>
          <span class="unit">/小时</span>
        </div>
      </div>
      
      <!-- 个人介绍 -->
      <div class="info-card">
        <h3 class="card-title">个人介绍</h3>
        <div class="intro-text">{{ tutor.introduction || '暂无介绍' }}</div>
      </div>
      
      <!-- 教学经历 -->
      <div class="info-card">
        <h3 class="card-title">教学经历</h3>
        <div class="experience-text">{{ tutor.experience || '暂无教学经历描述' }}</div>
      </div>
      
      <!-- 可授年级 -->
      <div class="info-card">
        <h3 class="card-title">可辅导年级</h3>
        <div class="grade-tags">
          <el-tag 
            v-for="grade in tutor.grades" 
            :key="grade"
            class="grade-tag"
          >
            {{ grade }}
          </el-tag>
          <span v-if="!tutor.grades?.length" class="empty-text">暂未设置</span>
        </div>
      </div>
      
      <!-- 可用时间 -->
      <div class="info-card">
        <h3 class="card-title">可用时间</h3>
        <div v-if="tutor.schedules?.length" class="schedule-list">
          <div v-for="(schedule, index) in tutor.schedules" :key="index" class="schedule-item">
            <span class="day">{{ schedule.dayOfWeek }}</span>
            <span class="time">{{ schedule.startTime }} - {{ schedule.endTime }}</span>
          </div>
        </div>
        <span v-else class="empty-text">暂未设置可用时间</span>
      </div>
      
      <!-- 学生评价 -->
      <div class="info-card">
        <div class="card-title-row">
          <h3 class="card-title">学生评价</h3>
          <span class="review-count">共 {{ tutor.reviewCount || 0 }} 条评价</span>
        </div>
        
        <div v-if="reviews.length > 0" class="review-list">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-header">
              <el-avatar :size="36" :src="review.parentAvatar">
                {{ review.parentName?.charAt(0) }}
              </el-avatar>
              <div class="review-info">
                <div class="reviewer-name">{{ review.parentName }}</div>
                <el-rate :model-value="review.rating" disabled size="small" />
              </div>
              <span class="review-time">{{ formatDate(review.createTime) }}</span>
            </div>
            <div class="review-content">{{ review.content }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无评价" :image-size="60" />
      </div>
      
      <!-- 底部操作 -->
      <div class="action-bar">
        <el-button size="large" @click="chatWithTutor">
          <el-icon><ChatDotRound /></el-icon>
          联系老师
        </el-button>
        <el-button size="large" type="primary" @click="bookTutor">
          预约老师
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ChatDotRound } from '@element-plus/icons-vue'
import { getPublicTutorProfile } from '@shared/api/tutor'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const tutor = ref(null)
const reviews = ref([])

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const loadTutor = async () => {
  loading.value = true
  try {
    const res = await getPublicTutorProfile(route.params.id)
    if (res.code === 200) {
      tutor.value = res.data
      reviews.value = res.data?.reviews || []
    }
  } catch (error) {
    console.error('加载教师信息失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const chatWithTutor = () => {
  router.push(`/chat/${route.params.id}`)
}

const bookTutor = () => {
  // TODO: 实现预约流程
  ElMessage.info('预约功能开发中')
  // router.push(`/book/${route.params.id}`)
}

onMounted(() => {
  loadTutor()
})
</script>

<style lang="scss" scoped>
.teacher-detail-page {
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

.profile-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
}

.profile-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  
  .profile-info {
    flex: 1;
    
    .name {
      font-size: 22px;
      margin: 0 0 8px;
      display: flex;
      align-items: center;
      gap: 8px;
      
      .el-tag {
        background: rgba(255, 255, 255, 0.2);
        border: none;
        color: #fff;
      }
    }
    
    .school {
      font-size: 14px;
      opacity: 0.9;
      margin-bottom: 10px;
    }
    
    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      
      .el-tag {
        background: rgba(255, 255, 255, 0.2);
        border: none;
        color: #fff;
      }
    }
  }
  
  .profile-stats {
    display: flex;
    gap: 24px;
    
    .stat-item {
      text-align: center;
      
      .value {
        display: block;
        font-size: 24px;
        font-weight: 700;
      }
      
      .label {
        font-size: 12px;
        opacity: 0.8;
      }
    }
  }
}

.price-info {
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  
  .price {
    font-size: 28px;
    font-weight: 700;
  }
  
  .unit {
    font-size: 14px;
    opacity: 0.8;
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
    
    .review-count {
      font-size: 13px;
      color: #999;
    }
  }
}

.intro-text,
.experience-text {
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}

.grade-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.empty-text {
  color: #999;
}

.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.schedule-item {
  display: flex;
  gap: 16px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  
  .day {
    font-weight: 500;
  }
  
  .time {
    color: #666;
  }
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  
  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  
  .review-info {
    flex: 1;
    
    .reviewer-name {
      font-weight: 500;
      margin-bottom: 2px;
    }
  }
  
  .review-time {
    font-size: 12px;
    color: #999;
  }
}

.review-content {
  color: #666;
  line-height: 1.6;
  padding-left: 48px;
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
