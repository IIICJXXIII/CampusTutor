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
      <div class="order-info-card">
        <div class="order-title">
          {{ order.subject }} · {{ order.grade }} 辅导
        </div>
        <div class="order-desc">
          课时单价: ¥{{ order.unitPrice }}/小时 | 共{{ order.totalHours }}课时
        </div>
      </div>
      
      <div v-if="order.status === 0" class="countdown-card">
        <el-icon><Clock /></el-icon>
        <span>请在 <strong>{{ countdown }}</strong> 内完成支付，超时订单将自动取消</span>
      </div>

      <div class="payment-mode-card">
        <h3 class="card-title">支付方式选择</h3>
        <div class="mode-options">
          <div 
            class="mode-item"
            :class="{ active: paymentMode === 'per_lesson' }"
            @click="paymentMode = 'per_lesson'"
          >
            <div class="mode-icon">
              <el-icon :size="24"><Calendar /></el-icon>
            </div>
            <div class="mode-info">
              <div class="mode-name">按课时支付</div>
              <div class="mode-desc">每节课单独付费，灵活便捷</div>
            </div>
            <el-icon v-if="paymentMode === 'per_lesson'" class="check-icon"><Check /></el-icon>
          </div>
          <div 
            class="mode-item"
            :class="{ active: paymentMode === 'full' }"
            @click="paymentMode = 'full'"
          >
            <div class="mode-icon">
              <el-icon :size="24"><Wallet /></el-icon>
            </div>
            <div class="mode-info">
              <div class="mode-name">一次性支付</div>
              <div class="mode-desc">一次性支付全部课时费用</div>
            </div>
            <el-icon v-if="paymentMode === 'full'" class="check-icon"><Check /></el-icon>
          </div>
        </div>
      </div>

      <div v-if="paymentMode === 'per_lesson'" class="lesson-count-card">
        <h3 class="card-title">选择支付课时数</h3>
        <div class="lesson-selector">
          <el-button 
            v-for="n in Math.min(5, order.totalHours - (order.paidHours || 0))" 
            :key="n"
            :type="lessonCount === n ? 'primary' : 'default'"
            @click="lessonCount = n"
            round
          >
            {{ n }}课时
          </el-button>
        </div>
        <div class="lesson-info">
          已支付: {{ order.paidHours || 0 }}课时 / 共{{ order.totalHours }}课时
        </div>
      </div>
      
      <div class="fee-card">
        <h3 class="card-title">费用明细</h3>
        <div class="fee-list">
          <div class="fee-item">
            <span class="label">课时单价</span>
            <span class="value">¥{{ order.unitPrice }}/小时</span>
          </div>
          <div v-if="paymentMode === 'per_lesson'" class="fee-item">
            <span class="label">支付课时数</span>
            <span class="value">{{ lessonCount }}课时</span>
          </div>
          <div class="fee-item">
            <span class="label">{{ paymentMode === 'per_lesson' ? '本次课时费' : '总课时费' }}</span>
            <span class="value">¥{{ currentPayAmount.toFixed(2) }}</span>
          </div>
          <div class="fee-item">
            <span class="label">平台服务费({{ (SERVICE_FEE_RATE * 100).toFixed(0) }}%)</span>
            <span class="value">¥{{ currentServiceFee.toFixed(2) }}</span>
          </div>
        </div>
        <div class="total-amount">
          <span class="label">应付金额</span>
          <span class="amount">¥{{ currentPayAmount.toFixed(2) }}</span>
        </div>
      </div>
      
      <div class="payment-card">
        <h3 class="card-title">支付渠道</h3>
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
        </div>
      </div>
      
      <div v-if="paymentMethod === 'balance' && balance < currentPayAmount" class="balance-warning">
        <el-icon><Warning /></el-icon>
        <span>余额不足，请先充值</span>
        <el-button type="primary" link @click="goToRecharge">去充值</el-button>
      </div>
      
      <div class="action-bar">
        <div class="pay-info">
          <span class="label">应付金额</span>
          <span class="amount">¥{{ currentPayAmount.toFixed(2) }}</span>
        </div>
        <el-button 
          type="primary" 
          size="large" 
          :loading="paying"
          :disabled="paymentMethod === 'balance' && balance < currentPayAmount"
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
import { ArrowLeft, Clock, Wallet, Check, Warning, Calendar } from '@element-plus/icons-vue'
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
const paymentMode = ref('per_lesson')
const lessonCount = ref(1)
const remainingSeconds = ref(24 * 60 * 60)
let countdownTimer = null

const SERVICE_FEE_RATE = 0.10

const currentPayAmount = computed(() => {
  if (!order.value) return 0
  if (paymentMode.value === 'full') {
    return order.value.totalAmount || 0
  }
  return (order.value.unitPrice || 0) * lessonCount.value
})

const currentServiceFee = computed(() => {
  return currentPayAmount.value * SERVICE_FEE_RATE
})

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
      if (order.value.paymentMode === 'full') {
        paymentMode.value = 'full'
      } else {
        paymentMode.value = 'per_lesson'
      }
      if (order.value.createTime && order.value.status === 0) {
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

const goBack = () => router.back()
const goToRecharge = () => router.push('/parent/recharge')

const handlePay = async () => {
  if (paying.value) return
  paying.value = true
  try {
    const modeText = paymentMode.value === 'per_lesson'
      ? `按课时支付${lessonCount.value}节课`
      : '一次性全额支付'
    await ElMessageBox.confirm(
      `确认使用余额${modeText}，支付 ¥${currentPayAmount.value.toFixed(2)}？`,
      '确认支付'
    )

    const res = await payOrder({
      orderId: order.value.id,
      payType: 1,
      paymentMode: paymentMode.value,
      lessonCount: paymentMode.value === 'per_lesson' ? lessonCount.value : order.value.totalHours
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

.payment-mode-card,
.lesson-count-card,
.fee-card,
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

.mode-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mode-item {
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
  
  .mode-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }
  
  .mode-info {
    flex: 1;
    
    .mode-name {
      font-weight: 600;
      font-size: 15px;
    }
    
    .mode-desc {
      font-size: 13px;
      color: #909399;
      margin-top: 4px;
    }
  }
  
  .check-icon {
    color: var(--el-color-primary);
    font-size: 20px;
  }
}

.lesson-selector {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.lesson-info {
  font-size: 13px;
  color: #909399;
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
  padding: 16px 20px;
  margin-top: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
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
