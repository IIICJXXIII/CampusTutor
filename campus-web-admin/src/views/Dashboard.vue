<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <div class="stat-card card-shadow">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card card-shadow">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c);">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalTutors }}</div>
            <div class="stat-label">教师总数</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card card-shadow">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalOrders }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="stat-card card-shadow">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ stats.totalRevenue }}</div>
            <div class="stat-label">总交易额</div>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <!-- 待办事项 -->
    <el-row :gutter="20" class="todo-row">
      <el-col :span="6">
        <el-card class="todo-card" shadow="hover" @click="$router.push('/tutor-audit')">
          <div class="todo-content">
            <el-badge :value="stats.pendingAudit" :max="99">
              <el-icon class="todo-icon warning"><Stamp /></el-icon>
            </el-badge>
            <span>待审核认证</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="todo-card" shadow="hover" @click="$router.push('/orders?status=pending')">
          <div class="todo-content">
            <el-badge :value="stats.pendingOrders" :max="99">
              <el-icon class="todo-icon primary"><List /></el-icon>
            </el-badge>
            <span>待处理订单</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="todo-card" shadow="hover" @click="$router.push('/lessons?status=pending')">
          <div class="todo-content">
            <el-badge :value="stats.pendingLessons" :max="99">
              <el-icon class="todo-icon success"><Calendar /></el-icon>
            </el-badge>
            <span>待确认课时</span>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="todo-card" shadow="hover">
          <div class="todo-content">
            <el-badge :value="stats.pendingRefunds" :max="99">
              <el-icon class="todo-icon danger"><RefreshLeft /></el-icon>
            </el-badge>
            <span>待处理退款</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="chart-header">
              <span>订单趋势</span>
              <el-radio-group v-model="orderChartType" size="small">
                <el-radio-button label="week">近7天</el-radio-button>
                <el-radio-button label="month">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="orderChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span>用户角色分布</span>
          </template>
          <div ref="userChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 最新动态 -->
    <el-row :gutter="20" class="recent-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-with-action">
              <span>最新注册用户</span>
              <el-button text type="primary" @click="$router.push('/users')">查看更多</el-button>
            </div>
          </template>
          <el-table :data="recentUsers" size="small">
            <el-table-column prop="nickname" label="用户名" />
            <el-table-column prop="role" label="角色">
              <template #default="{ row }">
                <el-tag :type="row.role === 1 ? 'primary' : 'success'" size="small">
                  {{ row.role === 1 ? '教师' : '家长' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="160" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header-with-action">
              <span>最新订单</span>
              <el-button text type="primary" @click="$router.push('/orders')">查看更多</el-button>
            </div>
          </template>
          <el-table :data="recentOrders" size="small">
            <el-table-column prop="orderNo" label="订单号" width="140" />
            <el-table-column prop="subject" label="科目" />
            <el-table-column prop="amount" label="金额">
              <template #default="{ row }">
                ¥{{ row.amount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusType(row.status)" size="small">
                  {{ getOrderStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { statsApi, userApi, orderApi } from '@/api'

// 统计数据
const stats = reactive({
  totalUsers: 0,
  totalTutors: 0,
  totalOrders: 0,
  totalRevenue: '0',
  pendingAudit: 0,
  pendingOrders: 0,
  pendingLessons: 0,
  pendingRefunds: 0
})

// 最新用户 (模拟)
const recentUsers = ref([
  { id: 1, nickname: '张老师', role: 1, createTime: '2026-01-06 10:30' },
  { id: 2, nickname: '李家长', role: 2, createTime: '2026-01-06 09:15' },
  { id: 3, nickname: '王老师', role: 1, createTime: '2026-01-05 18:20' },
  { id: 4, nickname: '刘家长', role: 2, createTime: '2026-01-05 16:45' },
  { id: 5, nickname: '陈老师', role: 1, createTime: '2026-01-05 14:30' }
])

// 最新订单 (模拟)
const recentOrders = ref([
  { id: 1, orderNo: 'ORD20260106001', subject: '数学', amount: 200, status: 1 },
  { id: 2, orderNo: 'ORD20260106002', subject: '英语', amount: 180, status: 2 },
  { id: 3, orderNo: 'ORD20260105003', subject: '物理', amount: 220, status: 3 },
  { id: 4, orderNo: 'ORD20260105004', subject: '化学', amount: 200, status: 1 },
  { id: 5, orderNo: 'ORD20260104005', subject: '语文', amount: 160, status: 4 }
])

const orderChartType = ref('week')
const orderChartRef = ref()
const userChartRef = ref()
let orderChart = null
let userChart = null

const getOrderStatusType = (status) => {
  const types = { 0: 'info', 1: 'warning', 2: 'primary', 3: '', 4: 'success' }
  return types[status] || 'info'
}

const getOrderStatusText = (status) => {
  const texts = { 0: '待支付', 1: '已支付', 2: '托管中', 3: '进行中', 4: '已完成' }
  return texts[status] || '未知'
}

const initOrderChart = () => {
  if (!orderChartRef.value) return
  
  orderChart = echarts.init(orderChartRef.value)
  
  const days = orderChartType.value === 'week' ? 7 : 30
  const dates = Array.from({ length: days }, (_, i) => {
    const d = new Date()
    d.setDate(d.getDate() - (days - 1 - i))
    return `${d.getMonth() + 1}/${d.getDate()}`
  })
  
  const orderData = Array.from({ length: days }, () => Math.floor(Math.random() * 50) + 10)
  const revenueData = Array.from({ length: days }, () => Math.floor(Math.random() * 5000) + 1000)
  
  orderChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['订单数', '交易额']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '交易额(元)', position: 'right' }
    ],
    series: [
      {
        name: '订单数',
        type: 'bar',
        data: orderData,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '交易额',
        type: 'line',
        yAxisIndex: 1,
        data: revenueData,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  })
}

const initUserChart = () => {
  if (!userChartRef.value) return
  
  userChart = echarts.init(userChartRef.value)
  
  userChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '5%',
      left: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 328, name: '教师', itemStyle: { color: '#409EFF' } },
          { value: 928, name: '家长', itemStyle: { color: '#67C23A' } }
        ]
      }
    ]
  })
}

