<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { matchTutors } from '@/api/match'

const router = useRouter()
const route = useRoute()

// 筛选条件
const activeFilter = ref('综合')
const loading = ref(false)
const teachers = ref([])

// 获取匹配的教师列表
const fetchTeachers = async () => {
  loading.value = true
  try {
    const demandId = route.query.demandId
    const res = await matchTutors({
      demandId,
      latitude: 31.2304,
      longitude: 121.4737,
      page: 1,
      size: 20
    })
    
    const list = res.data?.records || [] // <--- 关键修改：取 .records
    teachers.value = list.map(item => ({
      id: item.tutorId || item.id, // 兼容字段
      tutorProfileId: item.id, // 教员档案ID
      userId: item.userId, // 用户ID
      name: item.realName || '老师',
      school: item.universityName || '未填写',
      subject: item.teachSubjects ? JSON.parse(item.teachSubjects)[0] : '综合',
      subjects: item.teachSubjects ? JSON.parse(item.teachSubjects) : [],
      grades: item.teachGrades ? JSON.parse(item.teachGrades) : [],
      price: item.expectPrice || 150,
      matchScore: item.matchScore || 80,
      distance: item.distance ? `${item.distance.toFixed(1)}km` : '未知',
      style: item.teachStyle || '鼓励型',
      tags: buildTags(item),
      avatar: item.avatar || `https://api.dicebear.com/7.x/miniavs/svg?seed=${item.tutorId}`
    }))
  } catch (error) {
    console.error('获取教师列表失败:', error)
    teachers.value = getMockTeachers()
  } finally {
    loading.value = false
  }
}

const buildTags = (item) => {
  const tags = []
  if (item.certStatus === 2) tags.push('实名认证')
  if (item.orderCount > 10) tags.push('经验丰富')
  if (item.rating >= 4.8) tags.push('好评率高')
  return tags.length ? tags : ['新入驻']
}

const getMockTeachers = () => [
  {
    id: 1,
    name: '张老师',
    school: '北京师范大学',
    subject: '数学',
    price: 200,
    matchScore: 95,
    distance: '1.2km',
    style: '鼓励型',
    tags: ['实名认证', '3年教龄'],
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=1'
  },
  {
    id: 2,
    name: '李同学',
    school: '同济大学',
    subject: '奥数',
    price: 120,
    matchScore: 88,
    distance: '2.5km',
    style: '趣味型',
    tags: ['奥数金牌', '学生证认证'],
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=2'
  },
  {
    id: 3,
    name: '王老师',
    school: '华东师范',
    subject: '物理',
    price: 250,
    matchScore: 75,
    distance: '严厉型',
    style: '严厉型',
    tags: ['在职教师', '提分快'],
    avatar: 'https://api.dicebear.com/7.x/miniavs/svg?seed=3'
  }
]

const sortedTeachers = computed(() => {
  const list = [...teachers.value]
  switch (activeFilter.value) {
    case '距离':
      return list.sort((a, b) => parseFloat(a.distance) - parseFloat(b.distance))
    case '价格':
      return list.sort((a, b) => a.price - b.price)
    case '好评':
      return list.sort((a, b) => b.matchScore - a.matchScore)
    default:
      return list.sort((a, b) => b.matchScore - a.matchScore)
  }
})

const getScoreType = (score) => {
  if (score >= 80) return 'success'
  if (score >= 60) return 'warning'
  return 'info'
}

const getStyleType = (style) => {
  const map = {
    '鼓励型': 'success',
    '严厉型': 'danger',
    '趣味型': 'primary'
  }
  return map[style] || 'info'
}

const goToDetail = (id) => {
  router.push(`/teacher/${id}`)
}

const goToBooking = (teacher) => {
  router.push({
    path: `/booking/${teacher.tutorProfileId || teacher.id}`,
    query: {
      teacherId: teacher.userId,
      tutorProfileId: teacher.tutorProfileId || teacher.id,
      teacherName: teacher.name,
      subject: teacher.subject,
      grade: teacher.grades?.[0] || '',
      price: teacher.price,
      demandId: route.query.demandId
    }
  })
}

onMounted(() => {
  fetchTeachers()
})
</script>

