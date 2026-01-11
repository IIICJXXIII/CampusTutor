<template>
  <div class="order-list-page">
    <div class="page-header">
      <h2 class="page-title">我的订单</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待支付" name="unpaid" />
        <el-tab-pane label="进行中" name="active" />
        <el-tab-pane label="已完成" name="completed" />
      </el-tabs>
    </div>

    <div class="order-container" v-loading="loading">
      <el-empty v-if="filteredOrders.length === 0 && !loading" description="暂无相关订单" />

      <div v-for="order in filteredOrders" :key="order.id" class="order-card" @click="goDetail(order)">
        <div class="card-header">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <el-tag :type="order.statusTagType" effect="light" round>
            {{ order.statusText }}
          </el-tag>
        </div>

        <div class="card-body">
          <div class="info-row">
            <div class="info-item main">
              <span class="label">{{ isParent ? '授课教师' : '学生家长' }}：</span>
              <span class="value highlight">{{ isParent ? order.tutorName : order.parentName }}</span>
            </div>
            <div class="info-item price">
              <span class="currency">¥</span>
              <span class="amount">{{ order.totalAmount }}</span>
            </div>
          </div>
          
          <div class="info-grid">
            <div class="grid-item">
              <el-icon><Reading /></el-icon>
              <span>{{ order.subject }} · {{ order.grade }}</span>
            </div>
            <div class="grid-item">
              <el-icon><Timer /></el-icon>
              <span>{{ order.totalHours }} 课时</span>
            </div>
            <div class="grid-item">
              <el-icon><LocationInformation /></el-icon>
              <span>{{ order.teachMode === 1 ? '上门授课' : '在线授课' }}</span>
            </div>
            <div class="grid-item progress" v-if="order.status >= 1 && order.status <= 3">
              <el-icon><PieChart /></el-icon>
              <span>进度：{{ order.usedHours || 0 }} / {{ order.totalHours }}</span>
            </div>
          </div>
        </div>

        <div class="card-footer" @click.stop>
          
          <template v-if="isParent">
            <div v-if="order.status === -1" class="btn-group">
              <el-button @click="handleCancel(order)">拒绝</el-button>
              <el-button type="primary" @click="handleConfirmContract(order)">确认并支付</el-button>
            </div>
            <div v-else-if="order.status === 0" class="btn-group">
              <el-button @click="handleCancel(order)">取消</el-button>
              <el-button type="danger" @click="handlePay(order)">去支付</el-button>
            </div>

            <div v-else-if="order.status === 1" class="btn-group">
              <el-button @click="goChat(order.tutorId)">联系老师</el-button>
              <el-tag type="warning">待老师确认开课</el-tag>
            </div>
            <div v-else-if="order.status === 2" class="btn-group">
              <el-button @click="goChat(order.tutorId)">联系老师</el-button>
              <el-button type="primary" plain @click="goRecord(order.id)">
                确认课时
              </el-button>
            </div>

            <div v-else class="btn-group">
              <el-button @click="goRecord(order.id)">查看记录</el-button>
            </div>
          </template>

          <template v-else>
            <div v-if="order.status <= 0" class="btn-group">
              <span class="status-tip">等待家长操作...</span>
              <el-button type="text" @click="goChat(order.parentId)">催一下</el-button>
            </div>

            <div v-else-if="order.status === 1" class="btn-group">
              <el-button @click="goChat(order.parentId)">联系家长</el-button>
              <el-button type="primary" @click="handleStartClass(order)">确认开课/打卡</el-button>
            </div>
            <div v-else-if="order.status === 2" class="btn-group">
              <el-button @click="goChat(order.parentId)">联系家长</el-button>
              <el-button type="primary" @click="handleCheckIn(order)">立即打卡</el-button>
            </div>

            <div v-else class="btn-group">
              <el-button @click="goRecord(order.id)">查看记录</el-button>
            </div>
          </template>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores'
import { getParentOrders, getTutorOrders, cancelOrder } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('all') // 默认显示全部
const loading = ref(false)
const allOrders = ref([]) // 存储所有订单

const isParent = computed(() => userStore.isParent)

// 状态字典
const statusMap = {
  '-1': { text: '待确认', type: 'info' },
  '0':  { text: '待支付', type: 'danger' },
  '1':  { text: '已支付/待开课', type: 'warning' }, // 关键修正：状态1
  '2':  { text: '进行中', type: 'primary' },       // 关键修正：状态2
  '3':  { text: '已完成', type: 'success' },
  '4':  { text: '已取消', type: 'info' },
  '5':  { text: '退款中', type: 'warning' },
  '6':  { text: '已退款', type: 'info' }
}

