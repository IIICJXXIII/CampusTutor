<template>
  <div class="withdraw-container">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="90px" class="withdraw-form">
      <el-form-item label="提现金额" prop="amount">
        <el-input v-model.number="form.amount" placeholder="请输入提现金额" type="number" min="1" />
      </el-form-item>
      <el-form-item label="提现渠道" prop="channel">
        <el-select v-model="form.channel" placeholder="请选择渠道">
          <el-option label="微信" value="wechat" />
          <el-option label="支付宝" value="alipay" />
          <el-option label="银行卡" value="bank" />
        </el-select>
      </el-form-item>
      <el-form-item label="账号" prop="account">
        <el-input v-model="form.account" placeholder="请输入账号" />
      </el-form-item>
      <el-form-item label="支付密码" prop="payPassword">
        <el-input v-model="form.payPassword" placeholder="请输入支付密码" type="password" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSubmit" :loading="loading">提交申请</el-button>
        <el-button @click="goBack">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { withdraw } from '@/api/wallet'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const form = ref({
  amount: '',
  channel: '',
  account: '',
  payPassword: ''
})
const rules = {
  amount: [ { required: true, message: '请输入金额', trigger: 'blur' } ],
  channel: [ { required: true, message: '请选择渠道', trigger: 'change' } ],
  account: [ { required: true, message: '请输入账号', trigger: 'blur' } ],
  payPassword: [ { required: true, message: '请输入支付密码', trigger: 'blur' } ]
}
const loading = ref(false)
const onSubmit = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await withdraw(form.value)
      ElMessage.success('提现申请已提交')
      router.push('/wallet')
    } catch (e) {
      ElMessage.error(e?.response?.data?.message || '提现失败')
    } finally {
      loading.value = false
    }
  })
}
const goBack = () => {
  router.back()
}
</script>

<style scoped>
.withdraw-container {
  max-width: 400px;
  margin: 48px auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px #f0f1f2;
  padding: 32px 24px;
}
.withdraw-form {
  margin-top: 16px;
}
</style>
