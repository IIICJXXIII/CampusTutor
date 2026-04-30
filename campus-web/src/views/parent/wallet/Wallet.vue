<template>
  <div class="wallet-page">
    <div class="page-header">
      <h1 class="page-title">我的钱包</h1>
    </div>
    
    <!-- 余额卡片 -->
    <div class="balance-card">
      <div class="balance-label">账户余额</div>
      <div class="balance-amount">
        <span class="currency">¥</span>
        {{ walletInfo.balance?.toFixed(2) || '0.00' }}
      </div>
      <div class="balance-actions">
        <el-button type="primary" @click="goRecharge">充值</el-button>
      </div>
    </div>
    
    <!-- 功能入口 -->
    <div class="action-grid">
      <div class="action-item" @click="showRecords('all')">
        <el-icon><Tickets /></el-icon>
        <span>全部记录</span>
      </div>
      <div class="action-item" @click="showRecords('recharge')">
        <el-icon><Plus /></el-icon>
        <span>充值记录</span>
      </div>
      <div class="action-item" @click="showRecords('pay')">
        <el-icon><Minus /></el-icon>
        <span>支出记录</span>
      </div>
    </div>
    
    <!-- 最近记录 -->
    <div class="recent-section">
      <div class="section-header">
        <h3>最近交易</h3>
        <el-button link type="primary" @click="showRecords('all')">
          查看全部
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="records.length === 0" class="empty-container">
        <el-empty description="暂无交易记录" :image-size="100" />
      </div>
      
      <div v-else class="record-list">
        <div
          v-for="record in records"
          :key="record.id"
          class="record-item"
        >
          <div class="record-icon" :class="record.type">
            <el-icon v-if="record.type === 'recharge'"><Plus /></el-icon>
            <el-icon v-else><Minus /></el-icon>
          </div>
          <div class="record-info">
            <div class="record-title">{{ record.title }}</div>
            <div class="record-time">{{ formatTime(record.createTime) }}</div>
          </div>
          <div class="record-amount" :class="record.type">
            {{ record.type === 'recharge' ? '+' : '-' }}{{ record.amount.toFixed(2) }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 交易记录抽屉 -->
    <el-drawer
      v-model="showDrawer"
      :title="drawerTitle"
      direction="rtl"
      size="100%"
    >
      <div class="drawer-content">
        <div v-if="drawerLoading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>
        
        <div v-else-if="allRecords.length === 0" class="empty-container">
          <el-empty description="暂无记录" />
        </div>
        
        <div v-else class="record-list">
          <div
            v-for="record in allRecords"
            :key="record.id"
            class="record-item"
          >
            <div class="record-icon" :class="record.type">
              <el-icon v-if="record.type === 'recharge'"><Plus /></el-icon>
              <el-icon v-else><Minus /></el-icon>
            </div>
            <div class="record-info">
              <div class="record-title">{{ record.title }}</div>
              <div class="record-time">{{ formatTime(record.createTime) }}</div>
            </div>
            <div class="record-amount" :class="record.type">
              {{ record.type === 'recharge' ? '+' : '-' }}{{ record.amount.toFixed(2) }}
            </div>
          </div>
        </div>
        
        <div v-if="hasMoreRecords" class="load-more">
          <el-button :loading="loadingMore" @click="loadMoreRecords">
            加载更多
          </el-button>
        </div>
      </div>
    </el-drawer>
    
    <!-- 充值弹窗 -->
    <el-dialog v-model="showRecharge" title="充值" width="400px">
      <div class="recharge-options">
        <div
          v-for="amount in rechargeAmounts"
          :key="amount"
          class="amount-option"
          :class="{ active: rechargeAmount === amount }"
          @click="rechargeAmount = amount"
        >
          ¥{{ amount }}
        </div>
      </div>
      <el-input
        v-model.number="customAmount"
        type="number"
        placeholder="自定义金额"
        :min="1"
        class="custom-input"
        @input="rechargeAmount = null"
      >
        <template #prepend>¥</template>
      </el-input>
      <template #footer>
        <el-button @click="showRecharge = false">取消</el-button>
        <el-button type="primary" @click="doRecharge">
          确认充值 ¥{{ rechargeAmount || customAmount || 0 }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Tickets, Plus, Minus, ArrowRight } from '@element-plus/icons-vue'
import { getWalletInfo, getTransactions, recharge } from '@shared/api/wallet'
import dayjs from 'dayjs'

const loading = ref(false)
const walletInfo = ref({})
const records = ref([])

// 抽屉相关
const showDrawer = ref(false)
const drawerTitle = ref('交易记录')
const drawerLoading = ref(false)
const allRecords = ref([])
const recordType = ref('all')
const recordPage = ref(1)
const hasMoreRecords = ref(false)
const loadingMore = ref(false)

// 充值相关
const showRecharge = ref(false)
const rechargeAmount = ref(null)
const customAmount = ref(null)
const rechargeAmounts = [50, 100, 200, 500, 1000]

const formatTime = (time) => {
  return dayjs(time).format('MM-DD HH:mm')
}

const loadWallet = async () => {
  loading.value = true
  try {
    const res = await getWalletInfo()
    if (res.code === 200) {
      walletInfo.value = res.data || {}
    }
    
    const recordRes = await getTransactions({ page: 1, size: 5 })
    if (recordRes.code === 200) {
      records.value = recordRes.data?.records || []
    }
  } catch (error) {
    console.error('加载钱包失败:', error)
  } finally {
    loading.value = false
  }
}

const showRecords = async (type) => {
  recordType.value = type
  drawerTitle.value = type === 'all' ? '全部记录' : type === 'recharge' ? '充值记录' : '支出记录'
  showDrawer.value = true
  recordPage.value = 1
  allRecords.value = []
  await loadRecords()
}

const loadRecords = async () => {
  drawerLoading.value = recordPage.value === 1
  loadingMore.value = recordPage.value > 1
  
  try {
    const params = { page: recordPage.value, size: 20 }
    if (recordType.value !== 'all') {
      params.type = recordType.value
    }
    
    const res = await getTransactions(params)
    if (res.code === 200) {
      const newRecords = res.data?.records || []
      allRecords.value = [...allRecords.value, ...newRecords]
      hasMoreRecords.value = newRecords.length >= 20
    }
  } catch (error) {
    console.error('加载记录失败:', error)
  } finally {
    drawerLoading.value = false
    loadingMore.value = false
  }
}

const loadMoreRecords = () => {
  recordPage.value++
  loadRecords()
}

const goRecharge = () => {
  rechargeAmount.value = null
  customAmount.value = null
  showRecharge.value = true
}

const doRecharge = async () => {
  const amount = rechargeAmount.value || customAmount.value
  if (!amount || amount <= 0) {
    ElMessage.warning('请选择或输入充值金额')
    return
  }
  
  try {
    const res = await recharge(amount)
    if (res.code === 200) {
      // 跳转到支付页面或显示支付二维码
      if (res.data?.payUrl) {
        window.open(res.data.payUrl)
      } else {
        ElMessage.success('充值成功')
        showRecharge.value = false
        loadWallet()
      }
    }
  } catch (error) {
    console.error('充值失败:', error)
    ElMessage.error('充值失败')
  }
}

onMounted(() => {
  loadWallet()
})
</script>

<style lang="scss" scoped>
.wallet-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  color: #fff;
  text-align: center;
  margin-bottom: 20px;
  
  .balance-label {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 8px;
  }
  
  .balance-amount {
    font-size: 40px;
    font-weight: 600;
    margin-bottom: 24px;
    
    .currency {
      font-size: 24px;
      margin-right: 4px;
    }
  }
  
  .balance-actions {
    .el-button {
      background: rgba(255, 255, 255, 0.2);
      border: none;
      color: #fff;
      min-width: 120px;
      
      &:hover {
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  
  .action-item {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    }
    
    .el-icon {
      font-size: 28px;
      color: #409eff;
      margin-bottom: 8px;
    }
    
    span {
      display: block;
      font-size: 14px;
      color: #666;
    }
  }
}

.recent-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h3 {
      margin: 0;
      font-size: 16px;
    }
  }
}

