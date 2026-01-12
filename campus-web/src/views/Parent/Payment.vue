<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { payOrder, getOrderDetail, confirmOrder } from '@/api/order'
import { useOrderStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()

const status = ref('pending') // pending, processing, success
const countdown = ref(899) // 倒计时秒数 (14:59)
const selectedPayment = ref('wechat')

// 订单数据
const orderId = route.query.orderId
const currentOrder = ref({
  id: orderId || 'ORD-DEMO',
  teacher: '演示老师',
  subject: '演示课程',
  amount: 2000,
  location: '线上教学',
  status: 0 // 订单状态
})

// 格式化倒计时
const formattedCountdown = computed(() => {
  const minutes = Math.floor(countdown.value / 60)
  const seconds = countdown.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// 支付方式选项
const paymentMethods = [
  { value: 'wechat', label: '微信支付', icon: 'ChatDotRound', color: '#07C160', desc: '亿万用户的选择，安全快捷' },
  { value: 'alipay', label: '支付宝', icon: 'Wallet', color: '#1677FF', desc: '支付宝安全支付' }
]

// 获取订单详情
onMounted(async () => {
  // 从本地store获取
  const orders = orderStore.orders
  const localOrder = orders.find(o => o.id === orderId)
  if (localOrder) {
    currentOrder.value = { ...currentOrder.value, ...localOrder }
  }

  // 尝试从API获取
  if (orderId && !orderId.startsWith('ORD-')) {
    try {
      const res = await getOrderDetail(orderId)
      if (res.data) {
        currentOrder.value = {
          id: res.data.id,
          teacher: res.data.tutorName || currentOrder.value.teacher,
          subject: res.data.subject || currentOrder.value.subject,
          amount: res.data.totalAmount || currentOrder.value.amount,
          location: res.data.location || '线上教学',
          status: res.data.status // 保存订单状态
        }
      }
    } catch (e) {
      console.log('使用本地订单数据')
    }
  }

  // 开始倒计时
  startCountdown()
})

// 倒计时
const startCountdown = () => {
  const timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      clearInterval(timer)
      ElMessage.warning('支付超时，请重新下单')
      router.back()
    }
  }, 1000)
}

// 支付方式映射：wechat -> 2, alipay -> 3, wallet -> 1
const getPayType = (method) => {
  const map = { wechat: 2, alipay: 3, wallet: 1 }
  return map[method] || 2
}

// 支付
const handlePay = async () => {
  status.value = 'processing'

  try {
    // 调用后端支付API
    if (orderId && !orderId.startsWith('ORD-')) {
      // 关键修正：如果订单状态是 -1（待确认），先确认订单
      if (currentOrder.value.status === -1) {
        console.log('订单状态为待确认，先确认订单...')
        try {
          await confirmOrder(orderId)
          console.log('订单确认成功')
        } catch (confirmError) {
          console.error('确认订单失败:', confirmError)
          throw new Error('确认订单失败: ' + (confirmError.message || '未知错误'))
        }
      }
      
      // Web端使用钱包支付 (payType=1)
      console.log('开始支付，订单ID:', orderId)
      try {
        const payType = 1 // 钱包支付
        await payOrder(orderId, payType)
        console.log('支付成功')
      } catch (payError) {
        console.error('支付API失败:', payError)
        throw new Error(payError.message || '支付失败')
      }
    }

    status.value = 'success'

    // 更新本地订单状态
    if (orderId) {
      orderStore.updateOrder(orderId, { status: 'active' })
    }

    ElMessage.success('支付成功！')
  } catch (error) {
    console.error('支付流程失败:', error)
    status.value = 'pending'
    ElMessage.error(error.message || '支付失败，请重试')
  }
}

// 查看课表
const goToSchedule = () => {
  router.replace('/process/record')
}

// 返回订单列表
const goToOrders = () => {
  router.replace('/mine/orders')
}
</script>

