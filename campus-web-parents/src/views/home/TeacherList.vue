<template>
  <div class="teacher-list-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">教员列表</h1>
    </div>
    
    <!-- 筛选条件 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filters">
        <el-form-item label="科目">
          <el-select v-model="filters.subject" placeholder="全部" clearable>
            <el-option label="语文" value="语文" />
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="filters.grade" placeholder="全部" clearable>
            <el-option label="小学" value="小学" />
            <el-option label="初中" value="初中" />
            <el-option label="高中" value="高中" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadTutors">筛选</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 教师列表 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>
    
    <div v-else-if="tutors.length === 0" class="empty-container">
      <el-empty description="暂无符合条件的老师" />
    </div>
    
    <div v-else class="tutor-grid">
      <div
        v-for="tutor in tutors"
        :key="tutor.id"
        class="tutor-card"
        @click="viewDetail(tutor.userId)"
      >
        <el-avatar :size="80" :src="tutor.avatar">
          {{ tutor.name?.charAt(0) }}
        </el-avatar>
        <div class="tutor-name">{{ tutor.name }}</div>
        <div class="tutor-school">{{ tutor.university }}</div>
        <div class="tutor-subjects">
          <el-tag v-for="s in (tutor.subjects || []).slice(0, 2)" :key="s" size="small">
            {{ s }}
          </el-tag>
        </div>
        <div class="tutor-price">¥{{ tutor.minPrice || 60 }}/时起</div>
        <div class="tutor-rating">
          <el-rate :model-value="tutor.rating || 5" disabled size="small" />
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
        @current-change="loadTutors"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getTutorList } from '@shared/api/match'

const router = useRouter()
const loading = ref(false)
const tutors = ref([])
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

const filters = reactive({
  subject: '',
  grade: ''
})

const loadTutors = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      ...filters
    }
    const res = await getTutorList(params)
    if (res.code === 200) {
      tutors.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    console.error('加载教师列表失败:', error)
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.subject = ''
  filters.grade = ''
  page.value = 1
  loadTutors()
}

const goBack = () => {
  router.back()
}

const viewDetail = (userId) => {
  router.push(`/teachers/${userId}`)
}

onMounted(() => {
  loadTutors()
})
</script>

<style lang="scss" scoped>
.teacher-list-page {
  padding: 20px;
  max-width: 1200px;
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

.filter-section {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.tutor-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.tutor-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
  
  .tutor-name {
    font-size: 16px;
    font-weight: 600;
    margin-top: 12px;
  }
  
  .tutor-school {
    font-size: 13px;
    color: #666;
    margin-top: 4px;
  }
  
  .tutor-subjects {
    margin-top: 8px;
    display: flex;
    justify-content: center;
    gap: 6px;
  }
  
  .tutor-price {
    margin-top: 10px;
    color: #f56c6c;
    font-weight: 600;
  }
  
  .tutor-rating {
    margin-top: 8px;
    display: flex;
    justify-content: center;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
