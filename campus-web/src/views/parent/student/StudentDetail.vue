<template>
  <div class="student-detail-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">孩子详情</h1>
      <el-button type="primary" plain @click="editStudent">
        <el-icon><Edit /></el-icon>
        编辑
      </el-button>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <template v-else-if="student">
      <div class="info-card">
        <div class="card-header">
          <el-avatar :size="72" :src="student.avatar">
            {{ (student.studentName || student.name)?.charAt(0) }}
          </el-avatar>
          <div class="header-info">
            <h2>{{ student.studentName || student.name }}</h2>
            <div class="tags">
              <el-tag :type="student.gender === 1 ? 'primary' : 'danger'">
                {{ student.gender === 1 ? '男' : '女' }}
              </el-tag>
              <el-tag type="info">{{ student.grade || '未设置年级' }}</el-tag>
            </div>
          </div>
        </div>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="就读学校">
            {{ student.school || '未填写' }}
          </el-descriptions-item>
          <el-descriptions-item label="需辅导科目">
            <template v-if="student.subjects?.length">
              <el-tag 
                v-for="subject in student.subjects" 
                :key="subject"
                class="subject-tag"
              >
                {{ subject }}
              </el-tag>
            </template>
            <span v-else class="empty-text">未设置</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="info-card">
        <h3 class="card-title">学习情况</h3>
        <div class="description-text">
          {{ student.description || '暂无描述' }}
        </div>
      </div>
      
      <div class="info-card">
        <div class="card-title-row">
          <h3 class="card-title">相关需求</h3>
          <el-button type="primary" size="small" @click="createDemand">
            发布需求
          </el-button>
        </div>
        
        <div v-if="demands.length === 0" class="empty-demands">
          <el-empty description="暂无相关需求" :image-size="80">
            <el-button type="primary" @click="createDemand">为TA发布需求</el-button>
          </el-empty>
        </div>
        <div v-else class="demand-list">
          <div 
            v-for="demand in demands" 
            :key="demand.id" 
            class="demand-item"
            @click="viewDemand(demand.id)"
          >
            <div class="demand-info">
              <div class="demand-title">{{ demand.title }}</div>
              <div class="demand-meta">
                <span>{{ demand.subject }}</span>
                <el-tag size="small" :type="demand.status === 1 ? 'success' : 'info'">
                  {{ demand.status === 1 ? '已上架' : (demand.status === 0 ? '草稿' : '已下架') }}
                </el-tag>
              </div>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <div class="danger-zone">
        <el-button type="danger" plain @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除孩子信息
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight, Edit, Delete } from '@element-plus/icons-vue'
import { getStudentDetail, deleteStudent } from '@shared/api/parent'
import { getMyDemands } from '@shared/api/demand' 

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const student = ref(null)
const demands = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const [studentRes, demandsRes] = await Promise.all([
      getStudentDetail(route.params.id),
      getMyDemands({ page: 1, size: 100 }) 
    ])
    
    if (studentRes.code === 200 && studentRes.data) {
      const data = studentRes.data
      
      let parsedSubjects = []
      if (Array.isArray(data.weakSubjects)) {
        parsedSubjects = data.weakSubjects
      } else if (typeof data.weakSubjects === 'string') {
        try { parsedSubjects = JSON.parse(data.weakSubjects) } catch (e) {}
      }
      
      student.value = {
        ...data,
        school: data.schoolName || data.school,
        subjects: parsedSubjects.length ? parsedSubjects : (data.subjects || []),
        description: data.studyDesc || data.description
      }
    }
    
    if (demandsRes.code === 200) {
      const allDemands = demandsRes.data?.records ?? demandsRes.data ?? []
      demands.value = allDemands.filter(d => String(d.studentId) === String(route.params.id))
    }
  } catch (error) {
    console.error('加载详情失败:', error)
    ElMessage.error('加载信息失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => router.back()
const editStudent = () => router.push(`/parent/students/${route.params.id}/edit`)
const createDemand = () => router.push({ path: '/parent/demands/create', query: { studentId: route.params.id } })
const viewDemand = (id) => router.push(`/parent/demands/${id}`)

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除"${student.value.studentName || student.value.name}"的信息吗？`, '删除确认', { type: 'warning' })
    const res = await deleteStudent(route.params.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      router.replace('/parent/students')
    }
  } catch (error) { /* cancelled */ }
}

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.student-detail-page { padding: 20px; max-width: 800px; margin: 0 auto; }
.page-header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; .page-title { flex: 1; font-size: 20px; font-weight: 600; margin: 0; } }
.loading-container { padding: 40px; background: #fff; border-radius: 12px; }
.info-card { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 16px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  .card-header { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; .header-info { h2 { font-size: 20px; margin: 0 0 8px; } .tags { display: flex; gap: 8px; } } }
  .card-title { font-size: 16px; font-weight: 600; margin: 0 0 16px; }
  .card-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; .card-title { margin: 0; } }
}
.subject-tag { margin-right: 8px; margin-bottom: 4px; }
.description-text { color: #666; line-height: 1.8; white-space: pre-wrap; }
.empty-demands { padding: 20px 0; }
.demand-list { display: flex; flex-direction: column; gap: 12px; }
.demand-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; background: #f5f7fa; border-radius: 8px; cursor: pointer; transition: background 0.3s;
  &:hover { background: #e8f0fe; }
  .demand-title { font-weight: 500; margin-bottom: 4px; }
  .demand-meta { font-size: 12px; color: #666; display: flex; align-items: center; gap: 12px; }
}
.danger-zone { margin-top: 32px; text-align: center; }
</style>