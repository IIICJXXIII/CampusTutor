<template>
  <div class="mine-page">
    <div class="page-header">
      <h1 class="page-title">我的</h1>
    </div>
    
    <!-- 用户信息卡片 -->
    <div class="user-card" @click="goToResume">
      <el-avatar :size="64" :src="userStore.avatar">
        {{ userStore.nickname?.charAt(0) }}
      </el-avatar>
      <div class="user-info">
        <h2>{{ userStore.nickname || '用户' }}</h2>
        <div class="user-tags">
          <el-tag v-if="tutorStore.certStatus === 2" type="success" size="small">已认证</el-tag>
          <el-tag v-else-if="tutorStore.certStatus === 1" type="warning" size="small">审核中</el-tag>
          <el-tag v-else size="small">未认证</el-tag>
        </div>
      </div>
      <el-icon><ArrowRight /></el-icon>
    </div>
    
    <!-- 数据统计 -->
    <div class="stats-card">
      <div class="stat-item" @click="goToOrders('completed')">
        <span class="value">{{ stats.completedOrders || 0 }}</span>
        <span class="label">完成订单</span>
      </div>
      <div class="stat-item" @click="goToLessons">
        <span class="value">{{ stats.totalHours || 0 }}</span>
        <span class="label">授课时长</span>
      </div>
      <div class="stat-item" @click="goToWallet">
        <span class="value">¥{{ (stats.totalIncome || 0).toFixed(0) }}</span>
        <span class="label">累计收入</span>
      </div>
      <div class="stat-item">
        <span class="value">{{ stats.rating?.toFixed(1) || '5.0' }}</span>
        <span class="label">综合评分</span>
      </div>
    </div>
    
    <!-- 功能菜单 -->
    <div class="menu-section">
      <div class="menu-group">
        <div class="menu-item" @click="goToResume">
          <el-icon><User /></el-icon>
          <span>我的简历</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="goToAuth">
          <el-icon><Document /></el-icon>
          <span>资质认证</span>
          <el-tag v-if="tutorStore.certStatus === 0" type="warning" size="small">待认证</el-tag>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="goToSchedule">
          <el-icon><Calendar /></el-icon>
          <span>排课设置</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <div class="menu-group">
        <div class="menu-item" @click="goToWallet">
          <el-icon><Wallet /></el-icon>
          <span>我的钱包</span>
          <span class="value">¥{{ walletStore.balance?.toFixed(2) || '0.00' }}</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="goToOrders('all')">
          <el-icon><List /></el-icon>
          <span>我的订单</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="goToLessons">
          <el-icon><Reading /></el-icon>
          <span>课程记录</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
      
      <div class="menu-group">
        <div class="menu-item" @click="goToSettings">
          <el-icon><Setting /></el-icon>
          <span>设置</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="goToHelp">
          <el-icon><QuestionFilled /></el-icon>
          <span>帮助与反馈</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="goToAbout">
          <el-icon><InfoFilled /></el-icon>
          <span>关于我们</span>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  ArrowRight, User, Document, Calendar, Wallet, List,
  Reading, Setting, QuestionFilled, InfoFilled
} from '@element-plus/icons-vue'
import { useUserStore, useTutorStore, useWalletStore } from '@shared/stores'
import { getTutorProfile } from '@shared/api/tutor'
import { getWalletInfo } from '@shared/api/wallet'

const router = useRouter()
const userStore = useUserStore()
const tutorStore = useTutorStore()
const walletStore = useWalletStore()

const stats = ref({
  completedOrders: 0,
  totalHours: 0,
  totalIncome: 0,
  rating: 5.0
})

const loadStats = async () => {
  try {
    // 加载教员信息
    const profileRes = await getTutorProfile()
    if (profileRes.code === 200) {
      const data = profileRes.data || {}
      tutorStore.setProfile(data)
      tutorStore.setCertStatus(data.certStatus || 0)
      stats.value = {
        completedOrders: data.completedOrders || 0,
        totalHours: data.totalHours || 0,
        totalIncome: data.totalIncome || 0,
        rating: data.rating || 5.0
      }
    }
    
    // 加载钱包信息
    const walletRes = await getWalletInfo()
    if (walletRes.code === 200) {
      walletStore.setWalletInfo(walletRes.data)
    }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

const goToResume = () => router.push('/resume')
const goToAuth = () => router.push('/auth')
const goToSchedule = () => router.push('/schedule')
const goToWallet = () => router.push('/wallet')
const goToOrders = (status) => router.push(`/orders?status=${status}`)
const goToLessons = () => router.push('/lessons')
const goToSettings = () => router.push('/settings')
const goToHelp = () => ElMessage.info('帮助中心开发中')
const goToAbout = () => ElMessage.info('关于我们页面开发中')

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      type: 'warning'
    })
    
    userStore.logout()
    router.push('/login')
  } catch {
    // 取消
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style lang="scss" scoped>
.mine-page {
  padding-bottom: 80px;
  
  .user-card {
    display: flex;
    align-items: center;
    gap: 16px;
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 16px;
    cursor: pointer;
    color: #fff;
    
    .user-info {
      flex: 1;
      
      h2 {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 8px;
      }
      
      .user-tags {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .stats-card {
    display: flex;
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    
    .stat-item {
      flex: 1;
      text-align: center;
      cursor: pointer;
      
      &:not(:last-child) {
        border-right: 1px solid #ebeef5;
      }
      
      .value {
        display: block;
        font-size: 20px;
        font-weight: 700;
        color: #409eff;
        margin-bottom: 4px;
      }
      
      .label {
        font-size: 12px;
        color: #909399;
      }
    }
  }
  
  .menu-section {
    .menu-group {
      background: #fff;
      border-radius: 12px;
      margin-bottom: 16px;
      overflow: hidden;
      
      .menu-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px 20px;
        cursor: pointer;
        transition: background-color 0.2s;
        
        &:not(:last-child) {
          border-bottom: 1px solid #ebeef5;
        }
        
        &:hover {
          background-color: #f5f7fa;
        }
        
        .el-icon:first-child {
          font-size: 20px;
          color: #409eff;
        }
        
        span:not(.value) {
          flex: 1;
          font-size: 15px;
        }
        
        .value {
          font-size: 14px;
          color: #409eff;
          font-weight: 500;
        }
        
        .el-icon:last-child {
          color: #c0c4cc;
        }
      }
    }
  }
  
  .logout-section {
    padding: 24px;
    
    .el-button {
      width: 100%;
    }
  }
}

// 响应式
@media (max-width: 576px) {
  .mine-page {
    .stats-card {
      flex-wrap: wrap;
      
      .stat-item {
        width: 50%;
        padding: 12px 0;
        
        &:nth-child(1), &:nth-child(2) {
          border-bottom: 1px solid #ebeef5;
        }
        
        &:nth-child(2) {
          border-right: none;
        }
      }
    }
  }
}
</style>
