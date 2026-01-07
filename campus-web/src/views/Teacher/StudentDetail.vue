<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDemandDetail } from '@/api/demand'

const router = useRouter()
const route = useRoute()
const loading = ref(false)

// 需求详情数据
const demand = ref({
  id: route.params.id,
  name: '王女士',
  subject: '小学三年级 · 数学',
  price: 180,
  frequency: '每周2次',
  location: '阳光花园 (距您1.2km)',
  desc: '孩子目前三年级，计算基础比较薄弱，做题粗心。希望找一位有耐心的大学生老师，最好是理科专业的，能带孩子整理错题。',
  tags: ['需要耐心', '基础巩固', '提供零食'],
  idVerified: true,
  publishTime: '2024-01-15 10:30',
  teachMode: '线下',
  gender: '不限',
  studentAge: 9
})

// 获取需求详情
const fetchDemandDetail = async () => {
  loading.value = true
  try {
    const res = await getDemandDetail(route.params.id)
    if (res.data) {
      demand.value = {
        ...demand.value,
        ...res.data,
        name: res.data.parentName || demand.value.name,
        subject: `${res.data.gradeLevel || '小学'}${res.data.gradeName || '三年级'} · ${res.data.subjects || '数学'}`,
        price: res.data.maxPrice || demand.value.price,
        desc: res.data.description || demand.value.desc,
        location: res.data.teachLocation || demand.value.location
      }
    }
  } catch (error) {
    console.error('获取需求详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 联系家长
const handleContact = () => {
  ElMessage.success('沟通请求已发送！请等待家长回复')
  // 实际应该调用后端API发送沟通请求
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

    <!-- 底部操作栏 -->
    <div class="bottom-action">
      <el-button type="primary" size="large" @click="handleContact">
        <el-icon class="mr-1"><Promotion /></el-icon>
        立即沟通
      </el-button>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.student-detail-page {
  min-height: 100vh;
  background: $bg-light;
  padding-bottom: 100px;
}

.page-header {
  background: #fff;
  padding: $spacing-md $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid $border-color;

  .page-title {
    font-size: 18px;
    font-weight: 600;
  }
}

.info-card {
  margin: $spacing-lg;
  border-radius: 16px;

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
  margin: 0 $spacing-lg $spacing-lg;
  border-radius: 16px;

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
  margin: 0 $spacing-lg;
  border-radius: 16px;

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

.bottom-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: $spacing-md $spacing-lg;
  border-top: 1px solid $border-color;

  .el-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 12px;
  }
}
</style>
