<template>
  <div class="mine-page">
    <!-- 用户信息卡片 -->
    <div class="user-card" @click="editProfile">
      <el-avatar :size="64" :src="userInfo.avatar">
        {{ userInfo.name?.charAt(0) }}
      </el-avatar>
      <div class="user-info">
        <div class="user-name">{{ userInfo.name || '未登录' }}</div>
        <div class="user-phone">{{ userInfo.phone || '点击登录' }}</div>
      </div>
      <el-icon><ArrowRight /></el-icon>
    </div>
    
    <!-- 快捷入口 -->
    <div class="quick-stats">
      <div class="stat-item" @click="goTo('/students')">
        <div class="stat-value">{{ stats.studentCount || 0 }}</div>
        <div class="stat-label">我的学生</div>
      </div>
      <div class="stat-item" @click="goTo('/demands')">
        <div class="stat-value">{{ stats.demandCount || 0 }}</div>
        <div class="stat-label">发布的需求</div>
      </div>
      <div class="stat-item" @click="goTo('/orders')">
        <div class="stat-value">{{ stats.orderCount || 0 }}</div>
        <div class="stat-label">订单数</div>
      </div>
    </div>
    
    <!-- 功能菜单 -->
    <div class="menu-section">
      <div class="menu-group">
        <div class="menu-item" @click="goTo('/wallet')">
          <div class="menu-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="menu-text">我的钱包</div>
          <div class="menu-extra">
            余额: ¥{{ walletBalance.toFixed(2) }}
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item" @click="goTo('/orders')">
          <div class="menu-icon" style="background: linear-gradient(135deg, #11998e, #38ef7d)">
            <el-icon><Document /></el-icon>
          </div>
          <div class="menu-text">我的订单</div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item" @click="goTo('/lessons')">
          <div class="menu-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="menu-text">课时记录</div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <div class="menu-group">
        <div class="menu-item" @click="goTo('/students')">
          <div class="menu-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
            <el-icon><User /></el-icon>
          </div>
          <div class="menu-text">学生管理</div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item" @click="goTo('/demands')">
          <div class="menu-icon" style="background: linear-gradient(135deg, #fa709a, #fee140)">
            <el-icon><Edit /></el-icon>
          </div>
          <div class="menu-text">我的需求</div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item" @click="goTo('/ai-chat')">
          <div class="menu-icon" style="background: linear-gradient(135deg, #a8edea, #fed6e3)">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="menu-text">AI助手</div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <div class="menu-group">
        <div class="menu-item" @click="goTo('/settings')">
          <div class="menu-icon" style="background: #909399">
            <el-icon><Setting /></el-icon>
          </div>
          <div class="menu-text">设置</div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item" @click="showAbout">
          <div class="menu-icon" style="background: #909399">
            <el-icon><InfoFilled /></el-icon>
          </div>
          <div class="menu-text">关于我们</div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
    
    <!-- 退出登录 -->
    <div class="logout-section">
      <el-button type="danger" plain size="large" @click="handleLogout">
        退出登录
      </el-button>
    </div>
    
    <!-- 关于弹窗 -->
    <el-dialog v-model="aboutVisible" title="关于我们" width="400px">
      <div class="about-content">
        <div class="app-logo">
          <img src="@/assets/logo.png" alt="logo" />
        </div>
        <h3>校园家教平台</h3>
        <p>版本: v1.0.0</p>
        <p class="desc">
          连接优质大学生家教与家长的平台，为孩子提供专业的辅导服务
        </p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, useWalletStore } from '@shared/stores'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowRight, Wallet, Document, Calendar, User, Edit, 
  ChatDotRound, Setting, InfoFilled 
} from '@element-plus/icons-vue'
import { getParentStats } from '@shared/api/parent'

const router = useRouter()
const userStore = useUserStore()
const walletStore = useWalletStore()

const aboutVisible = ref(false)
const stats = ref({})

const userInfo = computed(() => userStore.user || {})
const walletBalance = computed(() => walletStore.balance || 0)

const loadStats = async () => {
  try {
    const res = await getParentStats()
    if (res.code === 200) {
      stats.value = res.data || {}
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

const goTo = (path) => {
  router.push(path)
}

const editProfile = () => {
  if (!userInfo.value.id) {
    router.push('/login')
  } else {
    router.push('/settings/profile')
  }
}

const showAbout = () => {
  aboutVisible.value = true
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示')
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {}
}

onMounted(() => {
  loadStats()
  walletStore.fetchWallet()
})
</script>

<style lang="scss" scoped>
.mine-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  padding-bottom: 100px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 24px;
  color: #fff;
  cursor: pointer;
  margin-bottom: 20px;
  
  .user-info {
    flex: 1;
    
    .user-name {
      font-size: 20px;
      font-weight: 600;
      margin-bottom: 4px;
    }
    
    .user-phone {
      font-size: 14px;
      opacity: 0.9;
    }
  }
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
  
  .stat-item {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #409eff;
    }
    
    .stat-label {
      font-size: 13px;
      color: #666;
      margin-top: 4px;
    }
  }
}

.menu-section {
  .menu-group {
    background: #fff;
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
  
  .menu-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    cursor: pointer;
    transition: background 0.2s;
    
    &:not(:last-child) {
      border-bottom: 1px solid #f5f5f5;
    }
    
    &:hover {
      background: #fafafa;
    }
    
    .menu-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      .el-icon {
        font-size: 20px;
        color: #fff;
      }
    }
    
    .menu-text {
      flex: 1;
      font-size: 15px;
    }
    
    .menu-extra {
      font-size: 13px;
      color: #999;
      margin-right: 4px;
    }
    
    > .el-icon {
      color: #ccc;
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

.about-content {
  text-align: center;
  padding: 20px;
  
  .app-logo {
    margin-bottom: 16px;
    
    img {
      width: 80px;
      height: 80px;
      border-radius: 16px;
    }
  }
  
  h3 {
    margin: 0 0 8px;
    font-size: 18px;
  }
  
  p {
    color: #666;
    margin: 4px 0;
    font-size: 14px;
  }
  
  .desc {
    margin-top: 16px;
    line-height: 1.6;
  }
}
</style>
