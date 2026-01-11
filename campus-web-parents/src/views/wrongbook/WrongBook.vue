<template>
  <div class="wrongbook-page">
    <div class="page-header">
      <h1 class="page-title">错题本</h1>
      <el-button type="primary" @click="addQuestion">
        <el-icon><Plus /></el-icon>
        添加错题
      </el-button>
    </div>
    
    <!-- 筛选 -->
    <div class="filter-bar">
      <el-select v-model="filter.subject" placeholder="科目" clearable @change="loadQuestions">
        <el-option label="语文" value="语文" />
        <el-option label="数学" value="数学" />
        <el-option label="英语" value="英语" />
        <el-option label="物理" value="物理" />
        <el-option label="化学" value="化学" />
      </el-select>
      <el-select v-model="filter.studentId" placeholder="学生" clearable @change="loadQuestions">
        <el-option
          v-for="s in students"
          :key="s.id"
          :label="s.name"
          :value="s.id"
        />
      </el-select>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>
    
    <div v-else-if="questions.length === 0" class="empty-container">
      <el-empty description="暂无错题">
        <el-button type="primary" @click="addQuestion">添加第一道错题</el-button>
      </el-empty>
    </div>
    
    <div v-else class="question-list">
      <div
        v-for="q in questions"
        :key="q.id"
        class="question-card"
        @click="viewDetail(q.id)"
      >
        <div class="question-header">
          <el-tag size="small">{{ q.subject }}</el-tag>
          <span class="question-date">{{ formatDate(q.createTime) }}</span>
        </div>
        <div class="question-content">
          <div v-if="q.imageUrl" class="question-image">
            <el-image :src="q.imageUrl" fit="cover" />
          </div>
          <div class="question-text">{{ q.content }}</div>
        </div>
        <div class="question-footer">
          <span class="student-name">{{ q.studentName }}</span>
          <el-tag v-if="q.solved" type="success" size="small">已掌握</el-tag>
          <el-tag v-else type="warning" size="small">待复习</el-tag>
        </div>
      </div>
    </div>
    
    <div v-if="total > pageSize" class="pagination-container">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadQuestions"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { getWrongQuestions } from '@shared/api/wrongbook'
import { getStudentList } from '@shared/api/parent'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const questions = ref([])
const students = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filter = ref({
  subject: '',
  studentId: ''
})

const formatDate = (time) => dayjs(time).format('MM-DD HH:mm')

const loadStudents = async () => {
  try {
    const res = await getStudentList()
    if (res.code === 200) {
      students.value = res.data || []
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
  }
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      ...filter.value
    }
    const res = await getWrongQuestions(params)
    if (res.code === 200) {
      questions.value = res.data?.records || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    console.error('加载错题列表失败:', error)
  } finally {
    loading.value = false
  }
}

const addQuestion = () => {
  router.push('/wrongbook/add')
}

const viewDetail = (id) => {
  router.push(`/wrongbook/${id}`)
}

onMounted(() => {
  loadStudents()
  loadQuestions()
})
</script>

<style lang="scss" scoped>
.wrongbook-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  
  .el-select {
    width: 120px;
  }
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
  
  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .question-date {
      font-size: 13px;
      color: #999;
    }
  }
  
  .question-content {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
    
    .question-image {
      width: 80px;
      height: 80px;
      flex-shrink: 0;
      
      .el-image {
        width: 100%;
        height: 100%;
        border-radius: 8px;
      }
    }
    
    .question-text {
      flex: 1;
      font-size: 14px;
      line-height: 1.6;
      color: #333;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }
  
  .question-footer {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .student-name {
      flex: 1;
      font-size: 13px;
      color: #666;
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
