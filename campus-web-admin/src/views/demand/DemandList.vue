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
            <el-option label="钢琴/乐器陪练" value="钢琴/乐器陪练" />
            <el-option label="美术/书法" value="美术/书法" />
            <el-option label="声乐/视唱练耳" value="声乐/视唱练耳" />
            <el-option label="中考体育专项" value="中考体育专项" />
            <el-option label="羽毛球/网球陪练" value="羽毛球/网球陪练" />
            <el-option label="篮球/足球指导" value="篮球/足球指导" />
            <el-option label="少儿编程" value="少儿编程(Scratch/Python)" />
            <el-option label="机器人/3D打印" value="机器人/3D打印" />
            <el-option label="科学实验/航模" value="科学实验/航模" />
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
              v-for="sub in ['钢琴/乐器陪练', '美术/书法', '中考体育专项']"
              :key="sub" 
              :label="`随机分配教师 - ${sub}`" 
              :value="Math.floor(Math.random() * 1000)" 
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

const generateDemands = () => {
  const subjects = ['钢琴/乐器陪练', '美术/书法', '声乐/视唱练耳', '中考体育专项', '羽毛球/网球陪练', '篮球/足球指导', '少儿编程(Scratch/Python)', '机器人/3D打印', '科学实验/航模']
  const grades = ['4-6岁', '7-9岁', '10-12岁', '13-15岁', '16-18岁']
  const statuses = ['pending', 'matched', 'closed', 'expired']
  const firstNames = ['赵', '钱', '孙', '李', '周', '吴', '郑', '王', '陈', '林', '张']
  
  return Array.from({ length: 50 }, (_, i) => {
    const isMatched = Math.random() > 0.5
    const status = isMatched ? 'matched' : statuses[Math.floor(Math.random() * statuses.length)]
    const subject = subjects[Math.floor(Math.random() * subjects.length)]
    
    return {
      id: i + 1,
      parentName: firstNames[Math.floor(Math.random() * firstNames.length)] + '家长',
      phone: '13800' + String(Math.floor(Math.random() * 899999 + 100000)),
      studentName: firstNames[Math.floor(Math.random() * firstNames.length)] + '小' + ['明', '红', '华', '刚', '强', '丽'][Math.floor(Math.random() * 6)],
      grade: grades[Math.floor(Math.random() * grades.length)],
      subject: subject,
      expectPrice: Math.floor(Math.random() * 20) * 10 + 100, // 100~300
      frequency: '每周' + (Math.floor(Math.random() * 3) + 1) + '次',
      teacherGender: Math.random() > 0.7 ? (Math.random() > 0.5 ? '男' : '女') : null,
      address: ['海淀区中关村', '朝阳区建国门', '西城区单大街', '东城区王府井', '南山区科技园'][Math.floor(Math.random() * 5)] + (Math.floor(Math.random() * 100) + 1) + '号',
      description: '希望找到一位有经验的老师辅导' + subject,
      status: status,
      createTime: `2026-01-${String(Math.floor(Math.random() * 28 + 1)).padStart(2, '0')} 10:00:00`,
      matchedTutor: status === 'matched' ? {
        name: firstNames[Math.floor(Math.random() * firstNames.length)] + '老师',
        phone: '13900' + String(Math.floor(Math.random() * 899999 + 100000)),
        subject: subject,
        matchTime: '2026-01-15 10:00:00'
      } : null
    }
  })
}

// 模拟数据
const tableData = ref(generateDemands())

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
