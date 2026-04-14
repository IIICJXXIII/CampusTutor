<template>
  <div class="order-pay-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">订单支付</h1>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <template v-else-if="order">
      <!-- 订单信息 -->
      <div class="order-info-card">
        <div class="order-title">
          {{ order.subject }} · {{ order.grade }} 辅导
        </div>
        <div class="order-desc">
          教师: {{ order.tutorName }} | {{ order.frequency }} | {{ order.teachingMode }}
        </div>
      </div>
      
      <!-- 倒计时 -->
      <div class="countdown-card">
        <el-icon><Clock /></el-icon>
        <span>请在 <strong>{{ countdown }}</strong> 内完成支付，超时订单将自动取消</span>
      </div>
      
      <!-- 费用明细 -->
      <div class="fee-card">
        <h3 class="card-title">费用明细</h3>
        <div class="fee-list">
          <div class="fee-item">
            <span class="label">预估课时费</span>
            <span class="value">¥{{ (order.estimatedAmount || 0).toFixed(2) }}</span>
          </div>
          <div class="fee-item">
            <span class="label">平台服务费</span>
            <span class="value">¥{{ (order.serviceFee || 0).toFixed(2) }}</span>
          </div>
        </div>
        <div class="total-amount">
          <span class="label">应付金额</span>
          <span class="amount">¥{{ (order.totalAmount || 0).toFixed(2) }}</span>
        </div>
      </div>
      
      <!-- 支付方式 -->
      <div class="payment-card">
        <h3 class="card-title">支付方式</h3>
        <div class="payment-methods">
          <div 
            class="payment-item"
            :class="{ active: paymentMethod === 'balance' }"
            @click="paymentMethod = 'balance'"
          >
            <div class="method-icon wallet">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="method-info">
              <div class="method-name">余额支付</div>
              <div class="method-balance">可用余额: ¥{{ balance.toFixed(2) }}</div>
            </div>
            <el-icon v-if="paymentMethod === 'balance'" class="check-icon"><Check /></el-icon>
          </div>
          
          <div 
            class="payment-item"
            :class="{ active: paymentMethod === 'wechat' }"
            @click="paymentMethod = 'wechat'"
          >
            <div class="method-icon wechat">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="method-info">
              <div class="method-name">微信支付</div>
            </div>
            <el-icon v-if="paymentMethod === 'wechat'" class="check-icon"><Check /></el-icon>
          </div>
          
          <div 
            class="payment-item"
            :class="{ active: paymentMethod === 'alipay' }"
            @click="paymentMethod = 'alipay'"
          >
            <div class="method-icon alipay">
              <el-icon><CreditCard /></el-icon>
            </div>
            <div class="method-info">
              <div class="method-name">支付宝</div>
            </div>
            <el-icon v-if="paymentMethod === 'alipay'" class="check-icon"><Check /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 余额不足提示 -->
      <div v-if="paymentMethod === 'balance' && balance < order.totalAmount" class="balance-warning">
        <el-icon><Warning /></el-icon>
        <span>余额不足，请选择其他支付方式或先充值</span>
        <el-button type="primary" link @click="goToRecharge">去充值</el-button>
      </div>
      
      <!-- 底部支付按钮 -->
      <div class="action-bar">
        <div class="pay-info">
          <span class="label">应付金额</span>
          <span class="amount">¥{{ (order.totalAmount || 0).toFixed(2) }}</span>
        </div>
        <el-button 
          type="primary" 
          size="large" 
          :loading="paying"
          :disabled="paymentMethod === 'balance' && balance < order.totalAmount"
          @click="handlePay"
        >
          确认支付
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Clock, Wallet, ChatDotRound, CreditCard, Check, Warning } from '@element-plus/icons-vue'
import { getOrderDetail, payOrder } from '@shared/api/order'
import { getWalletInfo } from '@shared/api/wallet'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const paying = ref(false)
const order = ref(null)
const balance = ref(0)
const paymentMethod = ref('balance')
const remainingSeconds = ref(24 * 60 * 60)
let countdownTimer = null

