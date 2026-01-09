<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>家教平台管理后台</h1>
        <p>Campus Tutor Admin</p>
      </div>
      
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="form.remember">记住密码</el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>演示账号: admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  remember: true
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

onMounted(() => {
  // 读取记住的账号
  const savedUsername = localStorage.getItem('admin_username')
  if (savedUsername) {
    form.username = savedUsername
  }
})

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  
  try {
    // 尝试真实 API 登录
    const res = await authApi.login({
      account: form.username,
      password: form.password
    })
    
    localStorage.setItem('admin_token', res.data.token)
    
    if (form.remember) {
      localStorage.setItem('admin_username', form.username)
    } else {
      localStorage.removeItem('admin_username')
    }
    
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error('登录失败:', error)
    
    // 演示模式回退：如果后端未启动，使用模拟登录
    if (form.username === 'admin' && form.password === 'admin123') {
      const mockToken = 'admin_mock_token_' + Date.now()
      localStorage.setItem('admin_token', mockToken)
      
      if (form.remember) {
        localStorage.setItem('admin_username', form.username)
      }
      
      ElMessage.success('登录成功（演示模式）')
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h1 {
    font-size: 24px;
    color: #303133;
    margin-bottom: 8px;
  }
  
  p {
    font-size: 14px;
    color: #909399;
  }
}

.login-form {
  .el-input {
    --el-input-height: 44px;
  }
  
  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
  }
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  
  p {
    font-size: 12px;
    color: #909399;
  }
}
</style>