<template>
  <div class="teacher-list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">找老师</h1>
      <p class="page-subtitle">根据您的需求，为您匹配最合适的老师</p>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-radio-group v-model="activeFilter" size="default">
        <el-radio-button value="综合">综合推荐</el-radio-button>
        <el-radio-button value="距离">距离最近</el-radio-button>
        <el-radio-button value="价格">价格最低</el-radio-button>
        <el-radio-button value="好评">好评优先</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 教师列表 -->
    <div class="teacher-grid" v-loading="loading" element-loading-text="正在匹配老师...">
      <el-empty v-if="!loading && sortedTeachers.length === 0" description="暂无匹配的老师" />
      
      <div 
        v-for="teacher in sortedTeachers" 
        :key="teacher.id" 
        class="teacher-card"
        @click="goToDetail(teacher.id)"
      >
        <!-- 头像和基本信息 -->
        <div class="card-header">
          <el-avatar :size="56" :src="teacher.avatar" />
          <div class="teacher-info">
            <h3 class="teacher-name">{{ teacher.name }}</h3>
            <p class="teacher-school">{{ teacher.school }}</p>
          </div>
          <el-tag :type="getStyleType(teacher.style)" size="small">
            {{ teacher.style }}
          </el-tag>
        </div>

        <!-- AI 匹配度 -->
        <div class="match-score">
          <div class="score-label">
            <span>AI匹配度</span>
            <span class="score-value" :class="`score-${getScoreType(teacher.matchScore)}`">
              {{ teacher.matchScore }}%
            </span>
          </div>
          <el-progress 
            :percentage="teacher.matchScore" 
            :status="getScoreType(teacher.matchScore)"
            :stroke-width="8"
            :show-text="false"
          />
        </div>

        <!-- 标签 -->
        <div class="tag-group">
          <el-tag 
            v-for="tag in teacher.tags" 
            :key="tag" 
            type="info" 
            effect="plain"
            size="small"
          >
            {{ tag }}
          </el-tag>
        </div>

        <!-- 价格和距离 -->
        <div class="card-footer">
          <div class="price">
            <span class="price-value">¥{{ teacher.price }}</span>
            <span class="price-unit">/小时</span>
          </div>
          <div class="distance">
            <el-icon><Location /></el-icon>
            <span>{{ teacher.distance }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="card-actions">
          <el-button size="small" @click.stop="goToDetail(teacher.tutorProfileId || teacher.id)">
            查看详情
          </el-button>
          <el-button type="primary" size="small" @click.stop="goToBooking(teacher)">
            预约试课
          </el-button>
        </div>
      </div>
    </div>

    <!-- 底部提示 -->
    <div class="list-footer" v-if="sortedTeachers.length > 0">
      <el-divider>已显示全部高匹配教师</el-divider>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.teacher-list-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: 80px;
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
    opacity: 0.85;
  }
}

.filter-bar {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: $shadow-sm;

  :deep(.el-radio-group) {
    width: 100%;
    display: flex;
    
    .el-radio-button {
      flex: 1;
      
      .el-radio-button__inner {
        width: 100%;
      }
    }
  }
}

.teacher-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-md;
  padding: $spacing-lg;
  min-height: 300px;
}

.teacher-card {
  background: #fff;
  border-radius: 12px;
  padding: $spacing-lg;
  box-shadow: $shadow-sm;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  .card-header {
    display: flex;
    align-items: flex-start;
    gap: $spacing-md;
    margin-bottom: $spacing-md;

    .teacher-info {
      flex: 1;
      min-width: 0;

      .teacher-name {
        font-size: 16px;
        font-weight: 600;
        color: $text-primary;
        margin-bottom: 4px;
      }

      .teacher-school {
        font-size: 13px;
        color: $text-secondary;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }

  .match-score {
    margin-bottom: $spacing-md;

    .score-label {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: $spacing-xs;
      font-size: 13px;
      color: $text-secondary;

      .score-value {
        font-size: 18px;
        font-weight: 700;

        &.score-success { color: $success-color; }
        &.score-warning { color: $warning-color; }
        &.score-info { color: $text-muted; }
      }
    }
  }

  .tag-group {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: $spacing-md;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-md;

    .price {
      .price-value {
        font-size: 20px;
        font-weight: 700;
        color: $warning-color;
      }

      .price-unit {
        font-size: 12px;
        color: $text-muted;
      }
    }

    .distance {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: $text-secondary;
    }
  }

  .card-actions {
    display: flex;
    gap: $spacing-sm;

    .el-button {
      flex: 1;
    }
  }
}

.list-footer {
  padding: $spacing-lg;
  
  :deep(.el-divider__text) {
    color: $text-muted;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .teacher-grid {
    grid-template-columns: 1fr;
  }
}
</style>