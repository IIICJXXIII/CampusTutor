<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-left">
        <div class="logo" @click="goHome">
          <img src="@/assets/logo.svg" alt="Logo" />
          <span class="logo-text">校园智教</span>
        </div>
      </div>
      
      <div class="header-center">
        <el-menu 
          :default-active="activeMenu" 
          mode="horizontal" 
          :ellipsis="false"
          @select="handleMenuSelect"
        >
          <el-menu-item v-if="isParent" index="/parent/demand">发布需求</el-menu-item>
          <el-menu-item v-if="isParent" index="/teacher/list">找老师</el-menu-item>
          <el-menu-item v-if="isParent" index="/parent/tutors">智能推荐</el-menu-item>
          <el-menu-item v-if="isTutor" index="/teacher/students">找学生</el-menu-item>
          <el-menu-item v-if="isTutor" index="/teacher/resume">我的简历</el-menu-item>
          <el-menu-item index="/mine/orders">我的订单</el-menu-item>
          <el-menu-item index="/process/record">课时记录</el-menu-item>
        </el-menu>
      </div>
      
      <div class="header-right">
        <!-- 角色切换 -->
        <el-dropdown class="role-switch" @command="handleRoleSwitch">
          <span class="role-label">
            <el-icon><User /></el-icon>
            {{ isParent ? '家长模式' : '教师模式' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="parent" :disabled="isParent">
                <el-icon><User /></el-icon>切换为家长
              </el-dropdown-item>
              <el-dropdown-item command="tutor" :disabled="isTutor">
                <el-icon><Reading /></el-icon>切换为教师
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 用户下拉 -->
        <el-dropdown @command="handleUserCommand">
          <div class="user-info">
            <el-avatar :size="36" :src="userStore.avatar" />
            <span class="username">{{ userStore.nickname }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><UserFilled /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item v-if="isTutor" command="auth">
                <el-icon><Medal /></el-icon>资质认证
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>
    
    <!-- 主内容区域 -->
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    
    <!-- 页脚 -->
    <footer class="footer">
      <p>© 2026 校园智教 CampusTutor - 大学生家教智能服务平台</p>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/index'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isParent = computed(() => userStore.isParent)
const isTutor = computed(() => userStore.isTutor)
const activeMenu = computed(() => route.path)

const goHome = () => {
  if (isParent.value) {
    router.push('/parent/tutors')
  } else {
    router.push('/teacher/students')
  }
}

const handleMenuSelect = (index) => {
  router.push(index)
}

const handleRoleSwitch = (role) => {
  userStore.setRole(role)
  ElMessage.success(`已切换为${role === 'parent' ? '家长' : '教师'}模式`)
  
  // 切换后跳转到对应首页
  if (role === 'parent') {
    router.push('/parent/tutors')
  } else {
    router.push('/teacher/students')
  }
}

const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/mine')
      break
    case 'auth':
      router.push('/teacher/auth')
      break
    case 'logout':
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      userStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
      break
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: $bg-color;
}

.header {
  height: $header-height;
  background: $bg-white;
  box-shadow: $shadow-sm;
  display: flex;
  align-items: center;
  padding: 0 $spacing-lg;
  position: sticky;
  top: 0;
  z-index: 100;
  
  .header-left {
    .logo {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      img {
        width: 36px;
        height: 36px;
      }
      
      .logo-text {
        font-size: 18px;
        font-weight: 600;
        color: $primary-color;
        margin-left: $spacing-sm;
      }
    }
  }
  
  .header-center {
    flex: 1;
    display: flex;
    justify-content: center;
    
    :deep(.el-menu) {
      border-bottom: none;
      background: transparent;
      
      .el-menu-item {
        font-size: 15px;
        
        &.is-active {
          font-weight: 600;
        }
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: $spacing-lg;
    
    .role-switch {
      .role-label {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 6px 12px;
        background: $primary-light;
        color: $primary-color;
        border-radius: $radius-md;
        font-size: 13px;
        cursor: pointer;
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      cursor: pointer;
      
      .username {
        font-size: 14px;
        color: $text-primary;
      }
    }
  }
}

.main-content {
  flex: 1;
  padding: $spacing-lg;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
}

.footer {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-white;
  border-top: 1px solid $border-lighter;
  
  p {
    font-size: 13px;
    color: $text-secondary;
  }
}

@media (max-width: $breakpoint-md) {
  .header {
    padding: 0 $spacing-md;
    
    .header-center {
      display: none;
    }
    
    .logo-text {
      display: none;
    }
  }
  
  .main-content {
    padding: $spacing-md;
  }
}
</style>