.loading-container,
.empty-container {
  padding: 40px 0;
}

.record-list {
  .record-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 0;
    
    &:not(:last-child) {
      border-bottom: 1px solid #f5f5f5;
    }
  }
  
  .record-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.recharge {
      background: #e8f5e9;
      color: #4caf50;
    }
    
    &.pay {
      background: #fff3e0;
      color: #ff9800;
    }
  }
  
  .record-info {
    flex: 1;
    
    .record-title {
      font-size: 15px;
      margin-bottom: 4px;
    }
    
    .record-time {
      font-size: 12px;
      color: #999;
    }
  }
  
  .record-amount {
    font-size: 16px;
    font-weight: 600;
    
    &.recharge {
      color: #4caf50;
    }
    
    &.pay {
      color: #ff9800;
    }
  }
}

.drawer-content {
  padding: 0 16px;
}

.load-more {
  text-align: center;
  padding: 20px 0;
}

.recharge-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
  
  .amount-option {
    padding: 16px;
    border: 2px solid #eee;
    border-radius: 8px;
    text-align: center;
    font-size: 18px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      border-color: #409eff;
    }
    
    &.active {
      border-color: #409eff;
      background: #ecf5ff;
      color: #409eff;
    }
  }
}

.custom-input {
  margin-top: 12px;
}
</style>
