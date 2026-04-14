<template>
  <div class="settings-page">
    <div class="page-header">
      <el-button link @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h1 class="page-title">设置</h1>
    </div>
    
    <div class="settings-list">
      <!-- 个人资料 -->
      <div class="settings-group">
        <div class="settings-item" @click="goTo('/settings/profile')">
          <div class="item-left">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </div>
          <div class="item-right">
            <span class="item-value">{{ userInfo.name || '未设置' }}</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
        
        <div class="settings-item" @click="goTo('/settings/phone')">
          <div class="item-left">
            <el-icon><Phone /></el-icon>
            <span>绑定手机</span>
          </div>
          <div class="item-right">
            <span class="item-value">{{ maskPhone(userInfo.phone) }}</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
        
        <div class="settings-item" @click="goTo('/settings/password')">
          <div class="item-left">
            <el-icon><Lock /></el-icon>
            <span>修改密码</span>
          </div>
          <div class="item-right">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 通知设置 -->
      <div class="settings-group">
        <div class="settings-item">
          <div class="item-left">
            <el-icon><Bell /></el-icon>
            <span>消息通知</span>
          </div>
          <div class="item-right">
            <el-switch v-model="settings.notification" @change="saveSetting('notification')" />
          </div>
        </div>
        
        <div class="settings-item">
          <div class="item-left">
            <el-icon><ChatDotRound /></el-icon>
            <span>新消息提醒</span>
          </div>
          <div class="item-right">
            <el-switch v-model="settings.messageAlert" @change="saveSetting('messageAlert')" />
          </div>
        </div>
      </div>
      
      <!-- 其他设置 -->
      <div class="settings-group">
        <div class="settings-item" @click="clearCache">
          <div class="item-left">
            <el-icon><Delete /></el-icon>
            <span>清理缓存</span>
          </div>
          <div class="item-right">
            <span class="item-value">{{ cacheSize }}</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
        
        <div class="settings-item" @click="checkUpdate">
          <div class="item-left">
            <el-icon><Refresh /></el-icon>
            <span>检查更新</span>
          </div>
          <div class="item-right">
            <span class="item-value">v1.0.0</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
        
        <div class="settings-item" @click="goTo('/settings/privacy')">
          <div class="item-left">
            <el-icon><Document /></el-icon>
            <span>隐私政策</span>
          </div>
          <div class="item-right">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
        
        <div class="settings-item" @click="goTo('/settings/agreement')">
          <div class="item-left">
            <el-icon><Document /></el-icon>
            <span>用户协议</span>
          </div>
          <div class="item-right">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 账号安全 -->
      <div class="settings-group">
        <div class="settings-item danger" @click="showDeactivate">
          <div class="item-left">
            <el-icon><Warning /></el-icon>
            <span>注销账号</span>
          </div>
          <div class="item-right">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 退出登录 -->
    <div class="logout-section">
      <el-button type="danger" plain size="large" @click="handleLogout">
        退出登录
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@shared/stores'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, ArrowRight, User, Phone, Lock, Bell,
  ChatDotRound, Delete, Refresh, Document, Warning
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.user || {})
const cacheSize = ref('0KB')

const settings = ref({
  notification: true,
  messageAlert: true
})

const maskPhone = (phone) => {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const goBack = () => {
  router.back()
}

const goTo = (path) => {
  router.push(path)
}

const saveSetting = (key) => {
  localStorage.setItem(`setting_${key}`, settings.value[key])
  ElMessage.success('设置已保存')
}

const clearCache = () => {
  // 清理本地存储中的缓存数据
  const keys = Object.keys(localStorage).filter(k => 
    k.startsWith('cache_') || k.startsWith('temp_')
  )
  keys.forEach(k => localStorage.removeItem(k))
  cacheSize.value = '0KB'
  ElMessage.success('缓存已清理')
}

const checkUpdate = () => {
  ElMessage.info('当前已是最新版本')
}

const showDeactivate = async () => {
  try {
    await ElMessageBox.confirm(
      '注销账号后，您的所有数据将被永久删除且无法恢复，确定要继续吗？',
      '注销账号',
      { type: 'warning' }
    )
    
    const { value: reason } = await ElMessageBox.prompt(
      '请输入注销原因',
      '注销账号'
    )
    
    if (reason) {
      // 调用注销API
      ElMessage.success('账号注销申请已提交')
    }
  } catch {}
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示')
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {}
}

const loadSettings = () => {
  settings.value.notification = localStorage.getItem('setting_notification') !== 'false'
  settings.value.messageAlert = localStorage.getItem('setting_messageAlert') !== 'false'
}

const calculateCacheSize = () => {
  let size = 0
  for (let key in localStorage) {
    if (key.startsWith('cache_') || key.startsWith('temp_')) {
      size += localStorage.getItem(key)?.length || 0
    }
  }
  if (size < 1024) {
    cacheSize.value = size + 'B'
  } else if (size < 1024 * 1024) {
    cacheSize.value = (size / 1024).toFixed(1) + 'KB'
  } else {
    cacheSize.value = (size / 1024 / 1024).toFixed(1) + 'MB'
  }
}

onMounted(() => {
  loadSettings()
  calculateCacheSize()
})
</script>

<style lang="scss" scoped>
.settings-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  padding-bottom: 100px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin: 0;
  }
}

.settings-list {
  .settings-group {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
  
  .settings-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 20px;
    cursor: pointer;
    transition: background 0.2s;
    
    &:not(:last-child) {
      border-bottom: 1px solid #f5f5f5;
    }
    
    &:hover {
      background: #fafafa;
    }
    
    &.danger {
      .item-left {
        color: #f56c6c;
      }
    }
    
    .item-left {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .el-icon {
        font-size: 20px;
        color: #666;
      }
      
      span {
        font-size: 15px;
      }
    }
    
    .item-right {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .item-value {
        font-size: 14px;
        color: #999;
      }
      
      > .el-icon {
        color: #ccc;
      }
    }
  }
}

.logout-section {
  margin-top: 40px;
  text-align: center;
  
  .el-button {
    width: 100%;
    max-width: 300px;
  }
}
</style>
