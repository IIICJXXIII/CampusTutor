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
          <el-descriptions-item label="上课频率">{{ scheduleInfo.frequency || '面议' }}</el-descriptions-item>
          <el-descriptions-item label="每节课时长">{{ scheduleInfo.duration ? scheduleInfo.duration + '小时' : '面议' }}</el-descriptions-item>
          <el-descriptions-item label="可用时间" :span="2">
            <template v-if="scheduleInfo.availableTime?.length">
              <el-tag v-for="t in scheduleInfo.availableTime" :key="t" size="small" class="time-tag">{{ t }}</el-tag>
            </template>
            <span v-else>面议</span>
          </el-descriptions-item>
          <el-descriptions-item label="上课地址" :span="2">
            <el-icon><Location /></el-icon>
            {{ demand.address || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="技能水平">{{ demand.skillLevel || '不限' }}</el-descriptions-item>
          <el-descriptions-item label="授课方式">{{ getTeachModeText(demand.teachMode) }}</el-descriptions-item>
          <el-descriptions-item v-if="scheduleInfo.genderRequirement" label="性别要求">
            {{ scheduleInfo.genderRequirement }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="section">
          <h3>需求描述</h3>
          <p class="description">{{ demand.detail || '暂无详细描述' }}</p>
        </div>
      </div>
      
      <div class="parent-card">
        <h3>家长信息</h3>
        <div class="parent-info">
          <el-avatar :size="48" :src="parentInfo.avatar">
            {{ parentInfo.nickname?.charAt(0) || '家' }}
          </el-avatar>
          <div class="info">
            <p class="name">{{ parentInfo.nickname || '家长' }}</p>
            <p class="desc">发布于 {{ formatDate(demand.createTime) }}</p>
          </div>
          <el-button @click="goToChat">
            <el-icon><ChatDotRound /></el-icon>私聊
          </el-button>
        </div>
      </div>
      
      <div class="action-bar">
        <el-button size="large" @click="goBack">返回</el-button>
        <el-button 
          v-if="demand.status === 1"
          type="primary" 
          size="large" 
          :loading="applying"
          @click="handleApply"
        >
          申请接单
        </el-button>
        <el-tag v-else-if="demand.status === 2" type="warning" size="large">
          该需求已被其他教师接单
        </el-tag>
      </div>
    </div>

    <el-dialog v-model="showApplyDialog" title="申请接单" width="480px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="计划课时">
          <el-input-number v-model="applyForm.totalHours" :min="1" :max="100" />
          <span style="margin-left: 8px; color: #909399;">课时</span>
        </el-form-item>
        <el-form-item label="申请备注">
          <el-input
            v-model="applyForm.remark"
            type="textarea"
            :rows="3"
            placeholder="向家长介绍您的教学优势和计划"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, ChatDotRound } from '@element-plus/icons-vue'
import { getDemandDetail, applyForDemand } from '@shared/api/demand'
import { getDemandDetail } from '@shared/api/demand'
import { acceptOrder } from '@shared/api/order'
import { getUserById } from '@shared/api/user'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const applying = ref(false)
const demand = ref({})
const showApplyDialog = ref(false)
const applyForm = reactive({
  totalHours: 10,
  remark: ''
})
const parentInfo = ref({ nickname: '', avatar: '' })

const scheduleInfo = computed(() => {
  const raw = demand.value.scheduleRequire
  if (!raw) return {}
  try {
    if (typeof raw === 'string') return JSON.parse(raw)
    return raw
  } catch (e) {
    return { frequency: raw }
  }
})

const getTeachModeText = (mode) => {
  if (mode === 1) return '线下上门'
  if (mode === 2) return '线上授课'
  return '线上线下均可'
}

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'warning', 3: '' }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = { 0: '已下架', 1: '招募中', 2: '已匹配', 3: '已关闭' }
  const map = { 0: '草稿', 1: '已上架', 2: '已下架', 3: '已完成' }
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
      if (res.data.publisherId) {
        loadParentInfo(res.data.publisherId)
      }
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadParentInfo = async (publisherId) => {
  try {
    const res = await getUserById(publisherId)
    if (res.code === 200 && res.data) {
      parentInfo.value = {
        nickname: res.data.nickname || '家长',
        avatar: res.data.avatarUrl || res.data.avatar || ''
      }
    }
  } catch (e) {
    // silent
  }
}

const goBack = () => router.back()

const goToChat = () => {
  if (demand.value.publisherId) {
    router.push(`/chat/${demand.value.publisherId}`)
  }
}

const handleApply = () => {
  showApplyDialog.value = true
}

const submitApply = async () => {
  applying.value = true
  try {
    const res = await applyForDemand(demand.value.id, {
      totalHours: applyForm.totalHours,
      remark: applyForm.remark
    await ElMessageBox.confirm(
      '确定要接单吗？接单后请等待家长确认。',
      '确认接单',
      { confirmButtonText: '确定', cancelButtonText: '取消' }
    )
    
    accepting.value = true
    const res = await acceptOrder({
      demandId: parseInt(route.params.id),
      totalHours: 10,
      remark: ''
    })
    if (res.code === 200) {
      ElMessage.success('申请已提交，请等待家长审核')
      showApplyDialog.value = false
      router.push('/teacher/orders')
    }
  } catch (error) {
    ElMessage.error(error.message || '申请失败')
  } finally {
    applying.value = false
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
    }
  }

  .time-tag {
    margin-right: 6px;
    margin-bottom: 4px;
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
    align-items: center;
    
    .el-button {
      flex: 1;
    }
  }
}
</style>
