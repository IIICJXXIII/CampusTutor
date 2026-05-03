<template>
  <div class="main-layout" :class="layoutClass">
    <!-- 桌面端顶部导航 -->
    <header v-if="!isMobile" class="desktop-header">
      <div class="header-container">
        <!-- Logo -->
        <div class="logo" @click="goHome">
          <img src="@/assets/logo.svg" alt="Logo" />
          <span class="logo-text">校园智教</span>
          <el-tag size="small" :type="isTeacher ? 'primary' : 'warning'" class="role-tag">
            {{ isTeacher ? '教师端' : '家长端' }}
          </el-tag>
        </div>

        <!-- 导航菜单 -->
        <nav class="nav-menu">
          <el-menu
            :default-active="activeMenu"
            mode="horizontal"
            :ellipsis="false"
            @select="handleMenuSelect"
          >
            <!-- 家长端菜单 -->
            <template v-if="!isTeacher">
              <el-menu-item index="/parent/home">
                <el-icon><Search /></el-icon>找老师
              </el-menu-item>
              <el-menu-item index="/parent/demands">
                <el-icon><List /></el-icon>我的需求
              </el-menu-item>
              <el-menu-item index="/community" class="community-menu-item">
                <el-icon><ChatLineSquare /></el-icon>社区
              </el-menu-item>
              <el-menu-item index="/parent/orders">
                <el-icon><Document /></el-icon>我的订单
              </el-menu-item>
            </template>
            <template v-else>
              <el-menu-item index="/teacher/home">
                <el-icon><Location /></el-icon>找学生
              </el-menu-item>
              <el-menu-item index="/community" class="community-menu-item">
                <el-icon><ChatLineSquare /></el-icon>社区
              </el-menu-item>
              <el-menu-item index="/teacher/orders">
                <el-icon><Document /></el-icon>我的订单
              </el-menu-item>
              <el-menu-item index="/teacher/resume">
                <el-icon><User /></el-icon>我的简历
              </el-menu-item>
              <el-menu-item index="/teacher/ai/hub">
                <el-icon><Service /></el-icon>AI助手
              </el-menu-item>
            </template>
          </el-menu>
        </nav>

        <!-- 右侧操作区 -->
        <div class="header-actions">
          <!-- 发布需求按钮（家长端独有） -->
          <el-button v-if="!isTeacher" type="primary" @click="createDemand">发布需求</el-button>

          <!-- 消息通知 -->
          <div class="icon-btn" @click="goToChat">
            <el-badge :value="chatStore.unreadCount" :max="99" :hidden="chatStore.unreadCount === 0">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </el-badge>
          </div>

          <!-- 用户下拉 -->
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.avatar">
                {{ userStore.nickname?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.nickname }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="mine">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item v-if="isTeacher" command="auth">
                  <el-icon><Medal /></el-icon>资质认证
                </el-dropdown-item>
                <el-dropdown-item v-if="!isTeacher" command="students">
                  <el-icon><UserFilled /></el-icon>我的孩子
                </el-dropdown-item>
                <el-dropdown-item command="wallet">
                  <el-icon><Wallet /></el-icon>我的钱包
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 移动端顶部 -->
    <header v-else class="mobile-header">
      <div class="header-title">{{ route.meta.title || '校园智教' }}</div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content" :class="{ 'with-tabbar': isMobile }">
      <router-view v-slot="{ Component }">
        <keep-alive :include="cachedViews">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </main>

    <!-- 移动端底部导航 -->
    <nav v-if="isMobile" class="mobile-tabbar">
      <div
        v-for="item in mobileTabItems"
        :key="item.path"
        class="tab-item"
        :class="{ active: isTabActive(item.path), 'community-tab': item.isCommunity }"
        @click="item.isPublish ? createDemand() : router.push(item.path)"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span v-if="!item.isPublish">{{ item.label }}</span>
      </div>
    </nav>

    <!-- AI 悬浮按钮 -->
    <div v-if="showAiButton" class="ai-float-btn" @click="openAiChat">
      <el-icon><ChatDotRound /></el-icon>
      <span class="ai-label">AI助手</span>
    </div>

    <!-- 地址获取弹窗 -->
    <el-dialog
      v-model="locationDialogVisible"
      title="获取您的位置"
      width="400px"
      :close-on-click-modal="false"
      :show-close="false"
      align-center
    >
      <div class="location-dialog-content">
        <el-icon :size="48" color="#667eea"><Location /></el-icon>
        <p>为了更好地为您推荐附近的教师/学生，我们需要获取您的位置信息。</p>
        <p class="location-hint">您的位置信息仅用于服务推荐，不会公开显示。</p>
      </div>
      <template #footer>
        <el-button @click="skipLocation">暂不授权</el-button>
        <el-button type="primary" @click="grantLocation" :loading="locating">授权获取位置</el-button>
      </template>
    </el-dialog>

    <!-- 页脚 (桌面端) -->
    <footer v-if="!isMobile" class="footer">
      <p>© 2026 校园智教 CampusTutor - 大学生家教智能服务平台</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, List, Plus, Document, User, ChatDotRound,
  Location, Clock, Service, Medal, Wallet, SwitchButton,
  UserFilled, Setting, ChatLineSquare
} from '@element-plus/icons-vue'
import { useUserStore, useChatStore } from '@shared/stores'
import { getUnreadCount } from '@shared/api/chat'
import { reverseGeocode } from '@shared/api/map'
import { updateUserInfo } from '@shared/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const chatStore = useChatStore()

