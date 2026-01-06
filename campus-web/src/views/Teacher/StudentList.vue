<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDemandList } from '@/api/demand'

const router = useRouter()
const loading = ref(false)

// 学生需求列表
const students = ref([
  {
    id: 1,
    grade: '小学三年级',
    subject: '数学',
    price: 150,
    location: '阳光小区 (距您 1.5km)',
    tags: ['基础薄弱', '需耐心'],
    desc: '孩子数学基础较弱，计算容易出错，希望能找有耐心的老师辅导作业。',
    time: '周六上午',
    publishTime: '刚刚发布'
  },
  {
    id: 2,
    grade: '初中二年级',
    subject: '物理',
    price: 200,
    location: '万达广场 (距您 3.0km)',
    tags: ['目标提分', '严厉型'],
    desc: '期中考试想提升20分，希望老师严格一点，主攻力学部分。',
    time: '周日晚上',
    publishTime: '1小时前'
  },
  {
    id: 3,
    grade: '高中一年级',
    subject: '英语口语',
    price: 250,
    location: '线上教学',
    tags: ['留学准备', '全英教学'],
    desc: '准备出国，需要练习口语对话，希望老师有雅思教学经验。',
    time: '工作日晚上',
    publishTime: '2小时前'
  },
  {
    id: 4,
    grade: '小学六年级',
    subject: '全科辅导',
    price: 180,
    location: '幸福家园 (距您 0.8km)',
    tags: ['小升初', '作业辅导'],
    desc: '针对小升初考试进行全科复习梳理，查漏补缺。',
    time: '周末全天',
    publishTime: '3小时前'
  }
])

// 获取需求列表
const fetchStudents = async () => {
  loading.value = true
  try {
    const res = await getDemandList({ page: 1, pageSize: 20, status: 1 })
    if (res.data && res.data.records && res.data.records.length > 0) {
      students.value = res.data.records.map((item, idx) => ({
        id: item.id,
        grade: `${item.gradeLevel || '小学'}${item.gradeName || '年级'}`,
        subject: item.subjects || '数学',
        price: item.maxPrice || 150,
        location: item.teachLocation || '待定',
        tags: buildTags(item),
        desc: item.description || '暂无描述',
        time: item.availableTime || '待定',
        publishTime: formatTime(item.createTime)
      }))
    }
  } catch (error) {
    console.error('获取需求失败:', error)
  } finally {
    loading.value = false
  }
}

const buildTags = (item) => {
  const tags = []
  if (item.urgency === 1) tags.push('急需')
  if (item.teachMode === 'online') tags.push('线上')
  if (item.targetScore) tags.push('目标提分')
  return tags.length ? tags : ['新发布']
}

const formatTime = (time) => {
  if (!time) return '刚刚发布'
  const diff = Date.now() - new Date(time).getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours < 1) return '刚刚发布'
  if (hours < 24) return `${hours}小时前`
  return `${Math.floor(hours / 24)}天前`
}

// 跳转详情
const goToDetail = (id) => {
  router.push(`/student/${id}`)
}

onMounted(() => {
  fetchStudents()
})
</script>

<template>
  <div class="student-list-page" v-loading="loading">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">最新家教需求</h1>
      <p class="page-subtitle">为您匹配附近的学生需求</p>
    </div>

    <!-- 需求列表 -->
    <div class="list-content">
      <el-card 
        v-for="item in students" 
        :key="item.id"
        class="demand-card"
        shadow="hover"
        @click="goToDetail(item.id)"
      >
        <!-- 头部信息 -->
        <div class="card-header">
          <h3 class="demand-title">{{ item.grade }} · {{ item.subject }}</h3>
          <div class="demand-price">
            <span class="price-value">¥{{ item.price }}</span>
            <span class="price-unit">/h</span>
          </div>
        </div>

        <!-- 标签 -->
        <div class="tags-section">
          <el-tag 
            v-for="tag in item.tags" 
            :key="tag"
            type="primary"
            size="small"
            effect="plain"
          >
            {{ tag }}
          </el-tag>
        </div>

        <!-- 详细信息 -->
        <div class="info-section">
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span>{{ item.location }}</span>
          </div>
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>{{ item.time }}</span>
          </div>
          <div class="info-item desc">
            <el-icon><Document /></el-icon>
            <span>{{ item.desc }}</span>
          </div>
        </div>

        <!-- 底部 -->
        <div class="card-footer">
          <span class="publish-time">{{ item.publishTime }}</span>
          <el-button type="primary" text size="small">
            查看详情
            <el-icon class="ml-1"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </el-card>

      <el-empty v-if="students.length === 0" description="暂无需求" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.student-list-page {
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
    margin-bottom: 4px;
  }

  .page-subtitle {
    font-size: 14px;
    opacity: 0.9;
  }
}

.list-content {
  padding: $spacing-lg;
}

.demand-card {
  margin-bottom: $spacing-md;
  border-radius: 16px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-sm;

    .demand-title {
      font-size: 18px;
      font-weight: 600;
      color: $text-primary;
    }

    .demand-price {
      .price-value {
        font-size: 20px;
        font-weight: 700;
        color: $danger-color;
      }

      .price-unit {
        font-size: 12px;
        color: $text-muted;
      }
    }
  }

  .tags-section {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-bottom: $spacing-md;
  }

  .info-section {
    background: $bg-light;
    border-radius: 12px;
    padding: $spacing-md;
    margin-bottom: $spacing-md;

    .info-item {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      font-size: 14px;
      color: $text-muted;
      margin-bottom: $spacing-sm;

      &:last-child {
        margin-bottom: 0;
      }

      &.desc {
        span {
          display: -webkit-box;
          -webkit-line-clamp: 1;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
      }

      .el-icon {
        color: $text-muted;
        flex-shrink: 0;
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: $spacing-sm;
    border-top: 1px solid $border-color;

    .publish-time {
      font-size: 12px;
      color: $text-muted;
    }
  }
}
</style>
