<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        <h1>校园家教 · 家长端</h1>
        <p>为孩子找到最合适的老师</p>
      </div>
      
      <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
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
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </p>
        
        <div class="divider">
          <span>其他登录方式</span>
        </div>
        
        <div class="social-login">
          <el-button circle size="large" @click="wechatLogin">
            <el-icon :size="24" style="color: #07c160"><ChatDotRound /></el-icon>
          </el-button>
        </div>
        
        <p class="agreement">
          登录即表示同意
          <a href="#">《用户协议》</a>和
          <a href="#">《隐私政策》</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, Lock, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@shared/stores'
import { login } from '@shared/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  phone: '',
  password: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    
    loading.value = true
    const res = await login({
      account: form.phone,
      password: form.password,
      loginType: 'password',
      role: 'parent'
    })
    
    if (res.code === 200) {
      if (res.data.role !== 2 && res.data.role !== 0) { // 2 is parent, 0 is admin (optional check)
         ElMessage.warning('非家长账号，请前往教师端登录')
         loading.value = false
         return
      }

      userStore.setToken(res.data.token)
      // LoginResponse 为扁平结构，直接作为 userInfo 使用
      userStore.setUserInfo(res.data)
      ElMessage.success('登录成功')
      router.push('/')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

const wechatLogin = () => {
  ElMessage.info('微信登录功能开发中')
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff5e6 0%, #ffeccc 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 32px;
  box-shadow: 0 8px 32px rgba(255, 149, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
  
  .logo {
    width: 72px;
    height: 72px;
    margin-bottom: 16px;
  }
  
  h1 {
    font-size: 24px;
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

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }
}

.login-footer {
  text-align: center;
  
  p {
    font-size: 14px;
    color: #606266;
    
    a {
      color: #ff9500;
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
  }
  
  .divider {
    display: flex;
    align-items: center;
    margin: 24px 0;
    
    &::before, &::after {
      content: '';
      flex: 1;
      height: 1px;
      background: #ebeef5;
    }
    
    span {
      padding: 0 16px;
      font-size: 13px;
      color: #909399;
    }
  }
  
  .social-login {
    margin-bottom: 24px;
  }
  
  .agreement {
    font-size: 12px;
    color: #909399;
    
    a {
      color: #ff9500;
    }
  }
}
</style>
