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

// 快速填入测试账号（与 data.sql 中的模拟数据匹配）
const fillDemoAccount = (type) => {
  if (type === 'tutor') {
    // 教员账号：data.sql 中 id=101 的教员 张学霸
    form.account = '13800138101'
    form.password = 'test123456'
  } else if (type === 'parent') {
    // 家长账号：data.sql 中 id=201 的家长 子涵妈妈
    form.account = '13900139201'
    form.password = 'test123456'
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
    <!-- 左侧品牌展示区（桌面端可见） -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon">
            <el-icon :size="36"><School /></el-icon>
          </div>
          <span class="logo-text">易家教</span>
        </div>
        <h1 class="brand-title">连接优秀教师<br/>与每一位学生</h1>
        <p class="brand-desc">
          专业的大学生家教智能服务平台，通过AI智能匹配，帮助您找到最适合的老师。
        </p>
        <div class="feature-list">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="20"><Select /></el-icon>
            </div>
            <span>智能匹配 · 精准推荐最合适的老师</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="20"><Medal /></el-icon>
            </div>
            <span>实名认证 · 教师资质严格审核</span>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="20"><CreditCard /></el-icon>
            </div>
            <span>资金托管 · 课后满意再付款</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-section">
      <div class="auth-card">
        <!-- Logo 区域（移动端可见） -->
        <div class="auth-logo">
          <div class="logo-icon">
            <el-icon :size="48"><School /></el-icon>
          </div>
          <h1 class="logo-title">易家教</h1>
          <p class="logo-subtitle">连接好老师与好学生</p>
        </div>

        <!-- 表单标题（桌面端可见） -->
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户继续使用</p>
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
              教师: 13800138101
            </el-tag>
            <el-tag 
              type="warning" 
              effect="plain" 
              class="demo-tag"
              @click="fillDemoAccount('parent')"
            >
              家长: 13900139201
            </el-tag>
          </div>
          <p class="demo-password">密码均为: test123456</p>
        </div>

        <!-- 底部链接 -->
        <div class="auth-footer">
          还没有账号？
          <el-link type="primary" @click="router.push('/register')">去注册</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 左侧品牌展示区 */
.brand-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 80px;
  color: #fff;

  @media (max-width: 992px) {
    display: none;
  }

  .brand-content {
    max-width: 500px;
  }

  .brand-logo {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 40px;

    .logo-icon {
      width: 64px;
      height: 64px;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(10px);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .logo-text {
      font-size: 32px;
      font-weight: 700;
    }
  }

  .brand-title {
    font-size: 42px;
    font-weight: 700;
    line-height: 1.3;
    margin-bottom: 24px;
  }

  .brand-desc {
    font-size: 18px;
    opacity: 0.9;
    line-height: 1.6;
    margin-bottom: 48px;
  }

  .feature-list {
    display: flex;
    flex-direction: column;
    gap: 20px;

    .feature-item {
      display: flex;
      align-items: center;
      gap: 16px;
      font-size: 16px;

      .feature-icon {
        width: 44px;
        height: 44px;
        background: rgba(255, 255, 255, 0.15);
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

/* 右侧表单区 */
.form-section {
  width: 520px;
  min-width: 400px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;

  @media (max-width: 992px) {
    width: 100%;
    min-width: auto;
    padding: 40px 24px;
  }
}

.auth-card {
  width: 100%;
  max-width: 400px;
}

.auth-logo {
  text-align: center;
  margin-bottom: 40px;

  @media (min-width: 993px) {
    display: none; /* 桌面端隐藏，显示左侧品牌区 */
  }

  .logo-icon {
    width: 72px;
    height: 72px;
    background: linear-gradient(135deg, $primary-color 0%, #667eea 100%);
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    color: #fff;
  }

  .logo-title {
    font-size: 26px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 4px;
  }

  .logo-subtitle {
    font-size: 14px;
    color: $text-secondary;
  }
}

.form-header {
  margin-bottom: 32px;

  @media (max-width: 992px) {
    display: none;
  }

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 8px;
  }

  p {
    font-size: 15px;
    color: $text-secondary;
  }
}

.auth-form {
  .submit-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 8px;
  }
}

.demo-accounts {
  background: #f8fafc;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 24px;
  text-align: center;

  .demo-title {
    font-size: 12px;
    color: $text-muted;
    margin-bottom: 10px;
  }

  .demo-tags {
    display: flex;
    justify-content: center;
    gap: 10px;
    margin-bottom: 8px;
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
    margin-top: 8px;
  }
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: $text-secondary;
}
</style>