<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendCode, register } from '@/api/auth'

const router = useRouter()

// 表单数据
const formRef = ref(null)
const loading = ref(false)
const sendingCode = ref(false)
const countdown = ref(0)

const form = reactive({
  phone: '',
  password: '',
  confirmPassword: '',
  code: '',
  nickname: '',
  role: 2, // 2-家长, 1-教师
  agreed: true
})

// 表单验证规则
const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

// 发送验证码
const handleSendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  sendingCode.value = true
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
  } catch (e) {
    ElMessage.error(e.message || '验证码发送失败')
  } finally {
    sendingCode.value = false
  }
}

// 注册逻辑
const handleRegister = async () => {
  if (!form.agreed) {
    ElMessage.warning('请先同意用户协议')
    return
  }

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      phone: form.phone,
      password: form.password,
      code: form.code,
      role: form.role,
      nickname: form.nickname || undefined
    })
    
    // 注册成功后跳转到登录页面，不自动登录
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error(e.message || '注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <!-- 左侧品牌展示区 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon">
            <el-icon :size="36"><School /></el-icon>
          </div>
          <span class="logo-text">易家教</span>
        </div>
        <h1 class="brand-title">加入我们<br/>开启智能匹配</h1>
        <p class="brand-desc">
          无论您是家长还是教师，都可以在这里找到最适合的匹配对象。AI智能推荐让家教更简单。
        </p>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-section">
      <div class="auth-card">
        <!-- Logo 区域（移动端可见） -->
        <div class="auth-logo">
          <div class="logo-icon">
            <el-icon :size="48"><UserFilled /></el-icon>
          </div>
          <h1 class="logo-title">注册账号</h1>
          <p class="logo-subtitle">成为家长或教师，开启智能匹配</p>
        </div>

        <!-- 表单标题（桌面端可见） -->
        <div class="form-header">
          <h2>创建新账户</h2>
          <p>注册成为家长或教师，开始使用平台服务</p>
        </div>

        <!-- 注册表单 -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          class="auth-form"
        >
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入手机号"
              prefix-icon="Phone"
              maxlength="11"
            />
          </el-form-item>

          <el-form-item label="验证码" prop="code">
            <div class="code-input">
              <el-input
                v-model="form.code"
                placeholder="请输入验证码"
                prefix-icon="Message"
              />
              <el-button
                type="primary"
                :loading="sendingCode"
                :disabled="countdown > 0"
                @click="handleSendCode"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="昵称（可选）">
            <el-input
              v-model="form.nickname"
              placeholder="方便家长或老师识别"
              prefix-icon="Edit"
            />
          </el-form-item>

          <el-form-item label="注册身份">
            <el-radio-group v-model="form.role" class="role-group">
              <el-radio-button :value="2">
                <el-icon><User /></el-icon>
                <span>我是家长</span>
              </el-radio-button>
              <el-radio-button :value="1">
                <el-icon><Reading /></el-icon>
                <span>我是教师</span>
              </el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-checkbox v-model="form.agreed">
              我已阅读并同意
              <el-link type="primary" underline="never">《用户协议》</el-link>
              和
              <el-link type="primary" underline="never">《隐私政策》</el-link>
            </el-checkbox>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="submit-btn"
              @click="handleRegister"
            >
              立即注册
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 底部链接 -->
        <div class="auth-footer">
          已有账号？
          <el-link type="primary" @click="router.push('/login')">去登录</el-link>
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
}

/* 右侧表单区 */
.form-section {
  width: 520px;
  min-width: 420px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 60px;
  overflow-y: auto;

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
  margin-bottom: 32px;

  @media (min-width: 993px) {
    display: none;
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
    font-size: 24px;
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
  margin-bottom: 28px;

  @media (max-width: 992px) {
    display: none;
  }

  h2 {
    font-size: 26px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: $text-secondary;
  }
}

.auth-form {
  .code-input {
    display: flex;
    gap: 10px;
    width: 100%;

    .el-input {
      flex: 1;
    }
  }

  .role-group {
    width: 100%;
    
    :deep(.el-radio-button) {
      flex: 1;
      
      .el-radio-button__inner {
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
      }
    }
  }

  .submit-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 8px;
  }
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: $text-secondary;
  margin-top: 20px;
}
</style>
