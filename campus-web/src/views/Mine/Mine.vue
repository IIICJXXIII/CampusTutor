<script setup>
import { useRouter } from 'vue-router'
import { computed } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'

const router = useRouter()
const userStore = useUserStore()

// 获取当前身份
const isTeacher = computed(() => userStore.isTutor)
const isParent = computed(() => userStore.isParent)

// 用户信息
const user = computed(() => {
  const userInfo = userStore.userInfo
  const isT = isTeacher.value
  return {
    name: userInfo?.nickname || (isT ? '老师' : '家长'),
    id: userInfo?.userId ? (isT ? `T-${userInfo.userId}` : `P-${userInfo.userId}`) : (isT ? 'T-0000' : 'P-0000'),
    avatar: userInfo?.avatar || `https://api.dicebear.com/7.x/${isT ? 'miniavs' : 'adventurer'}/svg?seed=${userInfo?.userId || 1}`,
    balance: isT ? 450 : 0,
    label: isT ? '认证教师' : 'VIP家长'
  }
})

// 统计数据
const stats = computed(() => ({
  balance: isTeacher.value ? 450 : 0,
  lessons: 12,
  pending: 3
}))

// 菜单列表
const menuList = computed(() => {
  const common = [
    { icon: 'ShoppingCart', title: '我的订单', path: '/mine/orders', color: 'warning' },
    { icon: 'Calendar', title: '课时记录', path: '/process/record', color: 'primary' }
  ]
  
  if (isTeacher.value) {
    return [
      ...common,
      { icon: 'Document', title: '简历与资质', path: '/teacher/resume', color: 'success' },
      { icon: 'Wallet', title: '收益提现', path: '/teacher/wallet', color: 'info' }
    ]
  } else {
    return [
      ...common,
      { icon: 'Notebook', title: '智能错题本', path: '/parent/wrong-book', color: 'danger' },
      { icon: 'CreditCard', title: '托管资金', path: '/parent/wallet', color: 'info' }
    ]
  }
})

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

// 图标颜色映射
const getColorClass = (color) => {
  const map = {
    primary: 'bg-primary',
    success: 'bg-success',
    warning: 'bg-warning',
    danger: 'bg-danger',
    info: 'bg-info'
  }
  return map[color] || 'bg-primary'
}
</script>

<template>
  <div class="mine-page">
    <!-- 用户信息头部 -->
    <div class="user-header" :class="isTeacher ? 'teacher' : 'parent'">
      <div class="user-info">
        <el-avatar :size="72" :src="user.avatar" />
        <div class="user-detail">
          <h2 class="user-name">
            {{ user.name }}
            <el-tag :type="isTeacher ? 'success' : 'primary'" size="small" effect="dark">
              {{ user.label }}
            </el-tag>
          </h2>
          <p class="user-id">ID: {{ user.id }}</p>
        </div>
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <el-card class="stats-card" shadow="hover">
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ stats.balance }}</div>
          <div class="stat-label">{{ isTeacher ? '可提现(元)' : '托管资金' }}</div>
        </div>
        <el-divider direction="vertical" />
        <div class="stat-item">
          <div class="stat-value">{{ stats.lessons }}</div>
          <div class="stat-label">剩余课时</div>
        </div>
        <el-divider direction="vertical" />
        <div class="stat-item">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待办事项</div>
        </div>
      </div>
    </el-card>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <el-card 
        v-for="item in menuList" 
        :key="item.path"
        class="menu-card"
        shadow="hover"
        @click="router.push(item.path)"
      >
        <div class="menu-item">
          <div class="menu-icon" :class="getColorClass(item.color)">
            <el-icon :size="20">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <span class="menu-title">{{ item.title }}</span>
          <el-icon class="menu-arrow"><ArrowRight /></el-icon>
        </div>
      </el-card>
    </div>

    <!-- 设置与退出 -->
    <div class="bottom-section">
      <el-button size="large" @click="router.push('/settings')">
        <el-icon><Setting /></el-icon>
        设置
      </el-button>
      <el-button size="large" type="danger" plain @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        退出登录
      </el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.mine-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: 80px;
}

.user-header {
  padding: 40px $spacing-lg 80px;
  border-radius: 0 0 40px 40px;
  
  &.teacher {
    background: linear-gradient(135deg, $success-color 0%, #14b8a6 100%);
  }
  
  &.parent {
    background: linear-gradient(135deg, $primary-color 0%, #667eea 100%);
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    color: #fff;

    .user-detail {
      .user-name {
        font-size: 20px;
        font-weight: 700;
        display: flex;
        align-items: center;
        gap: $spacing-sm;
        margin-bottom: 4px;
      }

      .user-id {
        font-size: 12px;
        opacity: 0.8;
        font-family: monospace;
      }
    }
  }
}

.stats-card {
  margin: -50px $spacing-lg $spacing-lg;
  border-radius: 16px;
  position: relative;
  z-index: 10;

  .stats-grid {
    display: flex;
    align-items: center;
    justify-content: center;

    .stat-item {
      flex: 1;
      text-align: center;
      padding: $spacing-sm 0;

      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: $text-primary;
      }

      .stat-label {
        font-size: 12px;
        color: $text-muted;
        margin-top: 4px;
      }
    }
  }
}

.menu-section {
  padding: 0 $spacing-lg;
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;

  .menu-card {
    cursor: pointer;
    border-radius: 12px;
    transition: all 0.3s;

    &:hover {
      transform: translateX(4px);
    }

    :deep(.el-card__body) {
      padding: $spacing-md;
    }
  }

  .menu-item {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .menu-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;

      &.bg-primary { background: $primary-color; }
      &.bg-success { background: $success-color; }
      &.bg-warning { background: $warning-color; }
      &.bg-danger { background: $danger-color; }
      &.bg-info { background: $info-color; }
    }

    .menu-title {
      flex: 1;
      font-weight: 600;
      color: $text-primary;
    }

    .menu-arrow {
      color: $text-muted;
    }
  }
}

.bottom-section {
  padding: $spacing-xl $spacing-lg;
  display: flex;
  gap: $spacing-md;

  .el-button {
    flex: 1;
    height: 48px;
  }
}
</style>