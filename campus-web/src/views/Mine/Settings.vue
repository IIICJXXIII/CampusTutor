<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores'
import { ElMessageBox, ElMessage } from 'element-plus'
import { updateUserInfo } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

// 修改昵称
const handleEditNickname = () => {
  ElMessageBox.prompt('请输入新的昵称', '修改昵称', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: userStore.nickname,
    inputPattern: /^[\u4e00-\u9fa5a-zA-Z0-9]{2,10}$/,
    inputErrorMessage: '昵称长度2-10位，只能包含中文、字母或数字'
  }).then(async ({ value }) => {
    try {
      if (value === userStore.nickname) return
      
      const updateData = {
        id: userStore.userId,
        nickname: value
      }
      
      await updateUserInfo(updateData)
      
      // 更新本地 store
      const newUserInfo = { ...userStore.userInfo, nickname: value }
      userStore.setUserInfo(newUserInfo)
      
      ElMessage.success('修改成功')
    } catch (error) {
      console.error('修改昵称失败:', error)
      ElMessage.error('修改失败，请稍后重试')
    }
  }).catch(() => {})
}

// 暂未实现的功能提示
const showFeatureTip = (feature) => {
  ElMessage.info(`${feature} 功能开发中，敬请期待`)
}
</script>

<template>
  <div class="settings-page">
    <div class="page-header">
      <el-button text @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">设置</h1>
      <div class="placeholder"></div>
    </div>

    <div class="settings-group">
      <div class="group-title">账号与安全</div>
      <el-card shadow="never" class="settings-card">
        <div class="setting-item" @click="handleEditNickname">
          <span class="label">昵称</span>
          <div class="right-content">
            <span class="value">{{ userStore.nickname }}</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
        <el-divider />
        <div class="setting-item" @click="showFeatureTip('修改密码')">
          <span class="label">修改密码</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <el-divider />
        <div class="setting-item" @click="showFeatureTip('换绑手机')">
          <span class="label">换绑手机</span>
          <span class="value">138****8888</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </el-card>
    </div>

    <div class="settings-group">
      <div class="group-title">通用</div>
      <el-card shadow="never" class="settings-card">
        <div class="setting-item">
          <span class="label">消息通知</span>
          <el-switch :model-value="true" />
        </div>
        <el-divider />
        <div class="setting-item" @click="showFeatureTip('清除缓存')">
          <span class="label">清除缓存</span>
          <span class="value">12.5MB</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </el-card>
    </div>

    <div class="settings-group">
      <div class="group-title">关于</div>
      <el-card shadow="never" class="settings-card">
        <div class="setting-item" @click="showFeatureTip('用户协议')">
          <span class="label">用户协议</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <el-divider />
        <div class="setting-item" @click="showFeatureTip('隐私政策')">
          <span class="label">隐私政策</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <el-divider />
        <div class="setting-item" @click="showFeatureTip('关于我们要')">
          <span class="label">关于我们</span>
          <span class="value">v1.0.0</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </el-card>
    </div>

    <div class="logout-section">
      <el-button type="danger" size="large" class="logout-btn" @click="handleLogout">
        退出登录
      </el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.settings-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: 40px;
}

.page-header {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid $border-color;

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }
  
  .placeholder {
    width: 32px;
  }
}

.settings-group {
  padding: $spacing-lg $spacing-lg 0;

  .group-title {
    font-size: 14px;
    color: $text-secondary;
    margin-bottom: $spacing-sm;
    padding-left: $spacing-xs;
  }

  .settings-card {
    border-radius: 12px;
    border: none;
    
    :deep(.el-card__body) {
      padding: 0;
    }

    .setting-item {
      padding: $spacing-md;
      display: flex;
      align-items: center;
      justify-content: space-between;
      cursor: pointer;
      min-height: 56px;

      &:active {
        background-color: $bg-light;
      }

      .label {
        font-size: 16px;
        color: $text-primary;
      }

      .right-content {
        display: flex;
        align-items: center;
      }

      .value {
        font-size: 14px;
        color: $text-secondary;
        margin-right: $spacing-xs;
      }

      .el-icon {
        color: $text-muted;
      }
    }
    
    .el-divider {
      margin: 0;
    }
  }
}

.logout-section {
  padding: $spacing-xl $spacing-lg;
  margin-top: $spacing-lg;

  .logout-btn {
    width: 100%;
    border-radius: 12px;
    font-weight: 600;
  }
}
</style>