const countdown = computed(() => {
  const hours = Math.floor(remainingSeconds.value / 3600)
  const minutes = Math.floor((remainingSeconds.value % 3600) / 60)
  const seconds = remainingSeconds.value % 60
  return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
})

const loadData = async () => {
  loading.value = true
  try {
    const [orderRes, walletRes] = await Promise.all([
      getOrderDetail(route.params.id),
      getWalletInfo()
    ])
    
    if (orderRes.code === 200) {
      order.value = orderRes.data
      // 计算剩余支付时间
      if (order.value.createTime) {
        const createTime = dayjs(order.value.createTime)
        const expireTime = createTime.add(24, 'hour')
        const now = dayjs()
        remainingSeconds.value = Math.max(0, expireTime.diff(now, 'second'))
        startCountdown()
      }
    }
    
    if (walletRes.code === 200) {
      balance.value = walletRes.data?.balance || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const startCountdown = () => {
  countdownTimer = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    } else {
      clearInterval(countdownTimer)
      ElMessage.warning('支付超时，订单已自动取消')
      router.back()
    }
  }, 1000)
}

const goBack = () => {
  router.back()
}

const goToRecharge = () => {
  router.push('/parent/recharge')
}

const handlePay = async () => {
  try {
    await ElMessageBox.confirm(
      `确认使用${paymentMethod.value === 'balance' ? '余额' : paymentMethod.value === 'wechat' ? '微信' : '支付宝'}支付 ¥${order.value.totalAmount.toFixed(2)}？`,
      '确认支付'
    )
    
    paying.value = true
    // 根据后端API，payType: 1钱包 2微信 3支付宝
    const payTypeMap = { balance: 1, wechat: 2, alipay: 3 }
    const res = await payOrder({
      orderId: route.params.id,
      payType: payTypeMap[paymentMethod.value]
    })
    
    if (res.code === 200) {
      ElMessage.success('支付成功')
      router.replace(`/parent/orders/${route.params.id}`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error('支付失败，请重试')
    }
  } finally {
    paying.value = false
  }
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style lang="scss" scoped>
.order-pay-page {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
  padding-bottom: 100px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
  }
}

.loading-container {
  padding: 40px;
  background: #fff;
  border-radius: 12px;
}

.order-info-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  
  .order-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 8px;
  }
  
  .order-desc {
    font-size: 14px;
    color: #666;
  }
}

.countdown-card {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #fff3cd;
  border-radius: 8px;
  margin-bottom: 16px;
  color: #856404;
  
  strong {
    color: #f56c6c;
  }
}

.fee-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }
  
  .fee-list {
    margin-bottom: 16px;
    
    .fee-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      color: #666;
    }
  }
  
  .total-amount {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
    
    .label {
      font-weight: 600;
    }
    
    .amount {
      font-size: 24px;
      font-weight: 700;
      color: #f56c6c;
    }
  }
}

.payment-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px;
  }
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  
  &.active {
    border-color: var(--el-color-primary);
    background: #f0f7ff;
  }
  
  .method-icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: #fff;
    
    &.wallet {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    &.wechat {
      background: #07c160;
    }
    
    &.alipay {
      background: #1677ff;
    }
  }
  
  .method-info {
    flex: 1;
    
    .method-name {
      font-weight: 600;
    }
    
    .method-balance {
      font-size: 13px;
      color: #666;
      margin-top: 2px;
    }
  }
  
  .check-icon {
    color: var(--el-color-primary);
    font-size: 20px;
  }
}

.balance-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fef0f0;
  border-radius: 8px;
  color: #f56c6c;
  margin-bottom: 16px;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px;
  background: #fff;
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .pay-info {
    .label {
      font-size: 13px;
      color: #666;
      margin-right: 8px;
    }
    
    .amount {
      font-size: 24px;
      font-weight: 700;
      color: #f56c6c;
    }
  }
  
  .el-button {
    min-width: 140px;
  }
}
</style>