<template>
  <div class="payment-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button 
        v-if="status === 'pending'"
        text 
        @click="router.back()"
        class="back-btn"
      >
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">收银台</h1>
    </div>

    <!-- 待支付状态 -->
    <template v-if="status !== 'success'">
      <!-- 倒计时 -->
      <div class="countdown-section">
        <p class="countdown-label">支付剩余时间</p>
        <p class="countdown-value">{{ formattedCountdown }}</p>
      </div>

      <!-- 订单金额卡片 -->
      <el-card class="amount-card" shadow="never">
        <div class="amount-header">
          <p class="platform-name">易家教 - 资金托管账户</p>
          <div class="amount-value">¥{{ currentOrder.amount }}</div>
        </div>
        
        <el-divider style="margin: 16px 0" />
        
        <el-descriptions :column="1" size="small">
          <el-descriptions-item label="课程商品">
            <span class="desc-value bold">{{ currentOrder.subject }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="授课教师">
            {{ currentOrder.teacher }}
          </el-descriptions-item>
          <el-descriptions-item label="授课地点">
            <span class="location-text">
              <el-icon><Location /></el-icon>
              {{ currentOrder.location }}
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 支付方式 -->
      <el-card class="payment-card" shadow="never">
        <h3 class="card-title">选择支付方式</h3>
        
        <el-radio-group v-model="selectedPayment" class="payment-group">
          <div 
            v-for="method in paymentMethods" 
            :key="method.value"
            class="payment-item"
            :class="{ active: selectedPayment === method.value }"
            @click="selectedPayment = method.value"
          >
            <div class="payment-icon" :style="{ background: method.color }">
              <el-icon :size="20" color="#fff">
                <component :is="method.icon" />
              </el-icon>
            </div>
            <div class="payment-info">
              <p class="payment-name">{{ method.label }}</p>
              <p class="payment-desc">{{ method.desc }}</p>
            </div>
            <el-radio :value="method.value" />
          </div>
        </el-radio-group>
      </el-card>

      <!-- 安全提示 -->
      <div class="security-tip">
        <el-icon color="#67c23a"><ShieldCheck /></el-icon>
        <span>平台全程资金托管，确认课时后结算</span>
      </div>

      <!-- 支付按钮 -->
      <div class="bottom-action">
        <el-button 
          v-if="status === 'pending'"
          type="success" 
          size="large"
          @click="handlePay"
        >
          立即支付 ¥{{ currentOrder.amount }}
        </el-button>
        <el-button 
          v-else 
          type="info" 
          size="large" 
          disabled
          :loading="true"
        >
          安全处理中...
        </el-button>
      </div>
    </template>

    <!-- 支付成功 -->
    <div v-else class="success-content">
      <div class="success-icon">
        <el-icon :size="48" color="#fff"><Check /></el-icon>
      </div>
      
      <h2 class="success-title">支付成功</h2>
      <p class="success-desc">订单金额已冻结至托管账户</p>

      <el-card class="escrow-card" shadow="never">
        <div class="escrow-info">
          <div class="escrow-icon">
            <el-icon :size="20" color="var(--el-color-primary)"><Lock /></el-icon>
          </div>
          <div class="escrow-text">
            <p class="escrow-title">资金托管中</p>
            <p class="escrow-desc">每次课后家长确认，资金分批到账</p>
          </div>
        </div>
      </el-card>

      <div class="success-actions">
        <el-button type="primary" size="large" @click="goToSchedule">
          查看课表
        </el-button>
        <el-button size="large" @click="goToOrders">
          返回订单列表
        </el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.payment-page {
  min-height: 100vh;
  background: $bg-light;
  display: flex;
  flex-direction: column;
}

.page-header {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border-bottom: 1px solid $border-color;

  .back-btn {
    position: absolute;
    left: $spacing-md;
  }

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }
}

.countdown-section {
  text-align: center;
  padding: $spacing-lg;

  .countdown-label {
    font-size: 14px;
    color: $text-muted;
  }

  .countdown-value {
    font-size: 28px;
    font-weight: 700;
    font-family: 'SF Mono', monospace;
    color: $text-primary;
    margin-top: $spacing-xs;
  }
}

.amount-card {
  margin: 0 $spacing-lg $spacing-lg;
  border-radius: 16px;

  .amount-header {
    text-align: center;

    .platform-name {
      font-size: 12px;
      color: $text-muted;
      margin-bottom: $spacing-xs;
    }

    .amount-value {
      font-size: 40px;
      font-weight: 700;
      color: $text-primary;
    }
  }

  .desc-value {
    &.bold {
      font-weight: 600;
    }
  }

  .location-text {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.payment-card {
  margin: 0 $spacing-lg $spacing-lg;
  border-radius: 16px;

  .card-title {
    font-size: 14px;
    font-weight: 600;
    color: $text-muted;
    margin-bottom: $spacing-md;
  }

  .payment-group {
    display: flex;
    flex-direction: column;
    width: 100%;
  }

  .payment-item {
    display: flex;
    align-items: center;
    padding: $spacing-md;
    border-radius: 12px;
    margin-bottom: $spacing-sm;
    border: 2px solid transparent;
    cursor: pointer;
    transition: all 0.3s;

    &:hover, &.active {
      background: rgba($primary-color, 0.05);
      border-color: $primary-color;
    }

    .payment-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .payment-info {
      flex: 1;
      margin-left: $spacing-md;

      .payment-name {
        font-size: 15px;
        font-weight: 600;
        color: $text-primary;
      }

      .payment-desc {
        font-size: 11px;
        color: $text-muted;
        margin-top: 2px;
      }
    }

    :deep(.el-radio) {
      margin-right: 0;
    }
  }
}

.security-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-xs;
  font-size: 12px;
  color: $text-muted;
  margin-top: $spacing-lg;
}

.bottom-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid $border-color;

  .el-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
  }
}

.success-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl;
  background: #fff;

  .success-icon {
    width: 80px;
    height: 80px;
    background: $success-color;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 10px 30px rgba($success-color, 0.3);
    margin-bottom: $spacing-lg;
  }

  .success-title {
    font-size: 24px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: $spacing-xs;
  }

  .success-desc {
    font-size: 14px;
    color: $text-muted;
  }

  .escrow-card {
    width: 100%;
    margin: $spacing-xl 0;
    border-radius: 12px;
    background: $bg-light;

    .escrow-info {
      display: flex;
      align-items: center;
      gap: $spacing-md;

      .escrow-icon {
        width: 40px;
        height: 40px;
        background: #fff;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: $shadow-sm;
      }

      .escrow-text {
        .escrow-title {
          font-size: 14px;
          font-weight: 600;
          color: $text-primary;
        }

        .escrow-desc {
          font-size: 12px;
          color: $text-muted;
          margin-top: 2px;
        }
      }
    }
  }

  .success-actions {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
    margin-top: auto;

    .el-button {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 12px;
    }
  }
}
</style>
