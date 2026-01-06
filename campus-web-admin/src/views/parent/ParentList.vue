<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form card-shadow">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="家长姓名/手机号" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <div class="table-container card-shadow">
      <div class="table-header">
        <span class="title">家长列表</span>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="studentCount" label="学员数" width="100" />
        <el-table-column prop="demandCount" label="发布需求" width="100" />
        <el-table-column prop="orderCount" label="订单数" width="100" />
        <el-table-column prop="totalSpent" label="消费金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalSpent }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              :active-value="1" 
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleView(row)">查看</el-button>
            <el-button text type="primary" @click="handleViewDemands(row)">需求</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
    
    <!-- 详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="家长详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="昵称">{{ currentParent.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentParent.phone }}</el-descriptions-item>
        <el-descriptions-item label="学员数量">{{ currentParent.studentCount }}</el-descriptions-item>
        <el-descriptions-item label="发布需求">{{ currentParent.demandCount }}</el-descriptions-item>
        <el-descriptions-item label="订单数量">{{ currentParent.orderCount }}</el-descriptions-item>
        <el-descriptions-item label="消费金额">¥{{ currentParent.totalSpent }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ currentParent.createTime }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 学员信息 -->
      <h4 style="margin: 20px 0 12px;">学员信息</h4>
      <el-table :data="currentParent.students || []" size="small" border>
        <el-table-column prop="name" label="学员姓名" />
        <el-table-column prop="grade" label="年级" />
        <el-table-column prop="school" label="学校" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const dialogVisible = ref(false)
const currentParent = ref({})

const searchForm = reactive({
  keyword: '',
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 模拟数据
const tableData = ref([
  { 
    id: 1, nickname: '张妈妈', phone: '13800138001', 
    studentCount: 2, demandCount: 3, orderCount: 5, totalSpent: 2500,
    status: 1, createTime: '2026-01-05 10:30:00',
    students: [
      { name: '张小明', grade: '初三', school: '北京四中' },
      { name: '张小红', grade: '高一', school: '北京四中' }
    ]
  },
  { 
    id: 2, nickname: '李爸爸', phone: '13800138002', 
    studentCount: 1, demandCount: 2, orderCount: 3, totalSpent: 1800,
    status: 1, createTime: '2026-01-04 14:20:00',
    students: [
      { name: '李小华', grade: '小学五年级', school: '人大附小' }
    ]
  },
  { 
    id: 3, nickname: '王女士', phone: '13800138003', 
    studentCount: 1, demandCount: 1, orderCount: 0, totalSpent: 0,
    status: 0, createTime: '2026-01-03 09:15:00',
    students: []
  }
])

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = 30
    loading.value = false
  }, 500)
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  handleSearch()
}

const handleView = (row) => {
  currentParent.value = { ...row }
  dialogVisible.value = true
}

const handleViewDemands = (row) => {
  router.push(`/demands?parentId=${row.id}`)
}

const handleStatusChange = async (row) => {
  ElMessage.success(`家长已${row.status === 1 ? '启用' : '禁用'}`)
}
</script>

<style lang="scss" scoped>
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  
  .title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
