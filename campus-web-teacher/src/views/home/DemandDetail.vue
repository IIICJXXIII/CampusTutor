<template>
  <div class="demand-detail">
    <el-page-header @back="goBack">
      <template #content>
        <span>需求详情</span>
      </template>
    </el-page-header>
    
    <div v-loading="loading" class="detail-content">
      <div class="main-card">
        <div class="card-header">
          <div class="title-row">
            <h1>{{ demand.title || `${demand.grade}${demand.subject}辅导` }}</h1>
            <el-tag :type="getStatusType(demand.status)">{{ getStatusText(demand.status) }}</el-tag>
          </div>
          <div class="price-row">
            <span class="price">¥{{ demand.expectPrice }}</span>
            <span class="unit">/小时</span>
          </div>
        </div>
        
        <el-divider />
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="科目">{{ demand.subject }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ demand.grade }}</el-descriptions-item>
          <el-descriptions-item label="上课频率" :span="2">{{ demand.scheduleRequire || '面议' }}</el-descriptions-item>
          <el-descriptions-item label="上课地址" :span="2">
            <el-icon><Location /></el-icon>
            {{ demand.address }}
          </el-descriptions-item>
          <el-descriptions-item label="技能水平">{{ demand.skillLevel || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="授课方式">{{ demand.teachMode === 1 ? '线上' : demand.teachMode === 2 ? '线下' : '均可' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="section">
          <h3>需求描述</h3>
          <p class="description">{{ demand.detail || '暂无详细描述' }}</p>
        </div>
        
        <div class="section">
          <h3>学生情况</h3>
          <p class="description">{{ demand.studentInfo || '暂无学生情况说明' }}</p>
        </div>
        
        <div class="section">
          <h3>对教员要求</h3>
          <div class="requirements">
            <el-tag v-for="req in demand.requirements" :key="req" class="req-tag">{{ req }}</el-tag>
            <span v-if="!demand.requirements?.length" class="no-data">暂无特殊要求</span>
          </div>
        </div>
      </div>
      
      <!-- 家长信息 -->
      <div class="parent-card">
        <h3>家长信息</h3>
        <div class="parent-info">
          <el-avatar :size="48" :src="demand.parentAvatar" />
          <div class="info">
            <p class="name">{{ demand.parentName || '家长' }}</p>
            <p class="desc">发布于 {{ formatDate(demand.createTime) }}</p>
          </div>
          <el-button @click="goToChat">
            <el-icon><ChatDotRound /></el-icon>私聊
          </el-button>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button size="large" @click="goBack">返回</el-button>
        <el-button 
          type="primary" 
          size="large" 
          :loading="accepting"
          @click="handleAccept"
        >
          立即接单
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, ChatDotRound } from '@element-plus/icons-vue'
import { getDemandDetail } from '@shared/api/demand'
import { acceptOrder } from '@shared/api/order'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const accepting = ref(false)
const demand = ref({})

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: '' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 0: '待发布', 1: '招募中', 2: '已匹配', 3: '已关闭' }
  return map[status] || '未知'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await getDemandDetail(route.params.id)
    if (res.code === 200) {
      demand.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const goToChat = () => {
  if (demand.value.publisherId) {
    router.push(`/chat/${demand.value.publisherId}`)
  }
}

const handleAccept = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要接单吗？接单后请等待家长确认。',
      '确认接单',
      { confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    
    accepting.value = true
    const res = await acceptOrder(demand.value.orderId || demand.value.id)
    if (res.code === 200) {
      ElMessage.success('接单成功！等待家长确认')
      router.push('/orders')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '接单失败')
    }
  } finally {
    accepting.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style lang="scss" scoped>
.demand-detail {
  max-width: 800px;
  margin: 0 auto;
  
  .detail-content {
    margin-top: 24px;
  }
  
  .main-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    .card-header {
      .title-row {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 12px;
        
        h1 {
          font-size: 20px;
          font-weight: 600;
        }
      }
      
      .price-row {
        .price {
          font-size: 32px;
          font-weight: 700;
          color: #f56c6c;
        }
        
        .unit {
          font-size: 14px;
          color: #909399;
        }
      }
    }
    
    .section {
      margin-top: 24px;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 12px;
        color: #303133;
      }
      
      .description {
        font-size: 14px;
        color: #606266;
        line-height: 1.8;
      }
      
      .requirements {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        
        .req-tag {
          margin: 0;
        }
        
        .no-data {
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }
  
  .parent-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 16px;
    }
    
    .parent-info {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .info {
        flex: 1;
        
        .name {
          font-size: 16px;
          font-weight: 500;
        }
        
        .desc {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }
  
  .action-bar {
    display: flex;
    gap: 16px;
    
    .el-button {
      flex: 1;
    }
  }
}
</style>
