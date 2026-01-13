<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDemandDetail } from '@/api/demand'
import { acceptDemand } from '@/api/order'
import { useUserStore } from '@/stores/index'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const accepting = ref(false)

// 需求详情数据
const demand = ref({
  id: route.params.id,
  publisherId: null, // 新增 publisherId
  name: '家长', // 默认显示
  subject: '',
  price: 0,
  frequency: '',
  location: '',
  desc: '',
  tags: [],
  idVerified: false,
  publishTime: '',
  teachMode: '面授',
  gender: '不限',
  studentAge: 0,
  status: 1 // 需求状态
})

// 获取需求详情
const fetchDemandDetail = async () => {
  loading.value = true
  try {
    const res = await getDemandDetail(route.params.id)
    if (res.data) {
      const data = res.data
      demand.value = {
        ...demand.value,
        ...data,
        publisherId: data.publisherId, // 映射 publisherId
        name: data.contactName || '家长', // 后端可能没有直接返回家长姓名，暂时用默认或 contactName
        subject: `${data.grade || ''} · ${data.subject || ''}`,
        price: data.expectPrice || 0,
        desc: data.detail || '',
        location: data.address || '',
        idVerified: true, // 假设能发布需求的都是验证过的
        teachMode: data.teachMode === 1 ? '线下上门' : (data.teachMode === 2 ? '在线网课' : '不限'),
        status: data.status
      }
    }
  } catch (error) {
    console.error('获取需求详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 判断是否是自己发布的需求
const isSelf = computed(() => {
  return userStore.userId && demand.value.publisherId === userStore.userId
})

// 判断是否是教师角色
const isTutor = computed(() => userStore.isTutor)

// 判断需求是否可接单（上架中且未被接单）
const canAccept = computed(() => {
  return demand.value.status === 1 && !isSelf.value && isTutor.value
})

// 联系家长
const handleContact = () => {
  if (isSelf.value) {
    ElMessage.warning('不能联系自己发布的需求')
    return
  }
  if (!demand.value.publisherId) {
    ElMessage.warning('无法获取家长信息')
    return
  }
  router.push(`/chat/${demand.value.publisherId}`)
}

// 立即接单
const handleAccept = async () => {
  try {
    await ElMessageBox.confirm(
      '确认接单后，将创建待确认订单，等待家长确认后可进行支付。',
      '确认接单',
      { confirmButtonText: '确认接单', cancelButtonText: '取消', type: 'info' }
    )
    
    accepting.value = true
    const res = await acceptDemand({ demandId: Number(route.params.id) })
    if (res.code === 200) {
      ElMessage.success('接单成功，等待家长确认')
      // 刷新需求状态
      fetchDemandDetail()
    } else {
      ElMessage.error(res.message || '接单失败')
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('接单失败:', err)
      ElMessage.error(err.message || '接单失败')
    }
  } finally {
    accepting.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
}

onMounted(() => {
  fetchDemandDetail()
})
</script>

<template>
  <div class="student-detail-page" v-loading="loading">
    <div class="detail-wrapper">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button text @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h1 class="page-title">需求详情</h1>
      <el-button text>
        <el-icon><ChatDotRound /></el-icon>
      </el-button>
    </div>

    <!-- 基本信息卡片 -->
    <el-card class="info-card" shadow="never">
      <div class="card-header">
        <div class="left-section">
          <h2 class="subject-title">{{ demand.subject }}</h2>
          <div class="parent-info">
            <span class="parent-name">{{ demand.name }}</span>
            <el-tag v-if="demand.idVerified" type="success" size="small" effect="plain">
              <el-icon class="mr-1"><ShieldCheck /></el-icon>
              已实名
            </el-tag>
          </div>
        </div>
        <div class="right-section">
          <div class="price">¥{{ demand.price }}</div>
          <div class="price-unit">/小时</div>
        </div>
      </div>

      <el-divider />

      <div class="tags-section">
        <el-tag 
          v-for="tag in demand.tags" 
          :key="tag"
          effect="plain"
          size="small"
        >
          {{ tag }}
        </el-tag>
      </div>
    </el-card>

    <!-- 详细信息卡片 -->
    <el-card class="detail-card" shadow="never">
      <div class="detail-item">
        <div class="item-icon time">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="item-content">
          <h3 class="item-title">授课时间</h3>
          <p class="item-value">{{ demand.frequency }}</p>
        </div>
      </div>

      <div class="detail-item">
        <div class="item-icon location">
          <el-icon><Location /></el-icon>
        </div>
        <div class="item-content">
          <h3 class="item-title">授课地点</h3>
          <p class="item-value">{{ demand.location }}</p>
        </div>
      </div>

      <div class="detail-item">
        <div class="item-icon mode">
          <el-icon><Monitor /></el-icon>
        </div>
        <div class="item-content">
          <h3 class="item-title">授课方式</h3>
          <p class="item-value">{{ demand.teachMode }}</p>
        </div>
      </div>

      <div class="detail-item">
        <div class="item-icon gender">
          <el-icon><User /></el-icon>
        </div>
        <div class="item-content">
          <h3 class="item-title">教师性别要求</h3>
          <p class="item-value">{{ demand.gender }}</p>
        </div>
      </div>
    </el-card>

    <!-- 家长描述 -->
    <el-card class="desc-card" shadow="never">
      <template #header>
        <div class="card-title">家长描述</div>
      </template>
      <p class="desc-content">{{ demand.desc }}</p>
    </el-card>

    <!-- 操作区域 -->
    <div class="action-section">
      <el-button size="large" @click="handleContact" :disabled="isSelf">
        <el-icon class="mr-1"><ChatDotRound /></el-icon>
        联系家长
      </el-button>
      <el-button 
        v-if="canAccept" 
        type="primary" 
        size="large" 
        @click="handleAccept"
        :loading="accepting"
      >
        <el-icon class="mr-1"><Check /></el-icon>
        立即接单
      </el-button>
      <el-button v-else-if="demand.status === 2" type="info" size="large" disabled>
        已被接单
      </el-button>
    </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.student-detail-page {
  min-height: calc(100vh - 114px);
  background: $bg-light;
  padding: $spacing-xl 0;
}

.detail-wrapper {
  max-width: 700px;
  margin: 0 auto;
  padding: 0 $spacing-lg;
}

.page-header {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: $radius-lg;
  margin-bottom: $spacing-lg;
  box-shadow: $shadow-sm;

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }
}

.info-card {
  margin-bottom: $spacing-lg;
  border-radius: 16px;
  box-shadow: $shadow-sm;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;

    .left-section {
      .subject-title {
        font-size: 20px;
        font-weight: 700;
        color: $text-primary;
        margin-bottom: $spacing-sm;
      }

      .parent-info {
        display: flex;
        align-items: center;
        gap: $spacing-sm;

        .parent-name {
          font-size: 14px;
          font-weight: 600;
          color: $text-secondary;
        }
      }
    }

    .right-section {
      text-align: right;

      .price {
        font-size: 28px;
        font-weight: 700;
        color: $warning-color;
      }

      .price-unit {
        font-size: 12px;
        color: $text-muted;
      }
    }
  }

  .tags-section {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
  }
}

.detail-card {
  margin-bottom: $spacing-lg;
  border-radius: 16px;
  box-shadow: $shadow-sm;

  .detail-item {
    display: flex;
    align-items: flex-start;
    gap: $spacing-md;
    padding: $spacing-md 0;

    &:not(:last-child) {
      border-bottom: 1px solid $border-color;
    }

    .item-icon {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      &.time {
        background: rgba($primary-color, 0.1);
        color: $primary-color;
      }

      &.location {
        background: rgba($warning-color, 0.1);
        color: $warning-color;
      }

      &.mode {
        background: rgba($success-color, 0.1);
        color: $success-color;
      }

      &.gender {
        background: rgba(#9333ea, 0.1);
        color: #9333ea;
      }
    }

    .item-content {
      .item-title {
        font-size: 14px;
        font-weight: 600;
        color: $text-primary;
      }

      .item-value {
        font-size: 14px;
        color: $text-muted;
        margin-top: 2px;
      }
    }
  }
}

.desc-card {
  margin-bottom: $spacing-lg;
  border-radius: 16px;
  box-shadow: $shadow-sm;

  .card-title {
    font-weight: 600;
    color: $text-primary;
  }

  .desc-content {
    font-size: 14px;
    color: $text-secondary;
    line-height: 1.8;
  }
}

.action-section {
  display: flex;
  gap: $spacing-md;
  margin-top: $spacing-lg;

  .el-button {
    flex: 1;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
  }
}

@media (max-width: 768px) {
  .detail-wrapper {
    padding: 0 $spacing-md;
  }

  .action-section {
    flex-direction: column;
  }
}
</style>
