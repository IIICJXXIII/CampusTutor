<template>
  <div class="page-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card card-shadow">
          <div class="stat-icon total">
            <el-icon size="28"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.totalBalance) }}</div>
            <div class="stat-label">平台托管总额</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-shadow">
          <div class="stat-icon income">
            <el-icon size="28"><Top /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.todayIncome) }}</div>
            <div class="stat-label">今日收入</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-shadow">
          <div class="stat-icon expense">
            <el-icon size="28"><Bottom /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(stats.todayExpense) }}</div>
            <div class="stat-label">今日支出</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card card-shadow">
          <div class="stat-icon pending">
            <el-icon size="28"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingWithdraw }}</div>
            <div class="stat-label">待处理提现</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 搜索表单 -->
    <div class="search-form card-shadow">
      <el-form :model="searchForm" inline>
        <el-form-item label="用户">
          <el-input v-model="searchForm.keyword" placeholder="用户名/手机号" clearable style="width: 160px;" />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="searchForm.userType" placeholder="全部" clearable style="width: 120px;">
            <el-option label="教师" value="tutor" />
            <el-option label="家长" value="parent" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易类型">
          <el-select v-model="searchForm.transType" placeholder="全部" clearable style="width: 120px;">
            <el-option label="充值" value="recharge" />
            <el-option label="支付" value="pay" />
            <el-option label="提现" value="withdraw" />
            <el-option label="退款" value="refund" />
            <el-option label="收入" value="income" />
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
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="wallet-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="交易记录" name="transactions">
        <div class="table-container card-shadow">
          <el-table :data="transactionData" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="transNo" label="交易号" width="200" />
            <el-table-column prop="userName" label="用户" width="100" />
            <el-table-column prop="userType" label="用户类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.userType === 'tutor' ? 'success' : 'primary'" size="small">
                  {{ row.userType === 'tutor' ? '教师' : '家长' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="交易类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getTransTypeStyle(row.type)" size="small">
                  {{ getTransTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="金额" width="120">
              <template #default="{ row }">
                <span :class="row.amount > 0 ? 'amount-in' : 'amount-out'">
                  {{ row.amount > 0 ? '+' : '' }}¥{{ Math.abs(row.amount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="余额" width="120">
              <template #default="{ row }">
                ¥{{ row.balance }}
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" show-overflow-tooltip />
            <el-table-column prop="createTime" label="交易时间" width="160" />
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
      </el-tab-pane>
      
      <el-tab-pane label="提现申请" name="withdraw">
        <div class="table-container card-shadow">
          <el-table :data="withdrawData" v-loading="loading" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="userName" label="用户" width="100" />
            <el-table-column prop="phone" label="手机号" width="140" />
            <el-table-column prop="amount" label="提现金额" width="120">
              <template #default="{ row }">
                <span class="amount-out">¥{{ row.amount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="bankName" label="银行" width="140" />
            <el-table-column prop="bankAccount" label="银行卡号" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getWithdrawStatusType(row.status)" size="small">
                  {{ getWithdrawStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="申请时间" width="160" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status === 'pending'">
                  <el-button text type="success" @click="handleApprove(row)">通过</el-button>
                  <el-button text type="danger" @click="handleReject(row)">拒绝</el-button>
                </template>
                <el-button v-else text type="primary" @click="handleViewWithdraw(row)">查看</el-button>
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
            />
          </div>
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="用户钱包" name="wallets">
        <div class="table-container card-shadow">
          <el-table :data="walletData" v-loading="loading" stripe>
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="userName" label="用户名" width="120" />
            <el-table-column prop="phone" label="手机号" width="140" />
            <el-table-column prop="userType" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.userType === 'tutor' ? 'success' : 'primary'" size="small">
                  {{ row.userType === 'tutor' ? '教师' : '家长' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="可用余额" width="120">
              <template #default="{ row }">
                <span style="color: #67c23a; font-weight: 600;">¥{{ row.balance }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="frozenAmount" label="冻结金额" width="120">
              <template #default="{ row }">
                <span style="color: #909399;">¥{{ row.frozenAmount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="totalIncome" label="累计收入" width="120">
              <template #default="{ row }">
                ¥{{ row.totalIncome }}
              </template>
            </el-table-column>
            <el-table-column prop="totalExpense" label="累计支出" width="120">
              <template #default="{ row }">
                ¥{{ row.totalExpense }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button text type="primary" @click="handleViewWallet(row)">详情</el-button>
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
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 提现审核弹窗 -->
    <el-dialog v-model="withdrawDialogVisible" title="提现审核" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户">{{ currentWithdraw.userName }}</el-descriptions-item>
        <el-descriptions-item label="提现金额">
          <span class="amount-out">¥{{ currentWithdraw.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="银行">{{ currentWithdraw.bankName }}</el-descriptions-item>
        <el-descriptions-item label="银行卡号">{{ currentWithdraw.bankAccount }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentWithdraw.createTime }}</el-descriptions-item>
      </el-descriptions>
      
      <el-form style="margin-top: 20px;" label-width="80px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="withdrawForm.approved">
            <el-radio :value="true">通过</el-radio>
            <el-radio :value="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" v-if="!withdrawForm.approved">
          <el-input v-model="withdrawForm.remark" type="textarea" rows="3" placeholder="拒绝原因" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="withdrawDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWithdrawReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Wallet, Top, Bottom, Clock } from '@element-plus/icons-vue'

const loading = ref(false)
const activeTab = ref('transactions')
const withdrawDialogVisible = ref(false)
const currentWithdraw = ref({})

const stats = reactive({
  totalBalance: 156800,
  todayIncome: 5600,
  todayExpense: 3200,
  pendingWithdraw: 3
})

const searchForm = reactive({
  keyword: '',
  userType: '',
  transType: '',
  dateRange: null
})

const withdrawForm = reactive({
  approved: true,
  remark: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 交易记录数据
const transactionData = ref([
  { id: 1, transNo: 'TRX20260116001', userName: '张妈妈', userType: 'parent', type: 'pay', amount: -1500, balance: 3500, remark: '订单支付 ORD20260115001', createTime: '2026-01-16 10:00:00' },
  { id: 2, transNo: 'TRX20260116002', userName: '李老师', userType: 'tutor', type: 'income', amount: 450, balance: 2800, remark: '课时收入结算', createTime: '2026-01-16 11:00:00' },
  { id: 3, transNo: 'TRX20260116003', userName: '王女士', userType: 'parent', type: 'refund', amount: 900, balance: 2900, remark: '订单退款', createTime: '2026-01-16 14:00:00' },
  { id: 4, transNo: 'TRX20260115001', userName: '李老师', userType: 'tutor', type: 'withdraw', amount: -500, balance: 2350, remark: '提现到银行卡', createTime: '2026-01-15 16:00:00' }
])

// 提现申请数据
const withdrawData = ref([
  { id: 1, userName: '李老师', phone: '13900000001', amount: 800, bankName: '工商银行', bankAccount: '622202****1234', status: 'pending', createTime: '2026-01-16 09:00:00' },
  { id: 2, userName: '王老师', phone: '13900000002', amount: 500, bankName: '建设银行', bankAccount: '622700****5678', status: 'pending', createTime: '2026-01-16 10:30:00' },
  { id: 3, userName: '张老师', phone: '13900000003', amount: 1200, bankName: '招商银行', bankAccount: '621483****9012', status: 'approved', createTime: '2026-01-15 14:00:00' },
  { id: 4, userName: '陈老师', phone: '13900000004', amount: 300, bankName: '农业银行', bankAccount: '622848****3456', status: 'rejected', createTime: '2026-01-14 11:00:00' }
])

// 用户钱包数据
const walletData = ref([
  { userId: 1, userName: '张妈妈', phone: '13800138001', userType: 'parent', balance: 3500, frozenAmount: 0, totalIncome: 0, totalExpense: 5500 },
  { userId: 2, userName: '李老师', phone: '13900000001', userType: 'tutor', balance: 2800, frozenAmount: 450, totalIncome: 8600, totalExpense: 5800 },
  { userId: 3, userName: '李爸爸', phone: '13800138002', userType: 'parent', balance: 1200, frozenAmount: 0, totalIncome: 0, totalExpense: 4800 },
  { userId: 4, userName: '王老师', phone: '13900000002', userType: 'tutor', balance: 4500, frozenAmount: 600, totalIncome: 12000, totalExpense: 7500 }
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

const formatMoney = (value) => {
  return value.toLocaleString()
}

const getTransTypeStyle = (type) => {
  const map = { 'recharge': 'success', 'pay': 'primary', 'withdraw': 'warning', 'refund': 'info', 'income': 'success' }
  return map[type] || 'info'
}

const getTransTypeText = (type) => {
  const map = { 'recharge': '充值', 'pay': '支付', 'withdraw': '提现', 'refund': '退款', 'income': '收入' }
  return map[type] || type
}

const getWithdrawStatusType = (status) => {
  const map = { 'pending': 'warning', 'approved': 'success', 'rejected': 'danger' }
  return map[status] || 'info'
}

const getWithdrawStatusText = (status) => {
  const map = { 'pending': '待处理', 'approved': '已通过', 'rejected': '已拒绝' }
  return map[status] || status
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.userType = ''
  searchForm.transType = ''
  searchForm.dateRange = null
  handleSearch()
}

const handleTabChange = () => {
  pagination.page = 1
  fetchData()
}

const handleApprove = async (row) => {
  await ElMessageBox.confirm('确定通过该提现申请吗？', '提示', { type: 'warning' })
  ElMessage.success('提现已通过')
}

const handleReject = (row) => {
  currentWithdraw.value = { ...row }
  withdrawForm.approved = false
  withdrawForm.remark = ''
  withdrawDialogVisible.value = true
}

const handleViewWithdraw = (row) => {
  currentWithdraw.value = { ...row }
  withdrawDialogVisible.value = true
}

const handleViewWallet = (row) => {
  // 跳转到用户钱包详情
  ElMessage.info('查看钱包详情功能开发中')
}

const submitWithdrawReview = async () => {
  ElMessage.success(withdrawForm.approved ? '提现已通过' : '提现已拒绝')
  withdrawDialogVisible.value = false
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
  
  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    margin-right: 16px;
    
    &.total { background: linear-gradient(135deg, #667eea, #764ba2); }
    &.income { background: linear-gradient(135deg, #11998e, #38ef7d); }
    &.expense { background: linear-gradient(135deg, #eb3349, #f45c43); }
    &.pending { background: linear-gradient(135deg, #f6d365, #fda085); }
  }
  
  .stat-info {
    .stat-value {
      font-size: 24px;
      font-weight: 700;
      color: #303133;
    }
    .stat-label {
      font-size: 14px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.wallet-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }
}

.amount-in {
  color: #67c23a;
  font-weight: 600;
}

.amount-out {
  color: #f56c6c;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
