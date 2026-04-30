<template>
  <div class="demand-list-page">
    <div class="page-header">
      <h1 class="page-title">我的需求</h1>
      <el-button type="primary" @click="createDemand">
        <el-icon><Plus /></el-icon>
        发布需求
      </el-button>
    </div>
    
    <!-- 状态筛选 -->
    <div class="filter-tabs">
      <el-radio-group v-model="statusFilter" @change="loadDemands">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="1">已上架</el-radio-button>
        <el-radio-button value="2">已匹配</el-radio-button>
        <el-radio-button value="0">已下架</el-radio-button>
        <el-radio-button value="3">已完成</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 需求列表 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>
    
    <div v-else-if="demands.length === 0" class="empty-container">
      <el-empty description="暂无需求">
        <el-button type="primary" @click="createDemand">发布需求</el-button>
      </el-empty>
    </div>
    
    <div v-else class="demand-list">
      <div
        v-for="demand in demands"
        :key="demand.id"
        class="demand-card"
        @click="viewDetail(demand.id)"
      >
        <div class="demand-header">
          <div class="demand-title">{{ demand.title }}</div>
          <el-tag :type="getStatusType(demand.status)" size="small">
            {{ getStatusText(demand.status) }}
          </el-tag>
        </div>
        
        <div class="demand-info">
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span>{{ getStudentName(demand.studentId) }}</span>
          </div>
          <div class="info-item">
            <el-icon><Reading /></el-icon>
            <span>{{ demand.subject }} · {{ demand.grade }}</span>
          </div>
          <div class="info-item">
            <el-icon><Money /></el-icon>
            <span>{{ demand.expectPrice }}元/小时</span>
          </div>
          <div class="info-item">
            <el-icon><Location /></el-icon>
            <span>{{ demand.address || '未设置地址' }}</span>
          </div>
        </div>
        
        <div class="demand-footer">
          <div class="apply-count" v-if="demand.applyCount > 0">
            <el-icon><UserFilled /></el-icon>
            <span>{{ demand.applyCount }}位老师申请</span>
          </div>
          <div class="time">{{ formatTime(demand.createTime) }}</div>
        </div>
        
        <div class="demand-actions" @click.stop>
          <template v-if="demand.status === 0">
            <el-button size="small" type="primary" @click="publishDemand(demand)">上架</el-button>
            <el-button size="small" @click="editDemand(demand.id)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="deleteDemandRow(demand)">删除</el-button>
          </template>
          
          <template v-else-if="demand.status === 1">
            <el-button size="small" @click="viewApplicants(demand.id)">查看申请</el-button>
            <el-button size="small" type="warning" @click="withdrawDemand(demand)">下架</el-button>
          </template>
          
          <template v-else-if="demand.status === 2">
            <el-button size="small" type="primary" @click="publishDemand(demand)">重新上架</el-button>
            <el-button size="small" type="danger" plain @click="deleteDemandRow(demand)">删除</el-button>
          </template>
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
        @current-change="loadDemands"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, Reading, Money, Location, UserFilled } from '@element-plus/icons-vue'
import { getMyDemands, publishDemand as publishApi, withdrawDemand as withdrawApi, deleteDemand as deleteApi } from '@shared/api/demand'
import { relativeFromNow } from '@shared/utils'
import { getStudentList } from '@shared/api/parent'

const router = useRouter()
const loading = ref(false)
const demands = ref([])
const statusFilter = ref('all')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const students = ref([])

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

const getStudentName = (id) => {
  if (!id) return '未关联学生'
  const student = students.value.find(s => s.id === id)
  return student ? (student.studentName || student.name) : '未关联学生'
}

const getStatusType = (status) => {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: '' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '已下架', 1: '已上架', 2: '已匹配', 3: '已完成' }
  return texts[status] || '未知'
}

const formatTime = (time) => relativeFromNow(time)

const loadDemands = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value
    }
    if (statusFilter.value !== 'all') {
      params.status = statusFilter.value
    }
    
    const res = await getMyDemands(params)
    if (res.code === 200) {
      // 兼容后端未分页的返回结构(list)以及分页结构(records/total)
      const records = res.data?.records ?? res.data ?? []
      const filtered = statusFilter.value === 'all'
        ? records
        : records.filter(item => String(item.status) === String(statusFilter.value))
      demands.value = filtered
      total.value = res.data?.total ?? filtered.length ?? 0
    }
  } catch (error) {
    console.error('加载需求列表失败:', error)
  } finally {
    loading.value = false
  }
}

const createDemand = () => {
  router.push('/parent/demands/create')
}

const viewDetail = (id) => {
  router.push(`/parent/demands/${id}`)
}

const editDemand = (id) => {
  router.push(`/parent/demands/${id}/edit`)
}

const viewApplicants = (id) => {
  router.push(`/parent/demands/${id}/applicants`)
}

const publishDemand = async (demand) => {
  try {
    await ElMessageBox.confirm('确定要上架此需求吗？上架后教师可以查看并申请。', '上架确认')
    const res = await publishApi(demand.id)
    if (res.code === 200) {
      ElMessage.success('上架成功')
      loadDemands()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('上架失败:', error)
    }
  }
}

const withdrawDemand = async (demand) => {
  try {
    await ElMessageBox.confirm('确定要下架此需求吗？下架后教师将无法查看。', '下架确认')
    const res = await withdrawApi(demand.id)
    if (res.code === 200) {
      ElMessage.success('下架成功')
      loadDemands()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('下架失败:', error)
    }
  }
}

// 删除需求的逻辑
const deleteDemandRow = async (demand) => {
  try {
    await ElMessageBox.confirm('确定要彻底删除此需求吗？删除后无法恢复。', '删除确认', { 
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning' 
    })
    
    const res = await deleteApi(demand.id)
    if (res.code === 200) {
      ElMessage.success('需求已删除')
      loadDemands() // 重新刷新列表
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

onMounted(() => {
  loadStudents()
  loadDemands()
})
</script>

<style lang="scss" scoped>
.demand-list-page {
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

.filter-tabs {
  margin-bottom: 20px;
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.demand-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.demand-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  }
}

.demand-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  
  .demand-title {
    font-size: 16px;
    font-weight: 600;
    flex: 1;
    margin-right: 12px;
  }
}

.demand-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
  
  .info-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 14px;
    color: #666;
  }
}

.demand-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  
  .apply-count {
    display: flex;
    align-items: center;
    gap: 4px;
    color: var(--el-color-primary);
  }
}

.demand-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
