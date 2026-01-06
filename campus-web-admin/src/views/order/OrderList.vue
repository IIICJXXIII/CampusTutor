<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card card-shadow pending">
          <div class="stat-icon">
            <el-icon size="32"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-label">待支付</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-shadow active">
          <div class="stat-icon">
            <el-icon size="32"><VideoPlay /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.active }}</div>
            <div class="stat-label">进行中</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-shadow completed">
          <div class="stat-icon">
            <el-icon size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.completed }}</div>
            <div class="stat-label">已完成</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-shadow refund">
          <div class="stat-icon">
            <el-icon size="32"><RefreshLeft /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.refund }}</div>
            <div class="stat-label">退款中</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 搜索表单 -->
    <div class="search-form card-shadow">
      <el-form :model="searchForm" inline>
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="订单号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px;">
            <el-option label="待支付" value="pending" />
            <el-option label="已支付" value="paid" />
            <el-option label="进行中" value="active" />
            <el-option label="已完成" value="completed" />
            <el-option label="退款中" value="refunding" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker 
            v-model="searchForm.dateRange" 
            type="daterange" 
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <div class="table-container card-shadow">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="parentName" label="家长" width="100" />
        <el-table-column prop="tutorName" label="教师" width="100" />
        <el-table-column prop="subject" label="科目" width="80" />
        <el-table-column prop="lessonCount" label="课时数" width="80" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="escrowAmount" label="托管金额" width="120">
          <template #default="{ row }">
            <span class="escrow">¥{{ row.escrowAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="handleView(row)">详情</el-button>
            <el-button text type="warning" v-if="row.status === 'refunding'" @click="handleRefund(row)">
              退款审核
            </el-button>
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
    
    <!-- 订单详情弹窗 -->
    <el-dialog v-model="dialogVisible" title="订单详情" width="800px">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(currentOrder.status)">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="家长">{{ currentOrder.parentName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentOrder.parentPhone }}</el-descriptions-item>
            <el-descriptions-item label="教师">{{ currentOrder.tutorName }}</el-descriptions-item>
            <el-descriptions-item label="教师电话">{{ currentOrder.tutorPhone }}</el-descriptions-item>
            <el-descriptions-item label="科目">{{ currentOrder.subject }}</el-descriptions-item>
            <el-descriptions-item label="课时单价">¥{{ currentOrder.lessonPrice }}/课时</el-descriptions-item>
            <el-descriptions-item label="购买课时">{{ currentOrder.lessonCount }} 课时</el-descriptions-item>
            <el-descriptions-item label="已完成">{{ currentOrder.completedCount || 0 }} 课时</el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="amount">¥{{ currentOrder.totalAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="托管金额">
              <span class="escrow">¥{{ currentOrder.escrowAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        
        <el-tab-pane label="课时记录" name="lessons">
          <el-table :data="currentOrder.lessons || []" size="small" border>
            <el-table-column prop="lessonNo" label="课时" width="80" />
            <el-table-column prop="date" label="上课日期" />
            <el-table-column prop="startTime" label="开始时间" />
            <el-table-column prop="endTime" label="结束时间" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="row.status === 'completed' ? 'success' : 'info'" size="small">
                  {{ row.status === 'completed' ? '已完成' : '待上课' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        
        <el-tab-pane label="支付信息" name="payment">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="支付方式">{{ currentOrder.paymentMethod || '微信支付' }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ currentOrder.payTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="交易流水号">{{ currentOrder.transactionId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="支付金额">¥{{ currentOrder.totalAmount }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
    
    <!-- 退款审核弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="退款审核" width="500px">
      <el-form :model="refundForm" label-width="100px">
        <el-form-item label="退款金额">
          <el-input-number v-model="refundForm.amount" :min="0" :max="currentOrder.escrowAmount" />
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="refundForm.approved">
            <el-radio :value="true">同意退款</el-radio>
            <el-radio :value="false">拒绝退款</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="refundForm.remark" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, VideoPlay, CircleCheck, RefreshLeft, Download } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const refundDialogVisible = ref(false)
const currentOrder = ref({})
const activeTab = ref('basic')

const stats = reactive({
  pending: 5,
  active: 23,
  completed: 156,
  refund: 2
})

const searchForm = reactive({
  orderNo: '',
  status: '',
  dateRange: null
})

const refundForm = reactive({
  orderId: null,
  amount: 0,
  approved: true,
  remark: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 模拟数据
const tableData = ref([
  { 
    id: 1, orderNo: 'ORD20260115001', parentName: '张妈妈', parentPhone: '13800138001',
    tutorName: '李老师', tutorPhone: '13900000001', subject: '数学',
    lessonCount: 10, lessonPrice: 150, totalAmount: 1500, escrowAmount: 1200,
    completedCount: 3, status: 'active', createTime: '2026-01-15 10:00:00',
    payTime: '2026-01-15 10:05:00', transactionId: 'WX20260115100500001',
    lessons: [
      { lessonNo: 1, date: '2026-01-16', startTime: '14:00', endTime: '16:00', status: 'completed' },
      { lessonNo: 2, date: '2026-01-18', startTime: '14:00', endTime: '16:00', status: 'completed' },
      { lessonNo: 3, date: '2026-01-20', startTime: '14:00', endTime: '16:00', status: 'completed' },
      { lessonNo: 4, date: '2026-01-22', startTime: '14:00', endTime: '16:00', status: 'pending' }
    ]
  },
  { 
    id: 2, orderNo: 'ORD20260114001', parentName: '李爸爸', parentPhone: '13800138002',
    tutorName: '王老师', tutorPhone: '13900000002', subject: '英语',
    lessonCount: 20, lessonPrice: 200, totalAmount: 4000, escrowAmount: 3200,
    completedCount: 8, status: 'active', createTime: '2026-01-14 15:00:00',
    payTime: '2026-01-14 15:10:00', transactionId: 'WX20260114151000002'
  },
  { 
    id: 3, orderNo: 'ORD20260110001', parentName: '王女士', parentPhone: '13800138003',
    tutorName: '张老师', tutorPhone: '13900000003', subject: '物理',
    lessonCount: 5, lessonPrice: 180, totalAmount: 900, escrowAmount: 450,
    completedCount: 0, status: 'refunding', createTime: '2026-01-10 09:00:00',
    payTime: '2026-01-10 09:05:00', transactionId: 'WX20260110090500003'
  },
  { 
    id: 4, orderNo: 'ORD20260108001', parentName: '赵先生', parentPhone: '13800138004',
    tutorName: '陈老师', tutorPhone: '13900000004', subject: '语文',
    lessonCount: 10, lessonPrice: 120, totalAmount: 1200, escrowAmount: 0,
    completedCount: 10, status: 'completed', createTime: '2026-01-08 11:00:00',
    payTime: '2026-01-08 11:05:00', transactionId: 'WX20260108110500004'
  }
])

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  setTimeout(() => {
    pagination.total = 100
    loading.value = false
  }, 500)
}

const getStatusType = (status) => {
  const map = {
    'pending': 'warning',
    'paid': 'primary',
    'active': 'success',
    'completed': 'success',
    'refunding': 'danger',
    'cancelled': 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'pending': '待支付',
    'paid': '已支付',
    'active': '进行中',
    'completed': '已完成',
    'refunding': '退款中',
    'cancelled': '已取消'
  }
  return map[status] || status
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  searchForm.dateRange = null
  handleSearch()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

const handleView = (row) => {
  currentOrder.value = { ...row }
  activeTab.value = 'basic'
  dialogVisible.value = true
}

const handleRefund = (row) => {
  currentOrder.value = { ...row }
  refundForm.orderId = row.id
  refundForm.amount = row.escrowAmount
  refundForm.approved = true
  refundForm.remark = ''
  refundDialogVisible.value = true
}

const submitRefund = async () => {
  ElMessage.success(refundForm.approved ? '退款已通过' : '退款已拒绝')
  refundDialogVisible.value = false
  fetchData()
}
</script>

<style lang="scss" scoped>
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  background: #fff;
  
  &.pending {
    .stat-icon { background: linear-gradient(135deg, #f6d365, #fda085); }
    .stat-value { color: #f6d365; }
  }
  
  &.active {
    .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
    .stat-value { color: #667eea; }
  }
  
  &.completed {
    .stat-icon { background: linear-gradient(135deg, #11998e, #38ef7d); }
    .stat-value { color: #11998e; }
  }
  
  &.refund {
    .stat-icon { background: linear-gradient(135deg, #eb3349, #f45c43); }
    .stat-value { color: #eb3349; }
  }
  
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    margin-right: 16px;
  }
  
  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 700;
    }
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.amount {
  color: #e6a23c;
  font-weight: 600;
}

.escrow {
  color: #409eff;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
