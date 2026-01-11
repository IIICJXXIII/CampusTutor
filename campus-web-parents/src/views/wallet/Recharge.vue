<template>
  <div class="recharge-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">充值</h1>
    </div>
    
    <!-- 当前余额 -->
    <div class="balance-card">
      <div class="balance-label">当前余额</div>
      <div class="balance-amount">¥{{ balance.toFixed(2) }}</div>
    </div>
    
    <!-- 充值金额选择 -->
    <div class="amount-section">
      <h3 class="section-title">选择充值金额</h3>
      <div class="amount-grid">
        <div
          v-for="amount in presetAmounts"
          :key="amount"
          class="amount-item"
          :class="{ active: selectedAmount === amount }"
          @click="selectAmount(amount)"
        >
          <div class="amount-value">¥{{ amount }}</div>
        </div>
      </div>
      
      <div class="custom-amount">
        <el-input
          v-model.number="customAmount"
          type="number"
          placeholder="其他金额"
          :min="1"
          @focus="selectedAmount = null"
        >
          <template #prepend>¥</template>
        </el-input>
      </div>
    </div>
    
    <!-- 支付方式 -->
    <div class="payment-section">
      <h3 class="section-title">选择支付方式</h3>
      <div class="payment-list">
        <div
          v-for="method in paymentMethods"
          :key="method.value"
          class="payment-item"
          :class="{ active: paymentMethod === method.value }"
          @click="paymentMethod = method.value"
        >
          <div class="payment-icon">
            <el-icon v-if="method.value === 'wechat'" style="color: #07c160"><ChatDotRound /></el-icon>
            <el-icon v-else style="color: #1677ff"><CreditCard /></el-icon>
          </div>
          <div class="payment-name">{{ method.label }}</div>
          <el-icon v-if="paymentMethod === method.value" class="check-icon"><Check /></el-icon>
        </div>
      </div>
    </div>
    
    <!-- 充值按钮 -->
    <div class="action-section">
      <el-button
        type="primary"
        size="large"
        :loading="submitting"
        :disabled="!finalAmount || finalAmount <= 0"
        @click="handleRecharge"
      >
        立即充值 ¥{{ finalAmount || 0 }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ChatDotRound, CreditCard, Check } from '@element-plus/icons-vue'
import { getWalletInfo, recharge } from '@shared/api/wallet'

const router = useRouter()

const balance = ref(0)
const selectedAmount = ref(100)
const customAmount = ref(null)
const paymentMethod = ref('wechat')
const submitting = ref(false)

const presetAmounts = [50, 100, 200, 500, 1000, 2000]

const paymentMethods = [
  { value: 'wechat', label: '微信支付' },
  { value: 'alipay', label: '支付宝' }
]

const finalAmount = computed(() => {
  return selectedAmount.value || customAmount.value || 0
})

const goBack = () => router.back()

const selectAmount = (amount) => {
  selectedAmount.value = amount
  customAmount.value = null
}

const loadBalance = async () => {
  try {
    const res = await getWalletInfo()
    if (res.code === 200) {
      balance.value = res.data?.balance || 0
    }
  } catch (error) {
    console.error('加载余额失败:', error)
  }
}

const handleRecharge = async () => {
  if (!finalAmount.value || finalAmount.value <= 0) {
    ElMessage.warning('请选择或输入充值金额')
    return
  }
  
  submitting.value = true
  try {
    const res = await recharge({
      amount: finalAmount.value,
      paymentMethod: paymentMethod.value
    })
    
    if (res.code === 200) {
      if (res.data?.payUrl) {
        // 跳转支付
        window.location.href = res.data.payUrl
      } else if (res.data?.qrCode) {
        // 显示二维码
        ElMessage.info('请使用手机扫描二维码完成支付')
      } else {
        ElMessage.success('充值成功')
        router.back()
      }
    }
  } catch (error) {
    console.error('充值失败:', error)
    ElMessage.error('充值失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadBalance()
})
</script>

<style lang="scss" scoped>
.recharge-page {
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

.balance-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  color: #fff;
  text-align: center;
  margin-bottom: 24px;
  
  .balance-label {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 8px;
  }
  
  .balance-amount {
    font-size: 32px;
    font-weight: 600;
  }
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px;
}

.amount-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .amount-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .amount-item {
    padding: 16px;
    border: 2px solid #eee;
    border-radius: 8px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      border-color: #409eff;
    }
    
    &.active {
      border-color: #409eff;
      background: #ecf5ff;
    }
    
    .amount-value {
      font-size: 18px;
      font-weight: 600;
    }
  }
}

.payment-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  
  .payment-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .payment-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    border: 2px solid #eee;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      border-color: #409eff;
    }
    
    &.active {
      border-color: #409eff;
      background: #ecf5ff;
    }
    
    .payment-icon {
      font-size: 24px;
    }
    
    .payment-name {
      flex: 1;
      font-weight: 500;
    }
    
    .check-icon {
      color: #409eff;
    }
  }
}

.action-section {
  text-align: center;
  
  .el-button {
    width: 100%;
    max-width: 300px;
  }
}
</style>
