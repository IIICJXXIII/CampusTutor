<template>
  <div class="student-list">
    <div class="page-header">
      <h1 class="page-title">学生需求列表</h1>
      <p class="page-desc">浏览所有家长发布的家教需求</p>
    </div>
    
    <!-- 筛选 -->
    <div class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="科目">
          <el-select v-model="filterForm.subject" placeholder="全部科目" clearable>
            <el-option-group label="艺术素养">
              <el-option label="钢琴/乐器陪练" value="钢琴/乐器陪练" />
              <el-option label="美术/书法" value="美术/书法" />
              <el-option label="声乐/视唱练耳" value="声乐/视唱练耳" />
            </el-option-group>
            <el-option-group label="体育健康">
              <el-option label="中考体育" value="中考体育" />
              <el-option label="羽毛球/网球" value="羽毛球/网球" />
              <el-option label="篮球/足球" value="篮球/足球" />
            </el-option-group>
            <el-option-group label="科创STEAM">
              <el-option label="少儿编程(Scratch/Python)" value="少儿编程(Scratch/Python)" />
              <el-option label="机器人/3D打印" value="机器人/3D打印" />
              <el-option label="科学实验/航模" value="科学实验/航模" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="filterForm.grade" placeholder="全部年级" clearable>
            <el-option label="小学" value="小学" />
            <el-option label="初中" value="初中" />
            <el-option label="高中" value="高中" />
          </el-select>
        </el-form-item>
        <el-form-item label="薪资">
          <el-select v-model="filterForm.salaryRange" placeholder="不限" clearable>
            <el-option label="50-100元/小时" value="50-100" />
            <el-option label="100-150元/小时" value="100-150" />
            <el-option label="150-200元/小时" value="150-200" />
            <el-option label="200元以上/小时" value="200+" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">筛选</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 需求列表 -->
    <div v-loading="loading" class="demand-list">
      <el-table :data="demands" stripe>
        <el-table-column prop="subject" label="科目" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.subject }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="title" label="需求标题" min-width="200">
          <template #default="{ row }">
            <span class="demand-title" @click="viewDetail(row)">
              {{ row.title || `${row.grade}${row.subject}辅导` }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="frequency" label="上课频率" width="120" />
        <el-table-column prop="address" label="地址" width="150" show-overflow-tooltip />
        <el-table-column prop="salary" label="薪资" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.salary }}/小时</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="120">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleAccept(row)">接单</el-button>
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadDemands"
          @current-change="loadDemands"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDemandList } from '@shared/api/demand'
import { acceptOrder } from '@shared/api/order'

const router = useRouter()
const loading = ref(false)
const demands = ref([])

const filterForm = reactive({
  subject: '',
  grade: '',
  salaryRange: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

const loadDemands = async () => {
  loading.value = true
  try {
    const res = await getDemandList({
      ...filterForm,
      page: pagination.page,
      size: pagination.size
    })
    
    if (res.code === 200) {
      demands.value = res.data.records || res.data || []
      pagination.total = res.data.total || demands.value.length
    }
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  pagination.page = 1
  loadDemands()
}

const resetFilter = () => {
  filterForm.subject = ''
  filterForm.grade = ''
  filterForm.salaryRange = ''
  handleFilter()
}

const viewDetail = (row) => {
  router.push(`/teacher/demand/${row.id}`)
}

const handleAccept = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要接单「${row.subject}辅导」吗？`,
      '确认接单',
      { confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    
    const res = await acceptOrder(row.orderId || row.id)
    if (res.code === 200) {
      ElMessage.success('接单成功！')
      loadDemands()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '接单失败')
    }
  }
}

onMounted(() => {
  loadDemands()
})
</script>

<style lang="scss" scoped>
.student-list {
  .filter-card {
    background: #fff;
    padding: 16px 24px;
    border-radius: 12px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
  
  .demand-list {
    background: #fff;
    border-radius: 12px;
    padding: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    .demand-title {
      color: #409eff;
      cursor: pointer;
      
      &:hover {
        text-decoration: underline;
      }
    }
    
    .price {
      color: #f56c6c;
      font-weight: 600;
    }
    
    .pagination {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
