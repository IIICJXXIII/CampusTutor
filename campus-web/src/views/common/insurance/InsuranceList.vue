<template>
  <div class="insurance-page">
    <van-nav-bar title="保险单" left-arrow @click-left="$router.back()" />

    <div class="filter-bar">
      <el-radio-group v-model="status" size="small" @change="loadPolicies">
        <el-radio-button :value="null">全部</el-radio-button>
        <el-radio-button :value="1">生效中</el-radio-button>
        <el-radio-button :value="2">已过期</el-radio-button>
      </el-radio-group>
    </div>

    <div class="policy-list" v-loading="loading">
      <el-empty v-if="!loading && policies.length === 0" description="暂无保险单" />

      <div v-for="policy in policies" :key="policy.id" class="policy-card">
        <div class="policy-header">
          <span class="policy-no">{{ policy.policyNo }}</span>
          <el-tag :type="policy.status === 1 ? 'success' : 'info'" size="small">
            {{ policy.status === 1 ? '生效中' : '已过期' }}
          </el-tag>
        </div>
        <div class="policy-body">
          <div class="info-row">
            <span class="label">保险公司</span>
            <span class="value">{{ policy.provider || '平安保险' }}</span>
          </div>
          <div class="info-row" v-if="policy.orderNo">
            <span class="label">关联订单</span>
            <span class="value">{{ policy.orderNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">创建时间</span>
            <span class="value">{{ policy.createTime }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getInsurancePolicies } from '@shared/api/insurance'

const loading = ref(false)
const policies = ref([])
const status = ref(null)

onMounted(() => {
  loadPolicies()
})

const loadPolicies = async () => {
  loading.value = true
  try {
    const res = await getInsurancePolicies({ status: status.value, page: 1, size: 20 })
    if (res.code === 200) {
      policies.value = res.data?.records || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.insurance-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.filter-bar {
  padding: 12px 16px;
  background: #fff;
}

.policy-list {
  padding: 12px 16px;
}

.policy-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.policy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .policy-no {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }
}

.policy-body {
  .info-row {
    display: flex;
    justify-content: space-between;
    padding: 6px 0;
    font-size: 14px;

    .label { color: #909399; }
    .value { color: #303133; }
  }
}
</style>
