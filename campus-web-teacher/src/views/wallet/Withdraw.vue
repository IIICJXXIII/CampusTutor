<template>
  <div class="withdraw-page">
    <el-page-header @back="goBack">
      <template #content>申请提现</template>
    </el-page-header>
    
    <div class="withdraw-container">
      <!-- 可提现金额 -->
      <div class="balance-info">
        <p class="label">可提现金额</p>
        <h2 class="amount">¥{{ walletInfo.balance?.toFixed(2) || '0.00' }}</h2>
      </div>
      
      <!-- 提现表单 -->
      <div class="withdraw-form">
        <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
          <el-form-item label="提现金额" prop="amount">
            <el-input
              v-model="form.amount"
              type="number"
              placeholder="请输入提现金额"
              size="large"
            >
              <template #prefix>¥</template>
            </el-input>
            <div class="quick-amounts">
              <el-button 
                v-for="amt in quickAmounts" 
                :key="amt" 
                size="small"
                :type="form.amount == amt ? 'primary' : 'default'"
                @click="form.amount = amt"
              >
                {{ amt }}元
              </el-button>
              <el-button size="small" @click="form.amount = walletInfo.balance">全部</el-button>
            </div>
          </el-form-item>
          
          <el-form-item label="提现方式" prop="method">
            <el-radio-group v-model="form.method" class="method-group">
              <el-radio-button label="wechat">
                <el-icon><ChatDotRound /></el-icon>微信
              </el-radio-button>
              <el-radio-button label="alipay">
                <el-icon><Wallet /></el-icon>支付宝
              </el-radio-button>
              <el-radio-button label="bank">
                <el-icon><CreditCard /></el-icon>银行卡
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
          
          <!-- 银行卡信息 -->
          <template v-if="form.method === 'bank'">
            <el-form-item label="银行卡号" prop="bankCard">
              <el-input v-model="form.bankCard" placeholder="请输入银行卡号" />
            </el-form-item>
            <el-form-item label="开户银行" prop="bankName">
              <el-select v-model="form.bankName" placeholder="请选择开户银行" style="width: 100%">
                <el-option label="中国工商银行" value="中国工商银行" />
                <el-option label="中国建设银行" value="中国建设银行" />
                <el-option label="中国农业银行" value="中国农业银行" />
                <el-option label="中国银行" value="中国银行" />
                <el-option label="招商银行" value="招商银行" />
                <el-option label="交通银行" value="交通银行" />
              </el-select>
            </el-form-item>
            <el-form-item label="持卡人姓名" prop="cardHolder">
              <el-input v-model="form.cardHolder" placeholder="请输入持卡人姓名" />
            </el-form-item>
          </template>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              :disabled="!canWithdraw"
              style="width: 100%"
              @click="handleSubmit"
            >
              确认提现
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 提现说明 -->
      <div class="withdraw-tips">
        <h4>提现说明</h4>
        <ul>
          <li>单笔最低提现金额为 10 元</li>
          <li>提现将在 1-3 个工作日内到账</li>
          <li>每月免费提现次数为 3 次，超出后收取 1% 手续费</li>
          <li>如有问题请联系客服</li>
        </ul>
      </div>
      
      <!-- 提现记录 -->
      <div class="withdraw-history">
        <h4>提现记录</h4>
        <div v-if="withdrawals.length" class="history-list">
          <div v-for="item in withdrawals" :key="item.id" class="history-item">
            <div class="history-info">
              <p class="amount">¥{{ item.amount.toFixed(2) }}</p>
              <p class="time">{{ formatTime(item.createTime) }}</p>
            </div>
            <el-tag :type="getStatusType(item.status)" size="small">
              {{ getStatusText(item.status) }}
            </el-tag>
          </div>
        </div>
        <el-empty v-else description="暂无提现记录" :image-size="40" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Wallet, CreditCard } from '@element-plus/icons-vue'
import { getWalletInfo, applyWithdraw, getWithdrawals } from '@shared/api/wallet'
import dayjs from 'dayjs'

const router = useRouter()
const formRef = ref(null)

