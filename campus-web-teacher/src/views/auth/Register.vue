<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        <h1>教师注册</h1>
        <p class="subtitle">加入素质教育平台，成为优秀教员</p>
      </div>
      
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        class="register-form"
      >
        <el-form-item prop="nickname">
          <el-input 
            v-model="form.nickname" 
            placeholder="请输入昵称"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入手机号"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item prop="code">
          <div class="code-input">
            <el-input 
              v-model="form.code" 
              placeholder="请输入验证码"
              size="large"
              :prefix-icon="Message"
            />
            <el-button 
              size="large"
              :disabled="countdown > 0"
              @click="handleSendCode"
            >
              {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请设置密码 (至少6位)"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请确认密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading"
            class="register-btn"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        
        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Phone, Message, Lock } from '@element-plus/icons-vue'
import { register, sendCode } from '@shared/api/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const countdown = ref(0)

const form = reactive({
  nickname: '',
  phone: '',
  code: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度2-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSendCode = async () => {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    await sendCode(form.phone)
    ElMessage.success('验证码已发送')
    
    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  }
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const res = await register({
      phone: form.phone,
      password: form.password,
      code: form.code,
      nickname: form.nickname,
      role: 1 // 教师角色
    })
    
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    }
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  padding: 24px;
}

.register-container {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 48px 32px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  
  @media (max-width: 480px) {
    padding: 32px 24px;
  }
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
  
  .logo {
    width: 64px;
    height: 64px;
    margin-bottom: 16px;
  }
  
  h1 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }
  
  .subtitle {
    font-size: 14px;
    color: #909399;
  }
}

.code-input {
  display: flex;
  gap: 12px;
  width: 100%;
  
  .el-input {
    flex: 1;
  }
}

.register-form {
  .register-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    border-radius: 8px;
  }
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
  
  a {
    color: #409eff;
    margin-left: 4px;
  }
}
</style>
