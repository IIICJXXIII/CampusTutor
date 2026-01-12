<template>
  <div class="order-list">
    <div class="page-header">
      <h1 class="page-title">我的订单</h1>
    </div>
    
    <!-- 状态标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="待确认" name="pending" />
      <el-tab-pane label="进行中" name="ongoing" />
      <el-tab-pane label="已完成" name="completed" />
      <el-tab-pane label="已取消" name="cancelled" />
    </el-tabs>
    
    <!-- 订单列表 -->
    <div v-loading="loading" class="order-container">
      <div v-if="orders.length" class="order-cards">
        <div 
          v-for="order in orders" 
          :key="order.id" 
          class="order-card"
          @click="viewDetail(order.id)"
        >
          <div class="order-header">
            <span class="order-no">订单号: {{ order.orderNo }}</span>
            <el-tag :type="getStatusType(order.status)" size="small">
              {{ getStatusText(order.status) }}
            </el-tag>
          </div>
          
          <div class="order-body">
            <div class="student-info">
              <el-avatar :size="48" :src="order.studentAvatar">
                {{ order.studentName?.charAt(0) }}
              </el-avatar>
              <div class="info">
                <h4>{{ order.studentName }}</h4>
                <p>{{ order.grade }} · {{ order.subject }}</p>
              </div>
            </div>
            
            <div class="order-detail">
              <div class="detail-item">
                <span class="label">课时费</span>
                <span class="value price">¥{{ order.hourlyRate }}/小时</span>
              </div>
              <div class="detail-item">
                <span class="label">总课时</span>
                <span class="value">{{ order.totalHours }}小时</span>
              </div>
              <div class="detail-item">
                <span class="label">已完成</span>
                <span class="value">{{ order.completedHours || 0 }}小时</span>
              </div>
            </div>
          </div>
          
          <div class="order-footer">
            <span class="time">{{ formatTime(order.createTime) }}</span>
            <div class="actions">
              <el-button 
                v-if="order.status === 1" 
                type="primary" 
                size="small"
                @click.stop="startOrder(order)"
              >
                确认开课
              </el-button>
              <el-button 
                v-if="order.status === 2" 
                type="success" 
                size="small"
                @click.stop="goToLesson(order)"
              >
                去上课
              </el-button>
              <el-button 
                v-if="[1, 2].includes(order.status)" 
                size="small"
                @click.stop="goToChat(order)"
              >
                联系家长
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无订单" />
      
      <!-- 分页 -->
      <div v-if="total > pageSize" class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadOrders"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTutorOrders, confirmStartOrder as confirmStartOrderApi } from '@shared/api/order'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const orders = ref([])
const activeTab = ref('all')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusMap = {
  all: null,
  pending: 1,
  ongoing: 2,
  completed: 3,
  cancelled: 4
}

const getStatusType = (status) => {
  const map = { '-1': 'info', 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { '-1': '待家长确认', 0: '待家长支付', 1: '待开课', 2: '进行中', 3: '已完成', 4: '已取消' }
  return map[status] || '未知'
}

const formatTime = (time) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getTutorOrders({
      status: statusMap[activeTab.value],
      page: page.value,
      pageSize: pageSize.value
    })
    
    if (res.code === 200) {
      orders.value = res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  page.value = 1
  loadOrders()
}

const viewDetail = (id) => {
  router.push(`/orders/${id}`)
}

const startOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确认开始 ${order.studentName} 的家教课程吗？`,
      '确认开课'
    )
    
    const res = await confirmStartOrderApi(order.id)
    if (res.code === 200) {
      ElMessage.success('开课成功')
      loadOrders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const goToLesson = (order) => {
  router.push(`/lessons?orderId=${order.id}`)
}

const goToChat = (order) => {
  router.push(`/chat/${order.parentId}`)
}

onMounted(() => {
  loadOrders()
})
</script>

<style lang="scss" scoped>
.order-list {
  .order-container {
    margin-top: 16px;
  }
  
  .order-cards {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }
  
  .order-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    cursor: pointer;
    transition: box-shadow 0.2s;
    
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .order-no {
        font-size: 13px;
        color: #909399;
      }
    }
    
    .order-body {
      .student-info {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;
        
        .info {
          h4 {
            font-size: 16px;
            font-weight: 600;
            margin-bottom: 4px;
          }
          
          p {
            font-size: 13px;
            color: #606266;
          }
        }
      }
      
      .order-detail {
        display: flex;
        gap: 24px;
        padding: 12px;
        background: #f5f7fa;
        border-radius: 8px;
        
        .detail-item {
          .label {
            font-size: 12px;
            color: #909399;
            display: block;
            margin-bottom: 4px;
          }
          
          .value {
            font-size: 14px;
            font-weight: 500;
            
            &.price {
              color: #f56c6c;
            }
          }
        }
      }
    }
    
    .order-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #ebeef5;
      
      .time {
        font-size: 12px;
        color: #909399;
      }
      
      .actions {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .pagination {
    display: flex;
    justify-content: center;
    margin-top: 24px;
  }
}

// 响应式
@media (max-width: 768px) {
  .order-list {
    .order-card {
      .order-body {
        .order-detail {
          flex-wrap: wrap;
          gap: 12px;
          
          .detail-item {
            flex: 1;
            min-width: 80px;
          }
        }
      }
      
      .order-footer {
        flex-direction: column;
        gap: 12px;
        
        .actions {
          width: 100%;
          justify-content: flex-end;
        }
      }
    }
  }
}
</style>
