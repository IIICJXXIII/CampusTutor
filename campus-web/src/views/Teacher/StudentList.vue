<template>
  <div class="student-list-page p-4">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-xl font-bold">我的孩子</h1>
      <el-button type="primary" @click="addStudent">
        <el-icon class="mr-1"><Plus /></el-icon>
        添加孩子
      </el-button>
    </div>
    
    <!-- 学生列表 -->
    <div v-if="loading" class="space-y-4">
      <el-skeleton :rows="3" animated v-for="i in 2" :key="i" class="p-4 bg-white rounded-lg shadow-sm" />
    </div>
    
    <div v-else-if="students.length === 0" class="flex flex-col items-center justify-center py-20 bg-white rounded-xl shadow-sm">
      <el-empty description="暂无孩子信息">
        <el-button type="primary" @click="addStudent">立即添加</el-button>
      </el-empty>
    </div>
    
    <div v-else class="grid gap-4">
      <el-card
        v-for="student in students"
        :key="student.id"
        class="student-card cursor-pointer hover:shadow-md transition-shadow"
        shadow="hover"
        @click="editStudent(student.id)"
      >
        <div class="flex items-center">
          <div class="mr-4">
            <el-avatar :size="60" :src="`https://api.dicebear.com/7.x/adventurer/svg?seed=${student.id}`">
              {{ student.name?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="flex-grow">
            <div class="flex items-center mb-1">
              <span class="text-lg font-bold mr-2">{{ student.name }}</span>
              <el-tag size="small" :type="student.gender === 1 ? 'primary' : 'danger'" effect="plain">
                {{ student.gender === 1 ? '男' : '女' }}
              </el-tag>
            </div>
            <div class="text-gray-500 text-sm mb-2">
              <span>{{ student.grade || '未设置年级' }}</span>
              <span v-if="student.universityName" class="mx-1">·</span>
              <span v-if="student.universityName">{{ student.universityName }}</span>
            </div>
            <div class="flex flex-wrap gap-2">
              <el-tag 
                v-for="subject in parseSubjects(student.subjects)" 
                :key="subject"
                size="small"
                type="info"
                class="opacity-80"
              >
                {{ subject }}
              </el-tag>
            </div>
          </div>
          <div class="flex flex-col gap-2">
            <el-button link type="primary" @click.stop="editStudent(student.id)">
              <el-icon :size="18"><Edit /></el-icon>
            </el-button>
            <el-button link type="danger" @click.stop="handleDelete(student)">
              <el-icon :size="18"><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getStudentList, deleteStudent } from '@/api/parent'

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
    ElMessage.error('无法加载学生数据')
  } finally {
    loading.value = false
  }
}

const parseSubjects = (subjectsStr) => {
  if (!subjectsStr) return []
  if (Array.isArray(subjectsStr)) return subjectsStr
  try {
    return JSON.parse(subjectsStr)
  } catch (e) {
    return subjectsStr.split(',').filter(s => s)
  }
}

const addStudent = () => {
  router.push('/students/add')
}

const editStudent = (id) => {
  router.push(`/students/${id}/edit`)
}

const handleDelete = (student) => {
  ElMessageBox.confirm(
    `确定要删除学生 ${student.name} 的信息吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      const res = await deleteStudent(student.id)
      if (res.code === 200) {
        ElMessage.success('已成功删除')
        loadStudents()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('系统异常，删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadStudents()
})
</script>

<style scoped>
.student-card {
  border-radius: 12px;
}
</style>