const isMobile = ref(window.innerWidth < 768)
let pollTimer = null

// 角色判断
const isTeacher = computed(() => userStore.userRole === 'tutor')

// 布局样式类
const layoutClass = computed(() => isTeacher.value ? 'teacher-theme' : 'parent-theme')

// 缓存的视图
const cachedViews = ['ParentHome', 'ParentDemandList', 'ParentOrderList', 'TeacherHome', 'TeacherOrderList', 'Mine']

// 激活菜单
const activeMenu = computed(() => route.path)

// AI按钮显示
const showAiButton = computed(() => {
  return route.name !== 'AiChat' && route.name !== 'ChatRoom'
})

// 移动端 Tab 配置
const mobileTabItems = computed(() => {
  if (isTeacher.value) {
    return [
      { path: '/teacher/home', label: '找学生', icon: Location },
      { path: '/teacher/orders', label: '订单', icon: Document },
      { path: '/community', label: '社区', icon: ChatLineSquare, isCommunity: true },
      { path: '/teacher/wallet', label: '钱包', icon: Wallet },
      { path: '/mine', label: '我的', icon: User }
    ]
  } else {
    return [
      { path: '/parent/home', label: '找老师', icon: Search },
      { path: '/parent/demands', label: '需求', icon: List },
      { path: '/community', label: '社区', icon: ChatLineSquare, isCommunity: true },
      { path: '/parent/orders', label: '订单', icon: Document },
      { path: '/mine', label: '我的', icon: User }
    ]
  }
})

const isTabActive = (path) => {
  if (path === '#publish') return false
  return route.path === path || route.path.startsWith(path + '/')
}

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
}

const goHome = () => {
  router.push(isTeacher.value ? '/teacher/home' : '/parent/home')
}

const createDemand = () => {
  router.push('/parent/demands/create')
}

const openAiChat = () => {
  router.push('/ai')
}

const goToChat = () => {
  router.push('/chat')
}

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleUserCommand = async (command) => {
  switch (command) {
    case 'mine':
      router.push('/mine')
      break
    case 'auth':
      router.push('/teacher/auth')
      break
    case 'students':
      router.push('/parent/students')
      break
    case 'wallet':
      router.push(isTeacher.value ? '/teacher/wallet' : '/parent/wallet')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        userStore.logout()
        router.push('/login')
      } catch (e) {
      }
      break
  }
}

const fetchUnreadCount = async () => {
  if (!userStore.token) return
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      chatStore.setUnreadCount(res.data || 0)
    }
  } catch (error) {
  }
}

const locationDialogVisible = ref(false)
const locating = ref(false)

const LOCATION_DECIDED_KEY = 'location_permission_decided'

const requestLocation = () => {
  const info = userStore.userInfo
  if (info?.longitude && info?.latitude) return

  const decided = localStorage.getItem(LOCATION_DECIDED_KEY)
  if (decided) return

  locationDialogVisible.value = true
}

const skipLocation = () => {
  localStorage.setItem(LOCATION_DECIDED_KEY, 'skipped')
  locationDialogVisible.value = false
}

