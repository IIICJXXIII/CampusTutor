<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        <h1>注册家长账号</h1>
        <p>开始为孩子寻找优质老师</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef" class="register-form">
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item prop="code">
          <el-input
            v-model="form.code"
            placeholder="请输入验证码"
            size="large"
            :prefix-icon="Message"
          >
            <template #append>
              <el-button :disabled="countdown > 0" @click="sendCode">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请设置密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="agreed">
            我已阅读并同意
            <a href="#">《用户协议》</a>和
            <a href="#">《隐私政策》</a>
          </el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="!agreed"
            style="width: 100%"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <p>
          已有账号？
          <router-link to="/login">立即登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, Message, User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@shared/stores'
import { register, sendCode as sendCodeApi } from '@shared/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
const agreed = ref(false)

const form = reactive({
  phone: '',
  code: '',
  nickname: '',
  password: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const sendCode = async () => {
  if (!form.phone || !/^1\d{10}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    const res = await sendCodeApi(form.phone)
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
    }
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  }
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
    
    loading.value = true
    const res = await register({
      ...form,
      role: 2
    })
    
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)
      ElMessage.success('注册成功')
      router.push('/')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '注册失败')
    }
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
  background: linear-gradient(135deg, #fff5e6 0%, #ffeccc 100%);
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 8px 32px rgba(255, 149, 0, 0.1);
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
    font-size: 22px;
    font-weight: 700;
    color: #303133;
    margin: 0 0 8px;
  }
  
  p {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.register-form {
  .el-checkbox {
    a {
      color: #ff9500;
      text-decoration: none;
    }
  }
}

.register-footer {
  text-align: center;
  
  p {
    font-size: 14px;
    color: #606266;
    
    a {
      color: #ff9500;
      text-decoration: none;
    }
  }
}
</style>