const walletInfo = ref({})
const withdrawals = ref([])
const submitting = ref(false)
const quickAmounts = [50, 100, 200, 500]

const form = reactive({
  amount: '',
  method: 'wechat',
  bankCard: '',
  bankName: '',
  cardHolder: ''
})

const rules = {
  amount: [
    { required: true, message: '请输入提现金额', trigger: 'blur' }
  ],
  method: [
    { required: true, message: '请选择提现方式', trigger: 'change' }
  ],
  bankCard: [
    { required: true, message: '请输入银行卡号', trigger: 'blur' }
  ],
  bankName: [
    { required: true, message: '请选择开户银行', trigger: 'change' }
  ],
  cardHolder: [
    { required: true, message: '请输入持卡人姓名', trigger: 'blur' }
  ]
}

const canWithdraw = computed(() => {
  const amount = parseFloat(form.amount)
  return amount >= 10 && amount <= (walletInfo.value.balance || 0)
})

const getStatusType = (status) => {
  const map = { 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '处理中', 2: '已到账', 3: '已拒绝' }
  return map[status] || '未知'
}

const formatTime = (time) => dayjs(time).format('MM-DD HH:mm')

const loadWalletInfo = async () => {
  try {
    const res = await getWalletInfo()
    if (res.code === 200) {
      walletInfo.value = res.data || {}
    }
  } catch (error) {
    console.error('加载钱包信息失败', error)
  }
}

const loadWithdrawals = async () => {
  try {
    const res = await getWithdrawals({ page: 1, pageSize: 5 })
    if (res.code === 200) {
      withdrawals.value = res.data?.list || []
    }
  } catch (error) {
    console.error('加载提现记录失败', error)
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    const amount = parseFloat(form.amount)
    if (amount < 10) {
      ElMessage.warning('最低提现金额为10元')
      return
    }
    if (amount > walletInfo.value.balance) {
      ElMessage.warning('提现金额不能超过可用余额')
      return
    }
    
    submitting.value = true
    // 后端API参数为 { amount, channel(Integer), accountNo, accountName, payPassword }
    // channel: 1-微信, 2-支付宝, 3-银行卡
    const channelMap = { wechat: 1, alipay: 2, bank: 3 }
    const res = await applyWithdraw({
      amount,
      channel: channelMap[form.method],        // 转换为Integer
      accountNo: form.method === 'bank' ? form.bankCard : form.method,  // 银行卡号或微信/支付宝标识
      accountName: form.cardHolder || ''       // 后端字段名是 accountName (非 accountHolder)
    })
    
    if (res.code === 200) {
      ElMessage.success('提现申请已提交')
      router.back()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '提现失败')
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadWalletInfo()
  loadWithdrawals()
})
</script>

<style lang="scss" scoped>
.withdraw-page {
  max-width: 500px;
  margin: 0 auto;
  
  .withdraw-container {
    margin-top: 24px;
  }
  
  .balance-info {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    text-align: center;
    margin-bottom: 16px;
    
    .label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 8px;
    }
    
    .amount {
      font-size: 32px;
      font-weight: 700;
      color: #409eff;
    }
  }
  
  .withdraw-form {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 16px;
    
    .quick-amounts {
      display: flex;
      gap: 8px;
      margin-top: 12px;
      flex-wrap: wrap;
    }
    
    .method-group {
      display: flex;
      width: 100%;
      
      .el-radio-button {
        flex: 1;
        
        :deep(.el-radio-button__inner) {
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 6px;
        }
      }
    }
  }
  
  .withdraw-tips {
    background: #fff9e6;
    border-radius: 12px;
    padding: 16px 20px;
    margin-bottom: 16px;
    
    h4 {
      font-size: 14px;
      font-weight: 600;
      color: #e6a23c;
      margin-bottom: 12px;
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        font-size: 13px;
        color: #606266;
        margin-bottom: 6px;
      }
    }
  }
  
  .withdraw-history {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    
    h4 {
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 16px;
    }
    
    .history-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .history-info {
        .amount {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 4px;
        }
        
        .time {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}
</style>
