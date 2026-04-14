<template>
  <div class="student-list-page">
    <div class="page-header">
      <h1 class="page-title">我的孩子</h1>
      <el-button type="primary" @click="addStudent">
        <el-icon><Plus /></el-icon>
        添加孩子
      </el-button>
    </div>
    
    <!-- 学生列表 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="students.length === 0" class="empty-container">
      <el-empty description="暂无孩子信息">
        <el-button type="primary" @click="addStudent">添加孩子</el-button>
      </el-empty>
    </div>
    
    <div v-else class="student-list">
      <div
        v-for="student in students"
        :key="student.id"
        class="student-card"
        @click="viewDetail(student.id)"
      >
        <div class="student-avatar">
          <el-avatar :size="56" :src="student.avatar">
            {{ student.name?.charAt(0) }}
          </el-avatar>
        </div>
        <div class="student-info">
          <div class="student-name">
            {{ student.name }}
            <el-tag size="small" :type="student.gender === 1 ? 'primary' : 'danger'">
              {{ student.gender === 1 ? '男' : '女' }}
            </el-tag>
          </div>
          <div class="student-desc">
            <span>{{ student.grade || '未设置年级' }}</span>
            <span v-if="student.school"> · {{ student.school }}</span>
          </div>
          <div class="student-subjects" v-if="student.subjects?.length">
            <el-tag 
              v-for="subject in student.subjects.slice(0, 3)" 
              :key="subject"
              size="small"
              type="info"
            >
              {{ subject }}
            </el-tag>
            <span v-if="student.subjects.length > 3" class="more-tag">
              +{{ student.subjects.length - 3 }}
            </span>
          </div>
        </div>
        <div class="student-actions">
          <el-button link type="primary" @click.stop="editStudent(student.id)">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button link type="danger" @click.stop="deleteStudent(student)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getStudentList, deleteStudent as deleteStudentApi } from '@shared/api/parent'

const router = useRouter()
const loading = ref(false)
const students = ref([])

const loadStudents = async () => {
  loading.value = true
  try {
    const res = await getStudentList()
    if (res.code === 200) {
      students.value = res.data || []
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
  } finally {
    loading.value = false
  }
}

const addStudent = () => {
  router.push('/parent/students/add')
}

const viewDetail = (id) => {
  router.push(`/parent/students/${id}`)
}

const editStudent = (id) => {
  router.push(`/parent/students/${id}/edit`)
}

const deleteStudent = async (student) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${student.name}"的信息吗？删除后相关需求将无法关联此孩子。`,
      '删除确认',
      { type: 'warning' }
    )
    const res = await deleteStudentApi(student.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadStudents()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(() => {
  loadStudents()
})
</script>

<style lang="scss" scoped>
.student-list-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.student-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.student-card {
  display: flex;
  align-items: center;
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

.student-info {
  flex: 1;
  min-width: 0;
  
  .student-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 6px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .student-desc {
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
  }
  
  .student-subjects {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    
    .more-tag {
      font-size: 12px;
      color: #999;
    }
  }
}

.student-actions {
  display: flex;
  gap: 8px;
}
</style>