// 核心修正：前端筛选逻辑
const filteredOrders = computed(() => {
  const list = allOrders.value
  const tab = activeTab.value
  
  if (tab === 'all') return list
  
  return list.filter(order => {
    const s = order.status
    if (tab === 'unpaid') {
      // 待支付栏：包含 -1(待确认) 和 0(待支付)
      return s === -1 || s === 0
    }
    if (tab === 'active') {
      // 进行中栏：包含 1(已支付) 和 2(进行中)
      // 支付成功后的订单(状态1)会立即出现在这里
      return s === 1 || s === 2
    }
    if (tab === 'completed') {
      // 已完成栏：包含 3(完成) 和 取消/退款类
      return s >= 3
    }
    return true
  })
})

const fetchOrders = async () => {
  loading.value = true
  try {
    // 获取全部订单，不做后端状态筛选，在前端处理
    const params = { page: 1, size: 100 } 
    const apiFunc = isParent.value ? getParentOrders : getTutorOrders
    const res = await apiFunc(params)
    
    const list = res.data?.records || []
    allOrders.value = list.map(item => {
      const statusInfo = statusMap[item.status] || { text: '未知', type: 'info' }
      return {
        ...item,
        statusText: statusInfo.text,
        statusTagType: statusInfo.type
      }
    })
  } catch (error) {
    console.error(error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// ========== 业务操作 ==========

const handleConfirmContract = (order) => {
  // 确认后跳转支付
  router.push({ path: '/payment', query: { orderId: order.id } })
}

const handlePay = (order) => {
  router.push({ path: '/payment', query: { orderId: order.id } })
}

// 教员：确认开课（实际上也是去打卡页，或者调用一个 start 接口）
const handleStartClass = (order) => {
  // 简单处理：直接引导去打卡，第一次打卡即视为正式开课
  router.push({
    path: '/process/record',
    query: { orderId: order.id, action: 'checkin' }
  })
}

const handleCheckIn = (order) => {
  router.push({
    path: '/process/record',
    query: { orderId: order.id, action: 'checkin' }
  })
}

const goRecord = (orderId) => {
  router.push({ path: '/process/record', query: { orderId } })
}

const goChat = (targetUserId) => {
  router.push(`/chat/${targetUserId}`)
}

const handleCancel = (order) => {
  ElMessageBox.prompt('请输入取消/拒绝原因', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(async ({ value }) => {
    try {
      await cancelOrder(order.id, value)
      ElMessage.success('操作成功')
      fetchOrders() // 刷新列表
    } catch (e) {}
  })
}

const goDetail = (order) => {
  // 可选：跳转详情页
}

onMounted(() => {
  fetchOrders()
})
</script>

<style lang="scss" scoped>
.order-list-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  .page-title {
    font-size: 20px;
    font-weight: 600;
    margin-bottom: 16px;
  }
}

.order-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  border: 1px solid transparent;
  cursor: pointer;

  &:hover {
    border-color: #409eff;
    transform: translateY(-2px);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f2f5;
    margin-bottom: 12px;

    .order-no {
      font-size: 13px;
      color: #909399;
    }
  }

  .card-body {
    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .label { color: #606266; font-size: 14px; }
      .highlight { color: #303133; font-weight: 600; font-size: 15px; }
      .price {
        color: #f56c6c; font-weight: 700;
        .currency { font-size: 14px; }
        .amount { font-size: 20px; }
      }
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 10px;
      background: #f9fafc;
      padding: 12px;
      border-radius: 8px;

      .grid-item {
        display: flex;
        align-items: center;
        font-size: 13px;
        color: #606266;
        .el-icon { margin-right: 6px; font-size: 14px; color: #909399; }
        
        &.progress {
          color: #409eff;
          grid-column: span 2;
        }
      }
    }
  }

  .card-footer {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    border-top: 1px dashed #e4e7ed;
    padding-top: 12px;
    margin-top: 12px;

    .status-tip {
      font-size: 13px;
      color: #909399;
      margin-right: 12px;
    }

    .btn-group {
      display: flex;
      gap: 12px;
      align-items: center;
    }
  }
}
</style>