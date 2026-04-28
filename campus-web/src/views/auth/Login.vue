<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        <h1>校园智教</h1>
        <p>大学生家教智能服务平台</p>
      </div>

      <el-tabs v-model="loginType" class="login-tabs">
        <el-tab-pane label="密码登录" name="password">
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" class="login-form">
            <el-form-item prop="phone">
              <el-input
                v-model="pwdForm.phone"
                placeholder="请输入手机号"
                size="large"
                :prefix-icon="Phone"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="pwdForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handlePasswordLogin"
              />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-btn"
                @click="handlePasswordLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="验证码登录" name="code">
          <el-form ref="codeFormRef" :model="codeForm" :rules="codeRules" class="login-form">
            <el-form-item prop="phone">
              <el-input
                v-model="codeForm.phone"
                placeholder="请输入手机号"
                size="large"
                :prefix-icon="Phone"
              />
            </el-form-item>

            <el-form-item prop="code">
              <div class="code-input">
                <el-input
                  v-model="codeForm.code"
                  placeholder="请输入验证码"
                  size="large"
                  @keyup.enter="handleCodeLogin"
                />
                <el-button
                  size="large"
                  :disabled="smsCooldown > 0 || sendingSmsCode"
                  @click="handleSendCode"
                >
                  {{ smsCooldown > 0 ? `${smsCooldown}s后重试` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-btn"
                @click="handleCodeLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="login-footer">
        <p>
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </p>
        <p class="forgot-password">
          <a href="#" @click.prevent="showResetPassword">忘记密码？</a>
        </p>
        <p class="agreement">
          登录即表示同意
          <router-link to="/settings/privacy">《用户协议》</router-link>和
          <router-link to="/settings/agreement">《隐私政策》</router-link>
        </p>
      </div>
    </div>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetVisible" title="重置密码" width="400px" :close-on-click-modal="false">
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules">
        <el-form-item prop="phone">
          <el-input v-model="resetForm.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-input">
            <el-input v-model="resetForm.code" placeholder="请输入验证码" />
            <el-button :disabled="resetCooldown > 0 || sendingResetCode" @click="sendResetCode">
              {{ resetCooldown > 0 ? `${resetCooldown}s后重试` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="resetForm.confirmPassword" type="password" placeholder="请确认新密码" show-password :prefix-icon="Lock" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="handleResetPassword">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Phone, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@shared/stores'
import { login, sendCode, resetPassword } from '@shared/api/auth'

const router = useRouter()
const userStore = useUserStore()

const loginType = ref('password')
const loading = ref(false)

const pwdFormRef = ref(null)
const codeFormRef = ref(null)

const pwdForm = reactive({
  phone: '',
  password: ''
})

const codeForm = reactive({
  phone: '',
  code: ''
})

const pwdRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const codeRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

const smsCooldown = ref(0)
const sendingSmsCode = ref(false)
let smsTimer = null
let resetTimer = null

onUnmounted(() => {
  if (smsTimer) { clearInterval(smsTimer); smsTimer = null }
  if (resetTimer) { clearInterval(resetTimer); resetTimer = null }
})

const handleSendCode = async () => {
  if (!codeForm.phone || !/^1[3-9]\d{9}$/.test(codeForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  sendingSmsCode.value = true
  try {
    const res = await sendCode(codeForm.phone, 'login')
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      smsCooldown.value = 60
      smsTimer = setInterval(() => {
        smsCooldown.value--
        if (smsCooldown.value <= 0) {
          clearInterval(smsTimer)
          smsTimer = null
        }
      }, 1000)
    }
  } catch (error) {
    if (!error._handled) {
      ElMessage.error(error.message || '发送验证码失败')
    }
  } finally {
    sendingSmsCode.value = false
  }
}

const handleLoginSuccess = (userInfo) => {
  userStore.setToken(userInfo.token)
  userStore.setUserInfo(userInfo)

  if (userInfo.role === 1) {
    userStore.setRole('tutor')
    ElMessage.success('登录成功，欢迎教师')
    router.push('/teacher/home')
  } else {
    userStore.setRole('parent')
    ElMessage.success('登录成功，欢迎家长')
    router.push('/parent/home')
  }
}

const handlePasswordLogin = async () => {
  try {
    await pwdFormRef.value.validate()
    loading.value = true
    const res = await login({
      account: pwdForm.phone,
      password: pwdForm.password,
      loginType: 'password'
    })
    if (res.code === 200) {
      handleLoginSuccess(res.data)
    }
  } catch (error) {
    if (error !== 'cancel' && !error._handled) {
      ElMessage.error(error.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

const handleCodeLogin = async () => {
  try {
    await codeFormRef.value.validate()
    loading.value = true
    const res = await login({
      account: codeForm.phone,
      code: codeForm.code,
      loginType: 'code'
    })
    if (res.code === 200) {
      handleLoginSuccess(res.data)
    }
  } catch (error) {
    if (error !== 'cancel' && !error._handled) {
      ElMessage.error(error.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

const resetVisible = ref(false)
const resetLoading = ref(false)
const resetFormRef = ref(null)
const resetCooldown = ref(0)
const sendingResetCode = ref(false)

const resetForm = reactive({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== resetForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const resetRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const showResetPassword = () => {
  resetForm.phone = ''
  resetForm.code = ''
  resetForm.newPassword = ''
  resetForm.confirmPassword = ''
  resetVisible.value = true
}

const sendResetCode = async () => {
  if (!resetForm.phone || !/^1[3-9]\d{9}$/.test(resetForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  sendingResetCode.value = true
  try {
    const res = await sendCode(resetForm.phone, 'reset_password')
    if (res.code === 200) {
      ElMessage.success('验证码已发送')
      resetCooldown.value = 60
      resetTimer = setInterval(() => {
        resetCooldown.value--
        if (resetCooldown.value <= 0) {
          clearInterval(resetTimer)
          resetTimer = null
        }
      }, 1000)
    }
  } catch (error) {
    if (!error._handled) {
      ElMessage.error(error.message || '发送验证码失败')
    }
  } finally {
    sendingResetCode.value = false
  }
}

const handleResetPassword = async () => {
  try {
    await resetFormRef.value.validate()
    resetLoading.value = true
    const res = await resetPassword({
      phone: resetForm.phone,
      code: resetForm.code,
      newPassword: resetForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码重置成功，请使用新密码登录')
      resetVisible.value = false
    }
  } catch (error) {
    if (error !== 'cancel' && !error._handled) {
      ElMessage.error(error.message || '重置密码失败')
    }
  } finally {
    resetLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  padding: 20px;
}

.login-container {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 20px;
  padding: 48px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 24px;

  .logo {
    width: 72px;
    height: 72px;
    margin-bottom: 16px;
  }

  h1 {
    font-size: 26px;
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

.login-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
  }

  :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
  }

  :deep(.el-tabs__active-bar) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  :deep(.el-tabs__item.is-active) {
    color: #667eea;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }

  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    border-radius: 10px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;

    &:hover {
      background: linear-gradient(135deg, #7b93f5 0%, #8b5cb6 100%);
    }
  }
}

.code-input {
  display: flex;
  gap: 8px;
  width: 100%;

  .el-input {
    flex: 1;
  }
}

.login-footer {
  text-align: center;

  p {
    font-size: 14px;
    color: #606266;

    a {
      color: #667eea;
      text-decoration: none;
      font-weight: 500;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .forgot-password {
    margin-top: 8px;

    a {
      font-size: 13px;
    }
  }

  .agreement {
    font-size: 12px;
    color: #909399;

    a {
      color: #667eea;
    }
  }
}
</style>
