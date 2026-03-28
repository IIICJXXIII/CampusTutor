<template>
  <div class="wallet-page">
    <div class="page-header">
      <h1 class="page-title">我的钱包</h1>
    </div>
    
    <!-- 余额卡片 -->
    <div class="balance-card">
      <div class="balance-info">
        <p class="label">可用余额 (元)</p>
        <h2 class="amount">{{ walletInfo.balance?.toFixed(2) || '0.00' }}</h2>
      </div>
      <div class="balance-actions">
        <el-button type="primary" @click="goToWithdraw">提现</el-button>
      </div>
      <div class="balance-stats">
        <div class="stat-item">
          <span class="value">¥{{ walletInfo.totalIncome?.toFixed(2) || '0.00' }}</span>
          <span class="label">累计收入</span>
        </div>
        <div class="stat-item">
          <span class="value">¥{{ walletInfo.totalWithdraw?.toFixed(2) || '0.00' }}</span>
          <span class="label">累计提现</span>
        </div>
        <div class="stat-item">
          <span class="value">¥{{ walletInfo.freezeAmount?.toFixed(2) || '0.00' }}</span>
          <span class="label">冻结金额</span>
        </div>
      </div>
    </div>
    
    <!-- 交易记录 -->
    <div class="transaction-section">
      <div class="section-header">
        <h3>交易记录</h3>
        <el-select v-model="filter.type" placeholder="全部类型" clearable size="small" @change="loadTransactions">
          <el-option label="收入" :value="1" />
          <el-option label="提现" :value="2" />
        </el-select>
      </div>
      
      <div v-loading="loading" class="transaction-list">
        <div v-if="transactions.length" class="transactions">
          <div v-for="item in transactions" :key="item.id" class="transaction-item">
            <div class="trans-icon" :class="item.type === 1 ? 'income' : 'expense'">
              <el-icon>
                <component :is="item.type === 1 ? 'Plus' : 'Minus'" />
              </el-icon>
            </div>
            <div class="trans-info">
              <h4>{{ item.title }}</h4>
              <p>{{ formatTime(item.createTime) }}</p>
            </div>
            <div class="trans-amount" :class="item.type === 1 ? 'income' : 'expense'">
              {{ item.type === 1 ? '+' : '-' }}¥{{ item.amount.toFixed(2) }}
            </div>
          </div>
        </div>
        
        <el-empty v-else description="暂无交易记录" />
        
        <div v-if="hasMore" class="load-more">
          <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Minus } from '@element-plus/icons-vue'
import { useWalletStore } from '@shared/stores'
import { getWalletInfo, getTransactions } from '@shared/api/wallet'
import { formatDate } from '@shared/utils'

const router = useRouter()
const walletStore = useWalletStore()

const loading = ref(false)
const loadingMore = ref(false)
const walletInfo = ref({})
const transactions = ref([])
const page = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

const filter = reactive({
  type: null
})

const formatTime = (time) => formatDate(time, 'YYYY-MM-DD HH:mm')

const loadWalletInfo = async () => {
  try {
    const res = await getWalletInfo()
    if (res.code === 200) {
      walletInfo.value = res.data || {}
      walletStore.setWalletInfo(res.data)
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  }
}

const loadTransactions = async (reset = true) => {
  if (reset) {
    page.value = 1
    transactions.value = []
    hasMore.value = true
  }
  
  loading.value = true
  try {
    const res = await getTransactions({
      type: filter.type,
      page: page.value,
      pageSize: pageSize.value
    })
    
    if (res.code === 200) {
      const list = res.data?.list || []
      transactions.value = reset ? list : [...transactions.value, ...list]
      hasMore.value = list.length === pageSize.value
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  page.value++
  loadingMore.value = true
  await loadTransactions(false)
  loadingMore.value = false
}

const goToWithdraw = () => {
  router.push('/withdraw')
}

onMounted(() => {
  loadWalletInfo()
  loadTransactions()
})
</script>

<style lang="scss" scoped>
.wallet-page {
  max-width: 600px;
  margin: 0 auto;
  
  .balance-card {
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    border-radius: 16px;
    padding: 24px;
    color: #fff;
    margin-bottom: 24px;
    
    .balance-info {
      text-align: center;
      margin-bottom: 20px;
      
      .label {
        font-size: 14px;
        opacity: 0.9;
        margin-bottom: 8px;
      }
      
      .amount {
        font-size: 40px;
        font-weight: 700;
      }
    }
    
    .balance-actions {
      text-align: center;
      margin-bottom: 20px;
      
      .el-button {
        background: rgba(255, 255, 255, 0.2);
        border: 1px solid rgba(255, 255, 255, 0.3);
        color: #fff;
        
        &:hover {
          background: rgba(255, 255, 255, 0.3);
        }
      }
    }
    
    .balance-stats {
      display: flex;
      justify-content: space-around;
      padding-top: 20px;
      border-top: 1px solid rgba(255, 255, 255, 0.2);
      
      .stat-item {
        text-align: center;
        
        .value {
          display: block;
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 4px;
        }
        
        .label {
          font-size: 12px;
          opacity: 0.8;
        }
      }
    }
  }
  
  .transaction-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
      }
    }
    
    .transaction-item {
      display: flex;
      align-items: center;
      padding: 16px 0;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .trans-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        
        &.income {
          background: #e7f7ef;
          color: #67c23a;
        }
        
        &.expense {
          background: #fef0f0;
          color: #f56c6c;
        }
      }
      
      .trans-info {
        flex: 1;
        
        h4 {
          font-size: 14px;
          font-weight: 500;
          margin-bottom: 4px;
        }
        
        p {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .trans-amount {
        font-size: 16px;
        font-weight: 600;
        
        &.income {
          color: #67c23a;
        }
        
        &.expense {
          color: #f56c6c;
        }
      }
    }
    
    .load-more {
      text-align: center;
      padding-top: 16px;
    }
  }
}
</style>
