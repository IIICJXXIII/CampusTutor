<template>
  <div class="wallet-container">
    <div class="wallet-header">
      <div class="balance-block">
        <div class="label">余额(元)</div>
        <div class="amount">{{ wallet.balance || '0.00' }}</div>
      </div>
      <div class="frozen-block">
        <div class="label">冻结金额</div>
        <div class="amount frozen">{{ wallet.frozen || '0.00' }}</div>
      </div>
      <el-button type="primary" class="withdraw-btn" @click="goWithdraw">提现</el-button>
    </div>
    <el-divider>交易流水</el-divider>
    <el-table :data="transactions" style="width: 100%" v-loading="loading">
      <el-table-column prop="type" label="类型" width="100"/>
      <el-table-column prop="amount" label="金额(元)" width="120"/>
      <el-table-column prop="status" label="状态" width="100"/>
      <el-table-column prop="createdAt" label="时间"/>
      <el-table-column prop="remark" label="备注"/>
    </el-table>
    <el-pagination
      background
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page.sync="page"
      @current-change="fetchTransactions"
      class="pagination"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWallet, getTransactions } from '@/api/wallet'
import { useRouter } from 'vue-router'

const wallet = ref({})
const transactions = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = 10
const total = ref(0)
const router = useRouter()

const fetchWallet = async () => {
  const { data } = await getWallet()
  wallet.value = data || {}
}
const fetchTransactions = async (curPage = 1) => {
  loading.value = true
  const { data } = await getTransactions({ page: curPage, pageSize })
  transactions.value = data?.records || []
  total.value = data?.total || 0
  loading.value = false
}
const goWithdraw = () => {
  router.push('/wallet/withdraw')
}
onMounted(() => {
  fetchWallet()
  fetchTransactions()
})
</script>

<style scoped>
.wallet-container {
  max-width: 600px;
  margin: 32px auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px #f0f1f2;
  padding: 32px 24px;
}
.wallet-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}
.balance-block, .frozen-block {
  text-align: center;
}
.amount {
  font-size: 2rem;
  font-weight: bold;
  color: #409eff;
}
.amount.frozen {
  color: #f56c6c;
}
.withdraw-btn {
  margin-left: 32px;
}
.pagination {
  margin-top: 24px;
  text-align: right;
}
</style>
