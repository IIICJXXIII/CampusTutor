<template>
  <div class="find-teachers-page">
    <div class="page-header">
      <h1 class="page-title">找老师</h1>
    </div>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索老师、科目、学校..."
        :prefix-icon="Search"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>
    
    <!-- 筛选条件 -->
    <div class="filter-bar">
      <el-select v-model="filters.subject" placeholder="科目" clearable @change="loadTutors">
        <el-option label="语文" value="语文" />
        <el-option label="数学" value="数学" />
        <el-option label="英语" value="英语" />
        <el-option label="物理" value="物理" />
        <el-option label="化学" value="化学" />
        <el-option label="生物" value="生物" />
        <el-option label="历史" value="历史" />
        <el-option label="地理" value="地理" />
        <el-option label="政治" value="政治" />
      </el-select>
      
      <el-select v-model="filters.grade" placeholder="年级" clearable @change="loadTutors">
        <el-option label="小学" value="小学" />
        <el-option label="初中" value="初中" />
        <el-option label="高中" value="高中" />
      </el-select>
      
      <el-select v-model="filters.gender" placeholder="性别" clearable @change="loadTutors">
        <el-option label="男" value="1" />
        <el-option label="女" value="2" />
      </el-select>
      
      <el-select v-model="filters.sort" placeholder="排序" @change="loadTutors">
        <el-option label="综合排序" value="default" />
        <el-option label="评分最高" value="rating" />
        <el-option label="课时最多" value="hours" />
        <el-option label="价格最低" value="price_asc" />
        <el-option label="价格最高" value="price_desc" />
      </el-select>
    </div>
    
    <!-- 教师列表 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>
    
    <div v-else-if="tutors.length === 0" class="empty-container">
      <el-empty description="暂无符合条件的老师">
        <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
      </el-empty>
    </div>
    
    <div v-else class="tutor-list">
      <div
        v-for="tutor in tutors"
        :key="tutor.id"
        class="tutor-card"
        @click="viewDetail(tutor.userId)"
      >
        <div class="tutor-avatar">
          <el-avatar :size="72" :src="tutor.avatar">
            {{ tutor.name?.charAt(0) }}
          </el-avatar>
          <el-tag v-if="tutor.verified" type="success" size="small" class="verified-tag">
            已认证
          </el-tag>
        </div>
        
        <div class="tutor-info">
          <div class="tutor-name">
            {{ tutor.name }}
            <el-tag size="small" :type="tutor.gender === 1 ? 'primary' : 'danger'">
              {{ tutor.gender === 1 ? '男' : '女' }}
            </el-tag>
          </div>
          <div class="tutor-school">{{ tutor.university }} · {{ tutor.major }}</div>
          <div class="tutor-subjects">
            <el-tag 
              v-for="subject in (tutor.subjects || []).slice(0, 4)" 
              :key="subject"
              size="small"
              type="info"
            >
              {{ subject }}
            </el-tag>
          </div>
          <div class="tutor-stats">
            <span class="stat-item">
              <el-icon><Star /></el-icon>
              {{ (tutor.rating || 5).toFixed(1) }}分
            </span>
            <span class="stat-item">
              <el-icon><Clock /></el-icon>
              {{ tutor.totalHours || 0 }}小时
            </span>
            <span class="stat-item price">
              ¥{{ tutor.minPrice || 60 }}-{{ tutor.maxPrice || 120 }}/时
            </span>
          </div>
        </div>
        
        <div class="tutor-actions">
          <el-button type="primary" size="small" @click.stop="contactTutor(tutor)">
            联系老师
          </el-button>
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
import { Search, Star, Clock } from '@element-plus/icons-vue'
import { getTutorList } from '@shared/api/match'

const router = useRouter()
const loading = ref(false)
const tutors = ref([])
const searchKeyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  subject: '',
  grade: '',
  gender: '',
  sort: 'default'
})

const loadTutors = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      subject: filters.subject || undefined,
      grade: filters.grade || undefined,
      gender: filters.gender || undefined,
      sort: filters.sort
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

const handleSearch = () => {
  page.value = 1
  loadTutors()
}

const resetFilters = () => {
  searchKeyword.value = ''
  filters.subject = ''
  filters.grade = ''
  filters.gender = ''
  filters.sort = 'default'
  page.value = 1
  loadTutors()
}

const viewDetail = (userId) => {
  router.push(`/teachers/${userId}`)
}

const contactTutor = (tutor) => {
  router.push(`/chat/${tutor.userId}`)
}

onMounted(() => {
  loadTutors()
})
</script>

<style lang="scss" scoped>
.find-teachers-page {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .el-input {
    flex: 1;
  }
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
  
  .el-select {
    width: 120px;
  }
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.tutor-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tutor-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.tutor-avatar {
  position: relative;
  
  .verified-tag {
    position: absolute;
    bottom: -4px;
    left: 50%;
    transform: translateX(-50%);
    white-space: nowrap;
  }
}

.tutor-info {
  flex: 1;
  min-width: 0;
  
  .tutor-name {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 6px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .tutor-school {
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
  }
  
  .tutor-subjects {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 10px;
  }
  
  .tutor-stats {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #666;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
    
    .price {
      color: #f56c6c;
      font-weight: 600;
    }
  }
}

.tutor-actions {
  display: flex;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
