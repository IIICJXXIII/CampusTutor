<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import { login } from '@/api/auth'
import { getTutorProfile } from '@/api/tutor'

const router = useRouter()
const userStore = useUserStore()

// 表单数据
const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  account: '',
  password: '',
  agreed: true
})

// 表单验证规则
const rules = {
  account: [
    { required: true, message: '请输入手机号或账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

// 快速填入测试账号
const fillDemoAccount = (type) => {
  if (type === 'tutor') {
    form.account = '13800000002'
    form.password = '123456'
  } else if (type === 'parent') {
    form.account = '13800000001'
    form.password = '123456'
  }
}

// 登录逻辑
const handleLogin = async () => {
  if (!form.agreed) {
    ElMessage.warning('请先同意用户协议')
    return
  }
  
  // 表单验证
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await login({
      account: form.account,
      password: form.password,
      loginType: 'password'
    })
    
    // 保存登录信息到 Pinia store
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data)
    
    ElMessage.success('登录成功')
    
    // 根据角色跳转
    const role = res.data.role
    if (role === 2) {
      // 家长端
      router.push('/parent/demand')
    } else if (role === 1) {
      // 教师端：检查认证状态
      try {
        const profileRes = await getTutorProfile()
        const certStatus = profileRes.data?.certStatus
        if (certStatus === 2) {
          router.push('/teacher/students')
        } else {
          router.push('/teacher/auth')
        }
      } catch {
        router.push('/teacher/auth')
      }
    } else {
      router.push('/mine')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查账号或密码')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- Logo 区域 -->
      <div class="auth-logo">
        <div class="logo-icon">
          <el-icon :size="48"><School /></el-icon>
        </div>
        <h1 class="logo-title">易家教</h1>
        <p class="logo-subtitle">连接好老师与好学生</p>
      </div>

      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        class="auth-form"
      >
        <el-form-item label="手机号 / 账号" prop="account">
          <el-input
            v-model="form.account"
            placeholder="请输入手机号或账号"
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="form.agreed">
            我已阅读并同意
            <el-link type="primary" underline="never">《用户协议》</el-link>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="submit-btn"
            @click="handleLogin"
          >
            立即登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 快速测试账号 -->
      <div class="demo-accounts">
        <p class="demo-title">测试账号（点击快速填入）：</p>
        <div class="demo-tags">
          <el-tag 
            type="success" 
            effect="plain" 
            class="demo-tag"
            @click="fillDemoAccount('tutor')"
          >
            教师: 13800000002
          </el-tag>
          <el-tag 
            type="warning" 
            effect="plain" 
            class="demo-tag"
            @click="fillDemoAccount('parent')"
          >
            家长: 13800000001
          </el-tag>
        </div>
        <p class="demo-password">密码均为: 123456</p>
      </div>

      <!-- 底部链接 -->
      <div class="auth-footer">
        还没有账号？
        <el-link type="primary" @click="router.push('/register')">去注册</el-link>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, $primary-color 0%, #667eea 100%);
  padding: $spacing-lg;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: $spacing-xl;
  box-shadow: $shadow-lg;
}

.auth-logo {
  text-align: center;
  margin-bottom: $spacing-xl;

  .logo-icon {
    width: 80px;
    height: 80px;
    background: linear-gradient(135deg, $primary-color 0%, #667eea 100%);
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto $spacing-md;
    color: #fff;
  }

  .logo-title {
    font-size: 28px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 4px;
  }

  .logo-subtitle {
    font-size: 14px;
    color: $text-secondary;
  }
}

.auth-form {
  .submit-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
  }
}

.demo-accounts {
  background: $bg-light;
  border-radius: 12px;
  padding: $spacing-md;
  margin-bottom: $spacing-lg;
  text-align: center;

  .demo-title {
    font-size: 12px;
    color: $text-muted;
    margin-bottom: $spacing-sm;
  }

  .demo-tags {
    display: flex;
    justify-content: center;
    gap: $spacing-sm;
    margin-bottom: $spacing-xs;
  }

  .demo-tag {
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: scale(1.05);
    }
  }

  .demo-password {
    font-size: 12px;
    color: $text-muted;
    margin-top: $spacing-xs;
  }
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: $text-secondary;
}
</style>