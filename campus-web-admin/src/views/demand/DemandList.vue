<template>
  <div class="page-container">
    <!-- 搜索表单 -->
    <div class="search-form card-shadow">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="家长/科目/地址" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="科目">
          <el-select v-model="searchForm.subject" placeholder="全部" clearable style="width: 120px;">
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="语文" value="语文" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="待匹配" value="pending" />
            <el-option label="已匹配" value="matched" />
            <el-option label="已关闭" value="closed" />
            <el-option label="已过期" value="expired" />
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
        <span class="title">需求列表</span>
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="pending">待匹配</el-radio-button>
          <el-radio-button label="matched">已匹配</el-radio-button>
        </el-radio-group>
      </div>
      
      <el-table :data="filteredData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="parentName" label="家长" width="100" />
        <el-table-column prop="studentName" label="学员" width="100" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="subject" label="科目" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.subject }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectPrice" label="期望价格" width="120">
          <template #default="{ row }">
            ¥{{ row.expectPrice }}/课时
          </template>
        </el-table-column>
        <el-table-column prop="address" label="上课地址" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleView(row)">查看</el-button>
            <el-button text type="warning" v-if="row.status === 'pending'" @click="handleMatch(row)">匹配</el-button>
            <el-button text type="danger" @click="handleClose(row)">关闭</el-button>
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
    <el-dialog v-model="dialogVisible" title="需求详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="家长">{{ currentDemand.parentName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentDemand.phone }}</el-descriptions-item>
        <el-descriptions-item label="学员姓名">{{ currentDemand.studentName }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ currentDemand.grade }}</el-descriptions-item>
        <el-descriptions-item label="科目">{{ currentDemand.subject }}</el-descriptions-item>
        <el-descriptions-item label="期望价格">¥{{ currentDemand.expectPrice }}/课时</el-descriptions-item>
        <el-descriptions-item label="上课频率">{{ currentDemand.frequency }}</el-descriptions-item>
        <el-descriptions-item label="教师性别要求">{{ currentDemand.teacherGender || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="上课地址" :span="2">{{ currentDemand.address }}</el-descriptions-item>
        <el-descriptions-item label="需求描述" :span="2">{{ currentDemand.description }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentDemand.status)">
            {{ getStatusText(currentDemand.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ currentDemand.createTime }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- 匹配的教师 -->
      <div v-if="currentDemand.matchedTutor" style="margin-top: 20px;">
        <h4 style="margin-bottom: 12px;">匹配教师</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="教师姓名">{{ currentDemand.matchedTutor.name }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentDemand.matchedTutor.phone }}</el-descriptions-item>
          <el-descriptions-item label="教授科目">{{ currentDemand.matchedTutor.subject }}</el-descriptions-item>
          <el-descriptions-item label="匹配时间">{{ currentDemand.matchedTutor.matchTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
    
    <!-- 匹配弹窗 -->
    <el-dialog v-model="matchDialogVisible" title="手动匹配教师" width="600px">
      <el-form :model="matchForm" label-width="100px">
        <el-form-item label="选择教师">
          <el-select v-model="matchForm.tutorId" placeholder="请选择教师" style="width: 100%;" filterable>
            <el-option 
              v-for="tutor in tutorList" 
              :key="tutor.id" 
              :label="`${tutor.name} - ${tutor.subject}`" 
              :value="tutor.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="matchForm.remark" type="textarea" rows="3" placeholder="匹配备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="matchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitMatch">确认匹配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const matchDialogVisible = ref(false)
const currentDemand = ref({})
const viewMode = ref('all')

const searchForm = reactive({
  keyword: '',
  subject: '',
  status: ''
})

const matchForm = reactive({
  demandId: null,
  tutorId: null,
  remark: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 模拟教师列表
const tutorList = ref([
  { id: 1, name: '李老师', subject: '数学', phone: '13900000001' },
  { id: 2, name: '王老师', subject: '英语', phone: '13900000002' },
  { id: 3, name: '张老师', subject: '物理', phone: '13900000003' }
])

// 模拟数据
const tableData = ref([
  { 
    id: 1, parentName: '张妈妈', phone: '13800138001', studentName: '张小明',
    grade: '初三', subject: '数学', expectPrice: 150, frequency: '每周2次',
    teacherGender: '男', address: '北京市海淀区中关村大街1号',
    description: '孩子数学基础较差，需要巩固提高',
    status: 'pending', createTime: '2026-01-15 10:00:00',
    matchedTutor: null
  },
  { 
    id: 2, parentName: '李爸爸', phone: '13800138002', studentName: '李小华',
    grade: '高一', subject: '英语', expectPrice: 200, frequency: '每周3次',
    teacherGender: null, address: '北京市朝阳区建国路88号',
    description: '英语听力和口语需要加强',
    status: 'matched', createTime: '2026-01-14 15:30:00',
    matchedTutor: { name: '王老师', phone: '13900000002', subject: '英语', matchTime: '2026-01-14 16:00:00' }
  },
  { 
    id: 3, parentName: '王女士', phone: '13800138003', studentName: '王小红',
    grade: '小学五年级', subject: '语文', expectPrice: 120, frequency: '每周2次',
    teacherGender: '女', address: '北京市西城区西单大街10号',
    description: '语文阅读理解需要提升',
    status: 'closed', createTime: '2026-01-10 09:00:00',
    matchedTutor: null
  }
])

const filteredData = computed(() => {
  if (viewMode.value === 'all') return tableData.value
  return tableData.value.filter(item => item.status === viewMode.value)
})

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = 50
    loading.value = false
  }, 500)
}

const getStatusType = (status) => {
  const map = {
    'pending': 'warning',
    'matched': 'success',
    'closed': 'info',
    'expired': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'pending': '待匹配',
    'matched': '已匹配',
    'closed': '已关闭',
    'expired': '已过期'
  }
  return map[status] || status
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.subject = ''
  searchForm.status = ''
  handleSearch()
}

const handleView = (row) => {
  currentDemand.value = { ...row }
  dialogVisible.value = true
}

const handleMatch = (row) => {
  matchForm.demandId = row.id
  matchForm.tutorId = null
  matchForm.remark = ''
  matchDialogVisible.value = true
}

const submitMatch = async () => {
  if (!matchForm.tutorId) {
    ElMessage.warning('请选择教师')
    return
  }
  ElMessage.success('匹配成功')
  matchDialogVisible.value = false
  fetchData()
}

const handleClose = async (row) => {
  await ElMessageBox.confirm('确定要关闭该需求吗？', '提示', { type: 'warning' })
  ElMessage.success('需求已关闭')
  fetchData()
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