const grantLocation = async () => {
  locating.value = true
  try {
    const pos = await new Promise((resolve, reject) => {
      if (!navigator.geolocation) return reject(new Error('不支持定位'))
      navigator.geolocation.getCurrentPosition(resolve, reject, {
        enableHighAccuracy: false, timeout: 8000, maximumAge: 300000
      })
    })
    const { longitude, latitude } = pos.coords
    try {
      const geoRes = await reverseGeocode(latitude, longitude)
      if (geoRes.code === 200 && geoRes.data) {
        const addr = geoRes.data
        await updateUserInfo({
          longitude, latitude,
          address: addr.formattedAddress || addr.address || '',
          region: [addr.province, addr.city, addr.district].filter(Boolean).join(',')
        })
        userStore.setUserInfo({
          ...userStore.userInfo,
          longitude, latitude,
          address: addr.formattedAddress || addr.address || '',
          region: [addr.province, addr.city, addr.district].filter(Boolean).join(',')
        })
        localStorage.setItem(LOCATION_DECIDED_KEY, 'granted')
        ElMessage.success('位置获取成功')
      }
    } catch (e) {
    }
  } catch (e) {
    localStorage.setItem(LOCATION_DECIDED_KEY, 'denied')
    ElMessage.warning('无法获取位置，您可以稍后在设置中授权')
  } finally {
    locating.value = false
    locationDialogVisible.value = false
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000)
  requestLocation()
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

// ===================== 桌面端头部 =====================
.desktop-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .header-container {
    max-width: 1400px;
    height: 60px;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: center;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    flex-shrink: 0;

    img {
      width: 32px;
      height: 32px;
    }

    .logo-text {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .role-tag {
      font-size: 11px;
    }
  }

  .nav-menu {
    flex: 1;
    display: flex;
    justify-content: center;

    :deep(.el-menu) {
      border-bottom: none;
      background: transparent;

      .el-menu-item {
        height: 60px;
        line-height: 60px;
        font-size: 15px;

        &.is-active {
          font-weight: 600;
        }
      }

      .community-menu-item {
        position: relative;

        .el-icon {
          color: #667eea;
        }
      }
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-shrink: 0;

    .icon-btn {
      cursor: pointer;
      padding: 8px;
      border-radius: 8px;
      transition: background 0.2s;

      &:hover {
        background: #f5f7fa;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

// ===================== 移动端头部 =====================
.mobile-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 50px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  z-index: 100;

  .header-title {
    font-size: 17px;
    font-weight: 600;
  }
}

// ===================== 主内容区 =====================
.main-content {
  flex: 1;
  padding: 24px;

  > * {
    max-width: 1200px;
    margin: 0 auto;
  }

  &.with-tabbar {
    padding-top: 60px;
    padding-bottom: 80px;
  }
}

// ===================== 移动端底部导航 =====================
.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-around;
  box-shadow: 0 -1px 4px rgba(0, 0, 0, 0.06);
  z-index: 100;
  padding-bottom: env(safe-area-inset-bottom);

  .tab-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    color: #909399;
    font-size: 11px;
    cursor: pointer;
    transition: color 0.2s;

    .el-icon {
      font-size: 22px;
    }

    &.active {
      color: var(--el-color-primary);
    }

    &.community-tab {
      position: relative;

      &::after {
        content: '';
        position: absolute;
        top: -2px;
        right: 50%;
        transform: translateX(14px);
        width: 6px;
        height: 6px;
        background: #f56c6c;
        border-radius: 50%;
      }

      .el-icon {
        color: #667eea;
      }
    }

    &.publish-btn {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
      color: #fff;
      justify-content: center;
      margin-top: -20px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

      .el-icon {
        font-size: 26px;
      }
    }
  }
}

// ===================== AI 悬浮按钮 =====================
.location-dialog-content {
  text-align: center;
  padding: 16px 0;

  p {
    font-size: 15px;
    color: #606266;
    margin: 16px 0 0;
  }

  .location-hint {
    font-size: 13px;
    color: #909399;
    margin-top: 8px;
  }
}

.ai-float-btn {
  position: fixed;
  bottom: 80px;
  right: 20px;
  height: 44px;
  padding: 0 16px 0 12px;
  border-radius: 22px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: transform 0.2s;
  z-index: 99;
  animation: ai-pulse 2s ease-in-out infinite;

  .el-icon {
    font-size: 20px;
  }

  .ai-label {
    font-size: 13px;
    font-weight: 500;
    white-space: nowrap;
  }

  &:hover {
    transform: scale(1.05);
  }
}

@keyframes ai-pulse {
  0%, 100% { box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4); }
  50% { box-shadow: 0 4px 24px rgba(102, 126, 234, 0.7); }
}

// ===================== 页脚 =====================
.footer {
  padding: 16px;
  text-align: center;
  color: #909399;
  font-size: 12px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}

// ===================== 页面切换动画 =====================
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// ===================== 响应式 =====================
@media (max-width: 768px) {
  .main-content {
    padding: 16px;

    > * {
      padding: 0;
    }
  }

  .ai-float-btn {
    bottom: 80px;
    right: 16px;
  }
}
</style>