watch(orderChartType, () => {
  initOrderChart()
})

// 获取仪表盘数据
const fetchDashboardData = async () => {
  try {
    const res = await statsApi.getDashboard()
    const data = res.data
    
    stats.totalUsers = data.totalUsers || 0
    stats.totalTutors = data.totalTutors || 0
    stats.totalOrders = data.totalOrders || 0
    stats.totalRevenue = data.totalRevenue ? Number(data.totalRevenue).toLocaleString() : '0'
    stats.pendingAudit = data.pendingAudit || 0
    stats.pendingOrders = data.pendingOrders || 0
    stats.pendingLessons = data.pendingLessons || 0
    stats.pendingRefunds = data.pendingRefunds || 0
    
    // 更新最新用户
    if (data.recentUsers?.length) {
      recentUsers.value = data.recentUsers.map(u => ({
        id: u.id,
        nickname: u.nickname || u.username,
        role: u.role,
        createTime: u.createTime?.replace('T', ' ')?.substring(0, 16)
      }))
    }
    
    // 更新最新订单
    if (data.recentOrders?.length) {
      recentOrders.value = data.recentOrders.map(o => ({
        id: o.id,
        orderNo: o.orderNo,
        subject: o.subject,
        amount: o.amount,
        status: o.status
      }))
    }
    
    // 更新饼图数据
    if (userChart) {
      userChart.setOption({
        series: [{
          data: [
            { value: data.tutorCount || stats.totalTutors, name: '教师', itemStyle: { color: '#409EFF' } },
            { value: data.parentCount || stats.totalParents || (stats.totalUsers - stats.totalTutors), name: '家长', itemStyle: { color: '#67C23A' } }
          ]
        }]
      })
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
  }
}

onMounted(() => {
  initOrderChart()
  initUserChart()
  fetchDashboardData()
  
  window.addEventListener('resize', () => {
    orderChart?.resize()
    userChart?.resize()
  })
})

onUnmounted(() => {
  orderChart?.dispose()
  userChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-row {
    margin-bottom: 20px;
  }
  
  .todo-row {
    margin-bottom: 20px;
    
    .todo-card {
      cursor: pointer;
      transition: transform 0.3s;
      
      &:hover {
        transform: translateY(-4px);
      }
      
      .todo-content {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .todo-icon {
          font-size: 28px;
          
          &.primary { color: #409EFF; }
          &.success { color: #67C23A; }
          &.warning { color: #E6A23C; }
          &.danger { color: #F56C6C; }
        }
        
        span {
          font-size: 14px;
          color: #606266;
        }
      }
    }
  }
  
  .chart-row {
    margin-bottom: 20px;
    
    .chart-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .chart-container {
      height: 300px;
    }
  }
  
  .recent-row {
    .card-header-with-action {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
}
</style>
