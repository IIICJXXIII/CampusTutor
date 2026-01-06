<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form card-shadow">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="用户名/昵称/手机号" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" clearable style="width: 120px;">
            <el-option label="教师" :value="1" />
            <el-option label="家长" :value="2" />
          </el-select>
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
        <span class="title">用户列表</span>
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>导出
        </el-button>
      </div>
      
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="avatar" label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar" icon="UserFilled" />
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'primary' : 'success'">
              {{ row.role === 1 ? '教师' : '家长' }}
            </el-tag>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleView(row)">查看</el-button>
            <el-button text type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm 
              title="确定要删除该用户吗？" 
              @confirm="handleDelete(row)"
            >
              <template #reference>
                <el-button text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
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
    
    <!-- 查看/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'view' ? '用户详情' : '编辑用户'"
      width="500px"
    >
      <el-form :model="currentUser" label-width="80px" :disabled="dialogType === 'view'">
        <el-form-item label="用户名">
          <el-input v-model="currentUser.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="currentUser.nickname" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="currentUser.role">
            <el-radio :label="1">教师</el-radio>
            <el-radio :label="2">家长</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="currentUser.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="注册时间">
          <el-input v-model="currentUser.createTime" disabled />
        </el-form-item>
      </el-form>
      <template #footer v-if="dialogType === 'edit'">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogType = ref('view')
const currentUser = ref({})

const searchForm = reactive({
  keyword: '',
  role: null,
  status: null
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 模拟数据
const tableData = ref([
  { id: 1, username: '13800138001', nickname: '张老师', avatar: '', role: 1, status: 1, createTime: '2026-01-05 10:30:00' },
  { id: 2, username: '13800138002', nickname: '李家长', avatar: '', role: 2, status: 1, createTime: '2026-01-05 11:20:00' },
  { id: 3, username: '13800138003', nickname: '王老师', avatar: '', role: 1, status: 1, createTime: '2026-01-04 09:15:00' },
  { id: 4, username: '13800138004', nickname: '刘家长', avatar: '', role: 2, status: 0, createTime: '2026-01-04 14:30:00' },
  { id: 5, username: '13800138005', nickname: '陈老师', avatar: '', role: 1, status: 1, createTime: '2026-01-03 16:45:00' }
])

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  // TODO: 调用真实API
  setTimeout(() => {
    pagination.total = 50
    loading.value = false
  }, 500)
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.role = null
  searchForm.status = null
  handleSearch()
}

const handleView = (row) => {
  currentUser.value = { ...row }
  dialogType.value = 'view'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  currentUser.value = { ...row }
  dialogType.value = 'edit'
  dialogVisible.value = true
}

const handleSave = async () => {
  // TODO: 调用保存API
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchData()
}

const handleStatusChange = async (row) => {
  // TODO: 调用更新状态API
  ElMessage.success(`用户已${row.status === 1 ? '启用' : '禁用'}`)
}

const handleDelete = async (row) => {
  // TODO: 调用删除API
  ElMessage.success('删除成功')
  fetchData()
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
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
