<template>
  <div class="main-layout">
    <!-- 顶部导航 (桌面端) -->
    <header v-if="!isMobile" class="desktop-header">
      <div class="header-container">
        <div class="logo" @click="goHome">
          <img src="@/assets/logo.svg" alt="Logo" />
          <span>校园家教 · 家长端</span>
        </div>
        
        <nav class="nav-menu">
          <router-link to="/home" class="nav-item">找老师</router-link>
          <router-link to="/demands" class="nav-item">我的需求</router-link>
          <router-link to="/orders" class="nav-item">我的订单</router-link>
          <router-link to="/chat" class="nav-item">消息</router-link>
        </nav>
        
        <div class="header-actions">
          <el-button type="primary" @click="createDemand">发布需求</el-button>
          <el-dropdown @command="handleCommand">
            <el-avatar :size="36" :src="userStore.avatar">
              {{ userStore.nickname?.charAt(0) }}
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="mine">个人中心</el-dropdown-item>
                <el-dropdown-item command="students">我的孩子</el-dropdown-item>
                <el-dropdown-item command="wallet">我的钱包</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    
    <!-- 移动端顶部 -->
    <header v-else class="mobile-header">
      <div class="header-title">{{ route.meta.title || '校园家教' }}</div>
    </header>
    
    <!-- 主内容区 -->
    <main class="main-content" :class="{ 'with-tabbar': showTabbar }">
      <router-view v-slot="{ Component }">
        <keep-alive :include="cachedViews">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </main>
    
    <!-- 移动端底部导航 -->
    <nav v-if="isMobile && showTabbar" class="mobile-tabbar">
      <router-link to="/home" class="tab-item" :class="{ active: route.path === '/home' }">
        <el-icon><Search /></el-icon>
        <span>找老师</span>
      </router-link>
      <router-link to="/demands" class="tab-item" :class="{ active: route.path.startsWith('/demands') }">
        <el-icon><List /></el-icon>
        <span>需求</span>
      </router-link>
      <div class="tab-item publish-btn" @click="createDemand">
        <el-icon><Plus /></el-icon>
      </div>
      <router-link to="/orders" class="tab-item" :class="{ active: route.path.startsWith('/orders') }">
        <el-icon><Document /></el-icon>
        <span>订单</span>
      </router-link>
      <router-link to="/mine" class="tab-item" :class="{ active: route.path === '/mine' }">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </router-link>
    </nav>
    
    <!-- AI 悬浮按钮 -->
    <div v-if="showAiButton" class="ai-float-btn" @click="openAiChat">
      <el-icon><ChatDotRound /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Search, List, Plus, Document, User, ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@shared/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isMobile = ref(false)
const cachedViews = ['Home', 'DemandList', 'OrderList', 'Mine']

const showTabbar = computed(() => {
  return route.meta.tabbar !== false
})

const showAiButton = computed(() => {
  return route.name !== 'AiChat' && route.name !== 'ChatRoom'
})

const checkMobile = () => {
  isMobile.value = window.innerWidth < 768
}

const goHome = () => {
  router.push('/')
}

const createDemand = () => {
  router.push('/demands/create')
}

const openAiChat = () => {
  router.push('/ai')
}

const handleCommand = (command) => {
  switch (command) {
    case 'mine':
      router.push('/mine')
      break
    case 'students':
      router.push('/students')
      break
    case 'wallet':
      router.push('/wallet')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示').then(() => {
        userStore.logout()
        router.push('/login')
      }).catch(() => {})
      break
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

// 桌面端头部
.desktop-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 100;
  
  .header-container {
    max-width: 1200px;
    height: 100%;
    margin: 0 auto;
    padding: 0 24px;
    display: flex;
    align-items: center;
  }
  
  .logo {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    
    img {
      width: 36px;
      height: 36px;
    }
    
    span {
      font-size: 18px;
      font-weight: 600;
      color: #ff9500;
    }
  }
  
  .nav-menu {
    flex: 1;
    display: flex;
    justify-content: center;
    gap: 40px;
    
    .nav-item {
      font-size: 15px;
      color: #606266;
      text-decoration: none;
      padding: 8px 0;
      border-bottom: 2px solid transparent;
      transition: all 0.2s;
      
      &:hover, &.router-link-active {
        color: #ff9500;
        border-bottom-color: #ff9500;
      }
    }
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .el-avatar {
      cursor: pointer;
    }
  }
}

// 移动端头部
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

// 主内容区
.main-content {
  padding-top: 60px;
  min-height: 100vh;
  
  &.with-tabbar {
    padding-bottom: 70px;
  }
  
  > * {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
  }
}

// 移动端底部导航
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
    text-decoration: none;
    font-size: 11px;
    
    .el-icon {
      font-size: 22px;
    }
    
    &.active {
      color: #ff9500;
    }
    
    &.publish-btn {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, #ff9500, #ffb74d);
      color: #fff;
      justify-content: center;
      margin-top: -20px;
      box-shadow: 0 4px 12px rgba(255, 149, 0, 0.3);
      
      .el-icon {
        font-size: 26px;
      }
    }
  }
}

// AI 悬浮按钮
.ai-float-btn {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: transform 0.2s;
  z-index: 99;
  
  .el-icon {
    font-size: 24px;
  }
  
  &:hover {
    transform: scale(1.1);
  }
}

// 响应式
@media (max-width: 768px) {
  .main-content {
    padding-top: 50px;
    
    > * {
      padding: 16px;
    }
  }
  
  .ai-float-btn {
    bottom: 140px;
  }
}
</style>
