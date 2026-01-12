<script setup>
import { useRouter } from 'vue-router'
import { computed, ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import { getUnreadCount } from '@/api/chat'

const router = useRouter()
const userStore = useUserStore()
const unreadCount = ref(0)

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
    { icon: 'ChatDotRound', title: '消息中心', path: '/chat', color: 'primary', badge: unreadCount.value },
    { icon: 'ShoppingCart', title: '我的订单', path: '/mine/orders', color: 'warning' },
    { icon: 'Calendar', title: '课时记录', path: '/process/record', color: 'primary' }
  ]
  
  if (isTeacher.value) {
    return [
      ...common,
      { icon: 'Document', title: '简历与资质', path: '/teacher/resume', color: 'success' },
      { icon: 'Wallet', title: '收益提现', path: '/wallet', color: 'info' }
    ]
  } else {
    return [
      ...common,
      { icon: 'List', title: '需求管理', path: '/parent/demands', color: 'warning' },
      { icon: 'UserFilled', title: '我的孩子', path: '/students', color: 'primary' },
      { icon: 'Notebook', title: '智能错题本', path: '/parent/wrong-book', color: 'danger' },
      { icon: 'CreditCard', title: '托管资金', path: '/wallet', color: 'info' }
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

// 加载未读消息数
const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (error) {
    console.error('获取未读消息数失败:', error)
  }
}

onMounted(() => {
  loadUnreadCount()
})
</script>

<template>
  <div class="mine-page">
    <!-- 用户信息头部 -->
    <div class="user-header" :class="isTeacher ? 'teacher' : 'parent'">
      <div class="header-content">
        <div class="user-info">
          <el-avatar :size="80" :src="user.avatar" />
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
    </div>

    <!-- 内容区域 -->
    <div class="content-wrapper">
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
              <el-icon :size="22">
                <component :is="item.icon" />
              </el-icon>
            </div>
            <span class="menu-title">{{ item.title }}</span>
            <el-badge 
              v-if="item.badge" 
              :value="item.badge" 
              :max="99"
              class="menu-badge"
            />
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
  </div>
</template>

<style lang="scss" scoped>
.mine-page {
  min-height: calc(100vh - 114px);
  background: $bg-light;
}

.user-header {
  padding: 48px 24px 90px;
  
  &.teacher {
    background: linear-gradient(135deg, $success-color 0%, #14b8a6 100%);
  }
  
  &.parent {
    background: linear-gradient(135deg, $primary-color 0%, #667eea 100%);
  }

  .header-content {
    max-width: 900px;
    margin: 0 auto;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 20px;
    color: #fff;

    .user-detail {
      .user-name {
        font-size: 24px;
        font-weight: 700;
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 8px;
      }

      .user-id {
        font-size: 14px;
        opacity: 0.9;
        font-family: monospace;
      }
    }
  }
}

.content-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 24px 40px;
}

.stats-card {
  margin-top: -60px;
  border-radius: 16px;
  position: relative;
  z-index: 10;
  margin-bottom: 24px;

  .stats-grid {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 8px 0;

    .el-divider {
      height: 40px;
    }

    .stat-item {
      flex: 1;
      text-align: center;
      padding: 12px 0;

      .stat-value {
        font-size: 28px;
        font-weight: 800;
        color: $text-primary;
      }

      .stat-label {
        font-size: 13px;
        color: $text-muted;
        margin-top: 6px;
      }
    }
  }
}

.menu-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;

  .menu-card {
    cursor: pointer;
    border-radius: 14px;
    transition: all 0.3s;

    &:hover {
      transform: translateX(6px);
      box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
    }

    :deep(.el-card__body) {
      padding: 18px 24px;
    }
  }

  .menu-item {
    display: flex;
    align-items: center;
    gap: 16px;

    .menu-icon {
      width: 46px;
      height: 46px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;

      &.bg-primary { background: linear-gradient(135deg, $primary-color, #667eea); }
      &.bg-success { background: linear-gradient(135deg, $success-color, #14b8a6); }
      &.bg-warning { background: linear-gradient(135deg, $warning-color, #f97316); }
      &.bg-danger { background: linear-gradient(135deg, $danger-color, #ef4444); }
      &.bg-info { background: linear-gradient(135deg, $info-color, #64748b); }
    }

    .menu-title {
      flex: 1;
      font-weight: 600;
      font-size: 16px;
      color: $text-primary;
    }

    .menu-arrow {
      color: $text-muted;
    }
  }
}

.bottom-section {
  display: flex;
  gap: 16px;

  .el-button {
    flex: 1;
    height: 50px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 12px;
  }
}

@media (max-width: 768px) {
  .user-header {
    padding: 40px 16px 80px;
  }

  .content-wrapper {
    padding: 0 16px 32px;
  }

  .stats-card .stats-grid .stat-item .stat-value {
    font-size: 24px;
  }
}
</style>