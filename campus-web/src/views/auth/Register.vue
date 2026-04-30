<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <img src="@/assets/logo.svg" alt="校园智教 Logo" class="logo" />
        <h1>注册账号</h1>
        <p>加入校园智教平台</p>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" class="register-form">
        <el-form-item prop="role">
          <div class="role-selector">
            <div
              class="role-card"
              :class="{ active: form.role === 2 }"
              @click="form.role = 2"
            >
              <el-icon :size="28"><User /></el-icon>
              <span>我是家长</span>
              <p>为孩子找到好老师</p>
            </div>
            <div
              class="role-card"
              :class="{ active: form.role === 1 }"
              @click="form.role = 1"
            >
              <el-icon :size="28"><Reading /></el-icon>
              <span>我是教师</span>
              <p>展示才华找到学生</p>
            </div>
          </div>
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            size="large"
            :prefix-icon="Phone"
            maxlength="11"
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
            size="large"
            :prefix-icon="UserIcon"
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
          <el-checkbox v-model="agreed">
            我已阅读并同意
          </el-checkbox>
          <router-link to="/settings/agreement" class="agreement-link">《用户协议》</router-link>和
          <router-link to="/settings/privacy" class="agreement-link">《隐私政策》</router-link>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="!agreed"
            class="register-btn"
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
import { Phone, User as UserIcon, Lock, Reading } from '@element-plus/icons-vue'
import { useUserStore } from '@shared/stores'
import { register } from '@shared/api/auth'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const agreed = ref(false)

const form = reactive({
  phone: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  role: 2
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
=======
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
>>>>>>> 3745728a8acb42a0a4d836ff5db19c2faa1cff85
  ]
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()

    loading.value = true
    const res = await register({
      phone: form.phone,
      nickname: form.nickname,
      password: form.password,
      role: form.role
    })

    if (res.code === 200) {
      userStore.setToken(res.data.token)
      userStore.setUserInfo(res.data)

      if (form.role === 1) {
        userStore.setRole('tutor')
        ElMessage.success('注册成功')
        router.push('/teacher/home')
      } else {
        userStore.setRole('parent')
        ElMessage.success('注册成功')
        router.push('/parent/home')
      }
    }
  } catch (error) {
    if (error !== 'cancel' && !error._handled) {
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  padding: 20px;
}

.register-container {
  width: 100%;
  max-width: 460px;
  background: #fff;
  border-radius: 20px;
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.register-header {
  text-align: center;
  margin-bottom: 28px;

  .logo {
    width: 64px;
    height: 64px;
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

.role-selector {
  display: flex;
  gap: 16px;
  width: 100%;

  .role-card {
    flex: 1;
    padding: 16px;
    border: 2px solid #ebeef5;
    border-radius: 12px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;

    .el-icon {
      color: #909399;
      margin-bottom: 8px;
    }

    span {
      display: block;
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }

    p {
      font-size: 12px;
      color: #909399;
      margin: 0;
    }

    &.active {
      border-color: #667eea;
      background: #f0f0ff;

      .el-icon {
        color: #667eea;
      }
    }

    &:hover {
      border-color: #667eea;
    }
  }
}

.register-form {
  .register-btn {
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

  .agreement-link {
    color: #667eea;
    text-decoration: none;
  }
}

.register-footer {
  text-align: center;

  p {
    font-size: 14px;
    color: #606266;

    a {
      color: #667eea;
      text-decoration: none;
      font-weight: 500;
    }
  }
}
</style>
