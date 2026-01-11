<template>
  <div class="main-layout">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-left">
        <div class="logo" @click="goHome">
          <img src="@/assets/logo.svg" alt="Logo" />
          <span class="logo-text">校园智教</span>
          <el-tag size="small" type="primary" class="role-tag">教师端</el-tag>
        </div>
      </div>
      
      <nav class="header-center hide-mobile">
        <el-menu 
          :default-active="activeMenu" 
          mode="horizontal" 
          :ellipsis="false"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/home">
            <el-icon><Location /></el-icon>找学生
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><Document /></el-icon>我的订单
          </el-menu-item>
          <el-menu-item index="/lessons">
            <el-icon><Clock /></el-icon>课时记录
          </el-menu-item>
          <el-menu-item index="/resume">
            <el-icon><User /></el-icon>我的简历
          </el-menu-item>
        </el-menu>
      </nav>
      
      <div class="header-right">
        <!-- 消息通知 -->
        <div class="icon-btn" @click="goToChat">
          <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
            <el-icon :size="20"><ChatDotRound /></el-icon>
          </el-badge>
        </div>
        
        <!-- 用户下拉 -->
        <el-dropdown @command="handleUserCommand">
          <div class="user-info">
            <el-avatar :size="36" :src="userStore.avatar" />
            <span class="username hide-mobile">{{ userStore.nickname }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="mine">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item command="auth">
                <el-icon><Medal /></el-icon>资质认证
              </el-dropdown-item>
              <el-dropdown-item command="wallet">
                <el-icon><Wallet /></el-icon>我的钱包
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    
    <!-- 主内容区 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- AI 浮动按钮 -->
    <div class="ai-float-btn" @click="goToAi">
      <el-tooltip content="AI 智能助手" placement="left">
        <el-icon :size="24"><Service /></el-icon>
      </el-tooltip>
    </div>
    
    <!-- 底部导航 (移动端) -->
    <nav class="bottom-nav show-mobile">
      <div 
        v-for="item in bottomNavItems" 
        :key="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
        @click="router.push(item.path)"
      >
        <el-icon :size="22"><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </div>
    </nav>
    
    <!-- 页脚 (桌面端) -->
    <footer class="footer hide-mobile">
      <p>© 2026 校园智教 CampusTutor - 教师端</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore, useChatStore } from '@shared/stores'
import { getUnreadCount } from '@shared/api/chat'
import { 
  Location, Document, Clock, User, ChatDotRound, 
  Medal, Wallet, SwitchButton, Service, House
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatStore()

const unreadCount = ref(0)
let pollTimer = null

// 底部导航配置
const bottomNavItems = [
  { path: '/home', label: '找学生', icon: Location },
  { path: '/orders', label: '订单', icon: Document },
  { path: '/lessons', label: '课时', icon: Clock },
  { path: '/wallet', label: '钱包', icon: Wallet },
  { path: '/mine', label: '我的', icon: User }
]

const activeMenu = computed(() => route.path)

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}

const goHome = () => {
  router.push('/home')
}

const handleMenuSelect = (index) => {
  router.push(index)
}

const goToChat = () => {
  router.push('/chat')
}

const goToAi = () => {
  router.push('/ai')
}

const handleUserCommand = async (command) => {
  switch (command) {
    case 'mine':
      router.push('/mine')
      break
    case 'auth':
      router.push('/auth')
      break
    case 'wallet':
      router.push('/wallet')
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
        ElMessage.success('已退出登录')
      } catch(e) {
        // 取消
      }
      break
  }
}

const fetchUnreadCount = async () => {
  if (!userStore.token) return
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data || 0
      chatStore.setUnreadCount(res.data || 0)
    }
  } catch (error) {
    console.error('获取未读数失败', error)
  }
}

onMounted(() => {
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 30000) // 30秒轮询
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }
})
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  @media (max-width: 768px) {
    padding: 0 12px;
  }
}

.header-left {
  .logo {
    display: flex;
    align-items: center;
    cursor: pointer;
    
    img {
      width: 32px;
      height: 32px;
      margin-right: 8px;
    }
    
    .logo-text {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      
      @media (max-width: 768px) {
        display: none;
      }
    }
    
    .role-tag {
      margin-left: 8px;
    }
  }
}

.header-center {
  :deep(.el-menu) {
    border-bottom: none;
    
    .el-menu-item {
      height: 60px;
      line-height: 60px;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  
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
    cursor: pointer;
    
    .username {
      margin-left: 8px;
      font-size: 14px;
      color: #606266;
    }
  }
}

.main-content {
  flex: 1;
  padding: 24px;
  background: #f5f7fa;
  
  @media (max-width: 768px) {
    padding: 12px;
    padding-bottom: 80px; // 为底部导航留空间
  }
}

.ai-float-btn {
  position: fixed;
  right: 24px;
  bottom: 100px;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  transition: all 0.3s;
  z-index: 99;
  
  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 24px rgba(64, 158, 255, 0.5);
  }
  
  @media (max-width: 768px) {
    right: 16px;
    bottom: 140px;
    width: 48px;
    height: 48px;
  }
}

.bottom-nav {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 60px;
  background: #fff;
  display: flex;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
  
  .nav-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    color: #909399;
    font-size: 12px;
    cursor: pointer;
    transition: color 0.2s;
    
    &.active {
      color: #409eff;
    }
  }
}

.footer {
  padding: 16px;
  text-align: center;
  color: #909399;
  font-size: 12px;
  background: #fff;
}

// 响应式隐藏显示
.hide-mobile {
  @media (max-width: 768px) {
    display: none !important;
  }
}

.show-mobile {
  display: none !important;
  
  @media (max-width: 768px) {
    display: flex !important;
  }
}
</style>
