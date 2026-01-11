<template>
  <div class="settings-page">
    <el-page-header @back="goBack">
      <template #content>设置</template>
    </el-page-header>
    
    <div class="settings-container">
      <!-- 账号设置 -->
      <div class="settings-group">
        <h4>账号设置</h4>
        <div class="settings-item" @click="changePhone">
          <span class="label">手机号</span>
          <span class="value">{{ maskPhone(userStore.phone) }}</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="settings-item" @click="changePassword">
          <span class="label">修改密码</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <!-- 通知设置 -->
      <div class="settings-group">
        <h4>通知设置</h4>
        <div class="settings-item">
          <span class="label">订单通知</span>
          <el-switch v-model="settings.orderNotify" @change="saveSettings" />
        </div>
        <div class="settings-item">
          <span class="label">消息通知</span>
          <el-switch v-model="settings.messageNotify" @change="saveSettings" />
        </div>
        <div class="settings-item">
          <span class="label">系统通知</span>
          <el-switch v-model="settings.systemNotify" @change="saveSettings" />
        </div>
      </div>
      
      <!-- 隐私设置 -->
      <div class="settings-group">
        <h4>隐私设置</h4>
        <div class="settings-item">
          <span class="label">显示在线状态</span>
          <el-switch v-model="settings.showOnline" @change="saveSettings" />
        </div>
        <div class="settings-item">
          <span class="label">允许陌生人联系</span>
          <el-switch v-model="settings.allowStranger" @change="saveSettings" />
        </div>
      </div>
      
      <!-- 其他设置 -->
      <div class="settings-group">
        <h4>其他</h4>
        <div class="settings-item" @click="clearCache">
          <span class="label">清除缓存</span>
          <span class="value">{{ cacheSize }}</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="settings-item" @click="checkUpdate">
          <span class="label">检查更新</span>
          <span class="value">v1.0.0</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="settings-item" @click="showPrivacy">
          <span class="label">隐私政策</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="settings-item" @click="showTerms">
          <span class="label">用户协议</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <!-- 账号注销 -->
      <div class="danger-zone">
        <el-button type="danger" link @click="deleteAccount">
          注销账号
        </el-button>
      </div>
    </div>
    
    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordVisible" title="修改密码" width="90%" max-width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPassword">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@shared/stores'
import { updatePassword } from '@shared/api/user'

const router = useRouter()
const userStore = useUserStore()

const passwordVisible = ref(false)
const passwordFormRef = ref(null)
const saving = ref(false)
const cacheSize = ref('0 KB')

const settings = reactive({
  orderNotify: true,
  messageNotify: true,
  systemNotify: true,
  showOnline: true,
  allowStranger: false
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const maskPhone = (phone) => {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const loadSettings = () => {
  const saved = localStorage.getItem('user_settings')
  if (saved) {
    try {
      Object.assign(settings, JSON.parse(saved))
    } catch {
      // ignore
    }
  }
}

const saveSettings = () => {
  localStorage.setItem('user_settings', JSON.stringify(settings))
}

const changePhone = () => {
  ElMessage.info('修改手机号功能开发中')
}

const changePassword = () => {
  passwordVisible.value = true
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const submitPassword = async () => {
  try {
    await passwordFormRef.value.validate()
    
    saving.value = true
    const res = await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      passwordVisible.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '修改失败')
    }
  } finally {
    saving.value = false
  }
}

const clearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清除缓存吗？', '清除缓存')
    localStorage.removeItem('ai_chat_history')
    cacheSize.value = '0 KB'
    ElMessage.success('缓存已清除')
  } catch {
    // ignore
  }
}

const checkUpdate = () => {
  ElMessage.success('已是最新版本')
}

const showPrivacy = () => {
  ElMessage.info('隐私政策页面开发中')
}

const showTerms = () => {
  ElMessage.info('用户协议页面开发中')
}

const deleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
      '注销账号后，您的所有数据将被删除且无法恢复，确定要注销吗？',
      '注销账号',
      { type: 'warning' }
    )
    
    ElMessage.info('账号注销功能开发中')
  } catch {
    // ignore
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadSettings()
  
  // 计算缓存大小
  let size = 0
  for (const key in localStorage) {
    if (localStorage.hasOwnProperty(key)) {
      size += localStorage.getItem(key)?.length || 0
    }
  }
  cacheSize.value = size > 1024 ? `${(size / 1024).toFixed(1)} KB` : `${size} B`
})
</script>

<style lang="scss" scoped>
.settings-page {
  max-width: 600px;
  margin: 0 auto;
  
  .settings-container {
    margin-top: 24px;
  }
  
  .settings-group {
    background: #fff;
    border-radius: 12px;
    margin-bottom: 16px;
    overflow: hidden;
    
    h4 {
      font-size: 13px;
      font-weight: 500;
      color: #909399;
      padding: 12px 20px 8px;
    }
    
    .settings-item {
      display: flex;
      align-items: center;
      padding: 16px 20px;
      cursor: pointer;
      transition: background-color 0.2s;
      
      &:not(:last-child) {
        border-bottom: 1px solid #ebeef5;
      }
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .label {
        flex: 1;
        font-size: 15px;
      }
      
      .value {
        font-size: 14px;
        color: #909399;
        margin-right: 8px;
      }
      
      .el-icon {
        color: #c0c4cc;
      }
    }
  }
  
  .danger-zone {
    text-align: center;
    padding: 24px;
    
    .el-button {
      font-size: 14px;
    }
  }
}
</style>
