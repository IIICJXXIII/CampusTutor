<template>
  <div class="demand-list-page p-4 pb-20">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-xl font-bold">我的家教需求</h1>
      <el-button type="primary" @click="router.push('/parent/demand')">
        <el-icon class="mr-1"><Plus /></el-icon>
        发布新需求
      </el-button>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="space-y-4">
      <el-skeleton :rows="3" animated v-for="i in 3" :key="i" class="p-4 bg-white rounded-lg" />
    </div>

    <!-- 空状态 -->
    <div v-else-if="demands.length === 0" class="flex flex-col items-center justify-center py-20 bg-white rounded-xl shadow-sm">
      <el-empty description="您还没有发布过家教需求">
        <el-button type="primary" @click="router.push('/parent/demand')">立即发布</el-button>
      </el-empty>
    </div>

    <!-- 需求列表 -->
    <div v-else class="grid gap-4">
      <el-card
        v-for="demand in demands"
        :key="demand.id"
        class="demand-card"
        shadow="hover"
      >
        <div class="flex justify-between items-start mb-3">
          <div class="text-lg font-bold flex items-center">
            {{ demand.grade }} {{ demand.subject }}
            <el-tag 
              size="small" 
              class="ml-2"
              :type="getStatusType(demand.status)"
            >
              {{ getStatusText(demand.status) }}
            </el-tag>
          </div>
          <div class="text-orange-500 font-bold text-lg">
            {{ demand.expectPrice || demand.salary || '--' }}元/小时
          </div>
        </div>

        <div class="grid grid-cols-2 gap-y-2 text-gray-500 text-sm mb-4">
          <div class="flex items-center">
            <el-icon class="mr-1"><User /></el-icon>
            {{ demand.studentName || '未关联学生' }}
          </div>
          <div class="flex items-center">
            <el-icon class="mr-1"><Timer /></el-icon>
            {{ demand.frequency || '时间待定' }}
          </div>
          <div class="flex items-center col-span-2">
            <el-icon class="mr-1"><Location /></el-icon>
            {{ demand.address || '地址待定' }}
          </div>
        </div>

        <div class="flex justify-between items-center pt-3 border-t">
          <div class="text-xs text-gray-400">
            发布时间: {{ formatDate(demand.createTime) }}
          </div>
          <div class="flex gap-2">
            <!-- 已下架状态 -->
            <el-button 
              v-if="demand.status === 0" 
              size="small" 
              type="success"
              @click="handleStatusChange(demand, 'online')"
            >
              上架
            </el-button>
            
            <!-- 已上架状态 -->
            <el-button 
              v-if="demand.status === 1" 
              size="small" 
              type="warning"
              @click="handleStatusChange(demand, 'offline')"
            >
              下架
            </el-button>

            <el-button size="small" @click="handleEdit(demand)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(demand)">删除</el-button>
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
import { Plus, User, Timer, Location } from '@element-plus/icons-vue'
import { getMyDemands, updateDemand, closeDemand } from '@/api/demand'
import request from '@/api/request'

const router = useRouter()
const loading = ref(false)
const demands = ref([])

const loadDemands = async () => {
  loading.value = true
  try {
    const res = await getMyDemands()
    if (res.code === 200) {
      // 后端返回的是直接的 List，前面我已经修复了它对 data.records 的依赖
      demands.value = res.data || []
    }
  } catch (error) {
    console.error('获取需求列表失败:', error)
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const map = { 0: '已下架', 1: '招聘中', 2: '已完成' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning' }
  return map[status] || ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return '--'
  return new Date(dateStr).toLocaleDateString()
}

const handleStatusChange = async (demand, action) => {
  try {
    const url = `/demand/${demand.id}/${action}`
    const res = await request.post(url)
    if (res.code === 200) {
      ElMessage.success(action === 'online' ? '已成功上架' : '已成功下架')
      loadDemands()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleEdit = (demand) => {
  router.push({
    path: '/parent/demand',
    query: { id: demand.id }
  })
}

const handleDelete = (demand) => {
  ElMessageBox.confirm('确定要删除这条需求吗？', '确认删除', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/demand/${demand.id}`)
      if (res.code === 200) {
        ElMessage.success('已删除')
        loadDemands()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

onMounted(() => {
  loadDemands()
})
</script>

<style scoped>
.demand-card {
  border-radius: 12px;
}
</style>
