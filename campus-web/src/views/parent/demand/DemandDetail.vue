<template>
  <div class="demand-detail-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">需求详情</h1>
      <el-dropdown v-if="demand" @command="handleCommand">
        <el-button link>
          <el-icon><MoreFilled /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-if="demand.status === 0" command="edit">编辑</el-dropdown-item>
            <el-dropdown-item v-if="demand.status === 0" command="publish">上架</el-dropdown-item>
            <el-dropdown-item v-if="demand.status === 1" command="withdraw">下架</el-dropdown-item>
            <el-dropdown-item v-if="demand.status !== 3" command="delete" divided>删除</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>
    
    <template v-else-if="demand">
      <!-- 状态卡片 -->
      <div class="status-card" :class="'status-' + demand.status">
        <div class="status-info">
          <el-tag :type="getStatusType(demand.status)" size="large">
            {{ getStatusText(demand.status) }}
          </el-tag>
          <span class="create-time">发布于 {{ formatDate(demand.createTime) }}</span>
        </div>
        <div v-if="demand.status === 1" class="apply-info">
          <strong>{{ demand.applyCount || 0 }}</strong> 位老师申请
        </div>
      </div>
      
      <!-- 基本信息 -->
      <div class="info-card">
        <h3 class="card-title">{{ demand.title }}</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="辅导科目">
            <el-tag>{{ demand.subject }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="学生年级">
            {{ demand.grade }}
          </el-descriptions-item>
          <el-descriptions-item label="期望薪资">
            <span class="salary">{{ demand.expectPrice }}元/小时</span>
          </el-descriptions-item>
          <el-descriptions-item label="授课方式">
            {{ demand.teachMode === 1 ? '线下上门' : (demand.teachMode === 2 ? '线上授课' : '线上线下均可') }}
          </el-descriptions-item>
          <el-descriptions-item label="上课频率">
            {{ parsedSchedule.frequency || '面议' }}
          </el-descriptions-item>
          <el-descriptions-item label="每次时长">
            {{ parsedSchedule.duration || 2 }}小时
          </el-descriptions-item>
          <el-descriptions-item label="性别要求">
            {{ parsedSchedule.genderRequirement || '不限' }}
          </el-descriptions-item>
          <el-descriptions-item label="关联学生">
            {{ getStudentName(demand.studentId) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="demand.address" label="上课地址" :span="2">
            {{ demand.address }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div v-if="demand.detail" class="info-card">
        <h3 class="card-title">对教师的其它要求</h3>
        <div class="requirements-text">{{ demand.detail }}</div>
      </div>
      
      <!-- 申请列表 -->
      <div v-if="demand.status === 1 && applicants.length > 0" class="info-card">
        <div class="card-title-row">
          <h3 class="card-title">申请老师</h3>
          <el-button type="primary" link @click="viewAllApplicants">
            查看全部 ({{ demand.applyCount }})
          </el-button>
        </div>
        
        <div class="applicant-list">
          <div
            v-for="applicant in applicants.slice(0, 3)"
            :key="applicant.id"
            class="applicant-item"
            @click="viewTutor(applicant.tutorUserId)"
          >
            <el-avatar :size="48" :src="applicant.avatar">
              {{ applicant.name?.charAt(0) }}
            </el-avatar>
            <div class="applicant-info">
              <div class="name">{{ applicant.name }}</div>
              <div class="desc">{{ applicant.university }} · {{ applicant.major }}</div>
            </div>
            <el-button size="small" type="primary" @click.stop="acceptApplicant(applicant)">
              选择TA
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 底部操作 -->
      <div class="action-bar" v-if="demand.status === 1 && applicants.length > 0">
        <el-button type="primary" size="large" @click="viewAllApplicants">
          查看全部 {{ demand.applyCount }} 位老师
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, MoreFilled } from '@element-plus/icons-vue'
import { getDemandDetail, publishDemand, withdrawDemand, deleteDemand } from '@shared/api/demand'
import dayjs from 'dayjs'
import { getStudentList } from '@shared/api/parent'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const demand = ref(null)
const applicants = ref([])
const students = ref([])

// 定义一个计算属性，自动解析后端的 JSON 字符串
const parsedSchedule = computed(() => {
  if (!demand.value?.scheduleRequire) return {}
  try {
    // 处理可能已经被后端 JSONUtil 处理过的转义字符
    return JSON.parse(demand.value.scheduleRequire)
  } catch (e) {
    console.warn('解析 scheduleRequire 失败:', e)
    return {}
  }
})

// 加载学生列表用于翻译名字
const loadStudents = async () => {
  const res = await getStudentList()
  if (res.code === 200) students.value = res.data || []
}

// 翻译函数
const getStudentName = (id) => {
  if (!id) return '未关联'
  const s = students.value.find(item => item.id === id)
  return s ? (s.studentName || s.name) : '未关联'
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: '' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '草稿', 1: '已上架', 2: '已下架', 3: '已完成' }
  return texts[status] || '未知'
}

const formatDate = (date) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 解析后端返回的 scheduleRequire JSON 字符串
const parseSchedule = (scheduleStr) => {
  if (!scheduleStr) return {}
  try {
    const parsed = JSON.parse(scheduleStr)
    return typeof parsed === 'object' ? parsed : {}
  } catch (e) {
    return {}
  }
}

const loadDemand = async () => {
  loading.value = true
  try {
    const res = await getDemandDetail(route.params.id)
    if (res.code === 200) {
      demand.value = res.data
      applicants.value = res.data?.applicants || []
    }
  } catch (error) {
    console.error('加载需求详情失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const handleCommand = async (command) => {
  switch (command) {
    case 'edit':
      router.push(`/parent/demands/${route.params.id}/edit`)
      break
    case 'publish':
      try {
        await ElMessageBox.confirm('确定要上架此需求吗？', '上架确认')
        const res = await publishDemand(route.params.id)
        if (res.code === 200) {
          ElMessage.success('上架成功')
          loadDemand()
        }
      } catch (e) { /* cancelled */ }
      break
    case 'withdraw':
      try {
        await ElMessageBox.confirm('确定要下架此需求吗？', '下架确认')
        const res = await withdrawDemand(route.params.id)
        if (res.code === 200) {
          ElMessage.success('下架成功')
          loadDemand()
        }
      } catch (e) { /* cancelled */ }
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('确定要删除此需求吗？删除后无法恢复。', '删除确认', { type: 'warning' })
        const res = await deleteDemand(route.params.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          router.replace('/parent/demands')
        }
      } catch (e) { /* cancelled */ }
      break
  }
}

const viewAllApplicants = () => {
  router.push(`/parent/demands/${route.params.id}/applicants`)
}

const viewTutor = (userId) => {
  router.push(`/parent/teachers/${userId}`)
}

const acceptApplicant = async (applicant) => {
  try {
    await ElMessageBox.confirm(`确定选择 ${applicant.name} 作为老师吗？`, '选择老师')
    router.push(`/parent/orders/create?demandId=${route.params.id}&tutorId=${applicant.tutorUserId}`)
  } catch (e) { /* cancelled */ }
}

onMounted(() => {
  loadStudents()
  loadDemand()
})
</script>

<style lang="scss" scoped>
.demand-detail-page {
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
    flex: 1;
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

.status-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .status-info {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .create-time {
    font-size: 13px;
    color: #999;
  }
  
  .apply-info {
    font-size: 14px;
    color: var(--el-color-primary);
    
    strong {
      font-size: 24px;
      margin-right: 4px;
    }
  }
}

.info-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .card-title {
    font-size: 18px;
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
  }
}

.salary {
  color: #f56c6c;
  font-weight: 600;
}

.requirements-text {
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}

.applicant-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.applicant-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
  
  &:hover {
    background: #e8f0fe;
  }
  
  .applicant-info {
    flex: 1;
    
    .name {
      font-weight: 500;
      margin-bottom: 4px;
    }
    
    .desc {
      font-size: 13px;
      color: #666;
    }
  }
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
  justify-content: center;
}
</style>
