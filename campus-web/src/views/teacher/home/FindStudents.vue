<template>
  <div class="find-students">
    <!-- 搜索筛选 -->
    <div class="filter-bar">
      <div class="filter-header">
        <div class="filter-title">
          <el-icon><Filter /></el-icon>
          <span>筛选条件</span>
        </div>
        <div class="location-status" v-if="!locating">
          <span class="status-dot" :class="{ ready: positionReady }"></span>
          <span class="status-text">{{ positionReady ? '已定位' : '使用注册地址' }}</span>
        </div>
      </div>
      <el-form :inline="true" :model="searchForm" class="filter-form">
        <div class="filter-controls">
          <el-form-item label="科目">
            <el-select v-model="searchForm.subject" placeholder="全部科目" clearable size="default">
              <el-option-group label="艺术素养">
                <el-option label="钢琴/乐器陪练" value="钢琴/乐器陪练" />
                <el-option label="美术/书法" value="美术/书法" />
                <el-option label="声乐/视唱练耳" value="声乐/视唱练耳" />
              </el-option-group>
              <el-option-group label="体育健康">
                <el-option label="中考体育" value="中考体育" />
                <el-option label="羽毛球/网球" value="羽毛球/网球" />
                <el-option label="篮球/足球" value="篮球/足球" />
              </el-option-group>
              <el-option-group label="科创STEAM">
                <el-option label="少儿编程(Scratch/Python)" value="少儿编程(Scratch/Python)" />
                <el-option label="机器人/3D打印" value="机器人/3D打印" />
                <el-option label="科学实验/航模" value="科学实验/航模" />
              </el-option-group>
            </el-select>
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="searchForm.grade" placeholder="全部年级" clearable size="default">
              <el-option label="小学" value="小学" />
              <el-option label="初中" value="初中" />
              <el-option label="高中" value="高中" />
            </el-select>
          </el-form-item>
          <el-form-item label="距离">
            <el-select v-model="searchForm.radius" placeholder="搜索范围" size="default">
              <el-option label="3公里内" :value="3" />
              <el-option label="5公里内" :value="5" />
              <el-option label="10公里内" :value="10" />
              <el-option label="20公里内" :value="20" />
              <el-option label="50公里内" :value="50" />
            </el-select>
          </el-form-item>
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch" class="btn-search">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="toggleView" class="btn-toggle">
            <el-icon><component :is="viewMode === 'map' ? 'List' : 'Location'" /></el-icon>
            {{ viewMode === 'map' ? '列表' : '地图' }}
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- 地图视图 -->
    <div v-show="viewMode === 'map'" class="map-container">
      <div id="demand-map" class="demand-map"></div>
      
      <!-- 定位按钮 -->
      <div class="map-controls">
        <el-button circle @click="relocate">
          <el-icon><Aim /></el-icon>
        </el-button>
      </div>
      
      <!-- 需求卡片弹窗 -->
      <transition name="slide-up">
        <div v-if="selectedDemand" class="demand-popup">
          <div class="popup-header">
            <h3>{{ selectedDemand.title || `${selectedDemand.subject} 家教需求` }}</h3>
            <el-icon class="close-btn" @click="selectedDemand = null"><Close /></el-icon>
          </div>
          <div class="popup-body">
            <el-descriptions :column="2" size="small">
              <el-descriptions-item label="科目">{{ selectedDemand.subject }}</el-descriptions-item>
              <el-descriptions-item label="年级">{{ selectedDemand.grade }}</el-descriptions-item>
              <el-descriptions-item label="薪资">
                <span class="price">¥{{ selectedDemand.salary }}/小时</span>
              </el-descriptions-item>
              <el-descriptions-item label="距离">{{ selectedDemand.distance }}km</el-descriptions-item>
            </el-descriptions>
            <div class="popup-actions">
              <el-button @click="viewDetail(selectedDemand)">查看详情</el-button>
              <el-button @click="contactParent(selectedDemand)">联系家长</el-button>
              <el-button type="primary" @click="handleAccept(selectedDemand)">立即接单</el-button>
            </div>
          </div>
        </div>
      </transition>
    </div>

    <!-- 列表视图 -->
    <div v-show="viewMode === 'list'" class="list-container">
      <el-row :gutter="16">
        <el-col 
          v-for="demand in demands" 
          :key="demand.id" 
          :xs="24" :sm="12" :md="8" :lg="6"
        >
          <div class="demand-card" @click="viewDetail(demand)">
            <div class="card-header">
              <el-tag :type="getSubjectType(demand.subject)" size="small">
                {{ demand.subject }}
              </el-tag>
              <span class="distance">{{ demand.distance != null ? demand.distance + 'km' : '' }}</span>
            </div>
            <h3 class="card-title">{{ demand.title || `${demand.grade}${demand.subject}辅导` }}</h3>
            <p class="card-desc">{{ demand.description || '暂无描述' }}</p>
            <div class="card-info">
              <span><el-icon><User /></el-icon>{{ demand.grade }}</span>
              <span><el-icon><Clock /></el-icon>{{ demand.frequency }}</span>
            </div>
            <div class="card-footer">
              <span class="price">¥{{ demand.salary }}<small>/小时</small></span>
              <el-button type="primary" size="small" @click.stop="handleAccept(demand)">
                接单
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
      
      <!-- 空状态 -->
      <el-empty v-if="demands.length === 0 && !loading" description="暂无附近需求" />
      
      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <el-button :loading="loading" @click="loadMore">加载更多</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Location, List, Aim, Close, User, Clock, Filter } from '@element-plus/icons-vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { getDemandListWithMatch } from '@shared/api/demand'
import { acceptOrder, getTutorOrders } from '@shared/api/order'

const router = useRouter()

const viewMode = ref('list') // 'map' | 'list'
const loading = ref(false)
const demands = ref([])
const selectedDemand = ref(null)
const hasMore = ref(true)
const page = ref(1)
const locating = ref(true)
const positionReady = ref(false)

let map = null
let currentPosition = { longitude: null, latitude: null }

const searchForm = reactive({
  subject: '',
  grade: '',
  radius: 10
})

const getSubjectType = (subject) => {
  const typeMap = {
    '钢琴/乐器陪练': '',
    '美术/书法': 'success',
    '声乐/视唱练耳': 'warning',
    '中考体育': 'danger',
    '羽毛球/网球': 'info',
    '篮球/足球': 'danger',
    '少儿编程(Scratch/Python)': 'success',
    '机器人/3D打印': 'warning',
    '科学实验/航模': 'info'
  }
  return typeMap[subject] || ''
}

// 使用浏览器原生API获取位置（不依赖高德地图SDK），后台静默更新
const initGeolocation = () => {
  locating.value = true
  if (!navigator.geolocation) {
    locating.value = false
    loadNearbyDemands()
    return
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {
      currentPosition = {
        longitude: position.coords.longitude,
        latitude: position.coords.latitude
      }
      positionReady.value = true
      locating.value = false
      // 获得精确坐标后刷新数据
      loadNearbyDemands()
    },
    () => {
      locating.value = false
      // GPS失败时，使用后端兜底（教师档案注册地址）加载数据
      loadNearbyDemands()
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
  )
}

const initMap = async () => {
  try {
    const amapKey = import.meta.env.VITE_AMAP_KEY
    if (!amapKey) {
      ElMessage.warning('地图功能需要配置高德地图 Key')
      return
    }
    const AMap = await AMapLoader.load({
      key: amapKey,
      version: '1.4.15',
      plugins: ['AMap.Geolocation', 'AMap.Marker']
    })
    
    const center = (currentPosition.longitude && currentPosition.latitude)
      ? [currentPosition.longitude, currentPosition.latitude]
      : [116.397428, 39.90923] // 默认北京，geolocation 成功后会自动更新

    map = new AMap.Map('demand-map', {
      zoom: 14,
      center
    })

    // 如果数据已加载（列表模式先于地图初始化完成），立即渲染标记
    if (demands.value.length > 0) {
      addMarkersToMap(demands.value)
    }

    // 获取当前位置（AMap 高精度定位，成功后刷新数据）
    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000
    })

    geolocation.getCurrentPosition((status, result) => {
      if (status === 'complete') {
        currentPosition = {
          longitude: result.position.lng,
          latitude: result.position.lat
        }
        map.setCenter([currentPosition.longitude, currentPosition.latitude])
        // AMap 高精度定位成功后，用更精确的坐标刷新数据
        loadNearbyDemands()
      }
    })
  } catch (error) {
    console.error('地图加载失败:', error)
  }
}

const loadNearbyDemands = async () => {
  loading.value = true
  try {
    let rawList = []
    let total = 0

    // 构建位置参数：只传有效坐标，避免 null 导致后端解析异常
    const hasPosition = currentPosition.longitude && currentPosition.latitude
    const locationParams = hasPosition
      ? { longitude: currentPosition.longitude, latitude: currentPosition.latitude }
      : {}

    // 统一使用 list-with-match 接口，保证列表/地图模式数据一致
    // 地图模式拉取更大数据量以便一次性展示所有标记
    const pageSize = viewMode.value === 'map' ? 200 : 20
    const matchRes = await getDemandListWithMatch({
      subject: searchForm.subject || undefined,
      grade: searchForm.grade || undefined,
      radius: searchForm.radius,
      ...locationParams,
      page: page.value,
      size: pageSize
    })
    if (matchRes.code === 200 && matchRes.data) {
      rawList = matchRes.data.records ?? (Array.isArray(matchRes.data) ? matchRes.data : [])
      total = matchRes.data.total ?? 0
    }

    // 仅过滤已上架的需求（防御性过滤）
    rawList = rawList.filter(item => item.status === 1)

    const list = rawList.map(item => ({
      id: item.id,
      title: item.title,
      subject: item.subject,
      grade: item.grade,
      salary: item.expectPrice,
      distance: item.distance != null ? Math.round(item.distance * 10) / 10 : (item.km || null),
      description: item.detail || item.description,
      frequency: item.scheduleRequire ? parseSchedule(item.scheduleRequire) : '',
      address: item.address,
      status: item.status,
      teachMode: item.teachMode,
      longitude: item.longitude,
      latitude: item.latitude
    }))
    if (page.value === 1) {
      demands.value = list
    } else {
      demands.value.push(...list)
    }
    hasMore.value = total > 0 ? demands.value.length < total : list.length >= pageSize

    if (map && viewMode.value === 'map') {
      addMarkersToMap(list)
    }
  } catch (error) {
    console.error('加载需求失败:', error)
  } finally {
    loading.value = false
  }
}

// 将后端保存的 scheduleRequire JSON 字符串转成简短文本
const parseSchedule = (value) => {
  if (!value) return ''
  try {
    const arr = typeof value === 'string' ? JSON.parse(value) : value
    if (Array.isArray(arr)) {
      return arr.join('，')
    }
    return ''
  } catch (e) {
    return ''
  }
}

const addMarkersToMap = (list) => {
  map.clearMap() // 清除已有标记再重新打点，避免切换模式或刷新时重复
  list.forEach(demand => {
    if (demand.longitude && demand.latitude) {
      const marker = new AMap.Marker({
        position: [demand.longitude, demand.latitude],
        title: demand.title || demand.subject,
        content: `<div style="background:#409eff;color:#fff;padding:2px 8px;border-radius:10px;font-size:12px;white-space:nowrap;">¥${demand.salary || '?'} ${demand.subject} ${demand.grade || ''}</div>`
      })
      
      marker.on('click', () => {
        selectedDemand.value = demand
      })
      
      map.add(marker)
    }
  })

  addOrderMarkersToMap()
}

const addOrderMarkersToMap = async () => {
  if (!map) return
  try {
    const res = await getTutorOrders({ status: undefined, page: 1, size: 50 })
    if (res.code === 200) {
      const orders = res.data?.records || res.data || []
      const statusColors = { '-1': '#e6a23c', 0: '#909399', 1: '#409eff', 2: '#67c23a', 3: '#909399' }
      const statusTexts = { '-1': '待确认', 0: '待支付', 1: '待开课', 2: '进行中', 3: '已完成' }
      orders.forEach(order => {
        if (order.longitude && order.latitude) {
          const color = statusColors[order.status] || '#909399'
          const statusText = statusTexts[order.status] || '未知'
          const marker = new AMap.Marker({
            position: [order.longitude, order.latitude],
            title: `${order.subject} - ${statusText}`,
            content: `<div style="background:${color};color:#fff;padding:4px 10px;border-radius:12px;font-size:12px;white-space:nowrap;box-shadow:0 2px 6px rgba(0,0,0,0.2);border:2px solid #fff;">📋${statusText} ${order.subject || ''}</div>`,
            offset: new AMap.Pixel(-30, -15)
          })
          marker.on('click', () => {
            router.push(`/teacher/orders/${order.id}`)
          })
          map.add(marker)
        }
      })
    }
  } catch (e) {
    console.error('加载订单标记失败:', e)
  }
}

const handleSearch = () => {
  page.value = 1
  loadNearbyDemands()
}

const toggleView = () => {
  viewMode.value = viewMode.value === 'map' ? 'list' : 'map'
  page.value = 1
  if (viewMode.value === 'map') {
    if (!map) {
      initMap() // initMap 负责初始化地图并渲染已有数据
    } else {
      loadNearbyDemands() // 地图已存在，直接刷新数据并打点
    }
  } else {
    loadNearbyDemands() // 列表模式，重新加载数据
  }
}

const relocate = () => {
  if (map && currentPosition.longitude && currentPosition.latitude) {
    map.setCenter([currentPosition.longitude, currentPosition.latitude])
  }
}

const loadMore = () => {
  page.value++
  loadNearbyDemands()
}

const viewDetail = (demand) => {
  router.push(`/teacher/demand/${demand.id}`)
}

const contactParent = (demand) => {
  if (demand.parentId) {
    router.push(`/chat/${demand.parentId}`)
  } else {
    ElMessage.info('暂无家长联系信息，请通过查看详情联系')
  }
}

const handleAccept = async (demand) => {
  try {
    await ElMessageBox.confirm(
      `确定要接单「${demand.subject}辅导」吗？`,
      '确认接单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    // 后端API需要传入 { demandId, totalHours, remark }
    const res = await acceptOrder({
      demandId: demand.id,
      totalHours: 10,
      remark: ''
    })
    if (res.code === 200) {
      ElMessage.success('接单成功！等待家长确认')
      // 刷新列表
      loadNearbyDemands()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '接单失败')
    }
  }
}

onMounted(() => {
  // 先获取GPS位置，成功或失败后再加载数据，避免无坐标时返回全量数据
  initGeolocation()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})
</script>

<style lang="scss" scoped>
.find-students {
  .filter-bar {
    background: #fff;
    border-radius: 12px;
    margin-bottom: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    overflow: hidden;

    .filter-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 14px 20px;
      background: linear-gradient(135deg, #f8fafc 0%, #f0f4f8 100%);
      border-bottom: 1px solid #ebeef5;

      .filter-title {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        font-weight: 600;
        color: #303133;

        .el-icon {
          color: #409eff;
          font-size: 15px;
        }
      }

      .location-status {
        display: flex;
        align-items: center;
        gap: 6px;

        .status-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: #e6a23c;
          transition: background 0.3s;

          &.ready {
            background: #67c23a;
          }
        }

        .status-text {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .filter-form {
      padding: 16px 20px;

      .filter-controls {
        display: flex;
        flex-wrap: wrap;
        gap: 0 20px;
        margin-bottom: 12px;

        :deep(.el-form-item) {
          margin-bottom: 0;
          flex-shrink: 0;

          .el-form-item__label {
            font-size: 13px;
            color: #606266;
            font-weight: 500;
            padding-right: 8px;
          }

          .el-select {
            width: 180px;
          }
        }
      }

      .filter-actions {
        display: flex;
        gap: 10px;

        .btn-search {
          border-radius: 8px;
          font-weight: 500;
          padding: 8px 18px;
        }

        .btn-toggle {
          border-radius: 8px;
          font-weight: 500;
          padding: 8px 16px;
          color: #606266;
          border-color: #dcdfe6;

          &:hover {
            color: #409eff;
            border-color: #c6e2ff;
            background: #ecf5ff;
          }
        }
      }
    }

    @media (max-width: 768px) {
      .filter-header {
        padding: 12px 16px;
      }

      .filter-form {
        padding: 12px 16px;

        .filter-controls {
          flex-direction: column;
          gap: 10px;

          :deep(.el-form-item) {
            width: 100%;
            margin-right: 0;

            .el-select {
              width: 100%;
            }
          }
        }

        .filter-actions {
          flex-direction: column;

          .el-button {
            width: 100%;
          }
        }
      }
    }
  }

  .map-container {
    position: relative;
    height: calc(100vh - 220px);
    border-radius: 12px;
    overflow: hidden;
    
    @media (max-width: 768px) {
      height: calc(100vh - 200px);
    }
    
    .demand-map {
      width: 100%;
      height: 100%;
    }
    
    .map-controls {
      position: absolute;
      right: 16px;
      bottom: 100px;
    }
    
    .demand-popup {
      position: absolute;
      left: 16px;
      right: 16px;
      bottom: 16px;
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      overflow: hidden;
      
      .popup-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px;
        border-bottom: 1px solid #ebeef5;
        
        h3 {
          font-size: 16px;
          font-weight: 600;
        }
        
        .close-btn {
          cursor: pointer;
          color: #909399;
        }
      }
      
      .popup-body {
        padding: 16px;
        
        .price {
          color: #f56c6c;
          font-weight: 600;
        }
        
        .popup-actions {
          margin-top: 16px;
          display: flex;
          gap: 12px;
          
          .el-button {
            flex: 1;
          }
        }
      }
    }
  }
  
  .list-container {
    .demand-card {
      background: #fff;
      border-radius: 12px;
      padding: 16px;
      margin-bottom: 16px;
      cursor: pointer;
      transition: all 0.3s;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        .distance {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .card-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 8px;
        color: #303133;
      }
      
      .card-desc {
        font-size: 13px;
        color: #909399;
        margin-bottom: 12px;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
      
      .card-info {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #606266;
        margin-bottom: 12px;
        
        span {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
      
      .card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .price {
          font-size: 20px;
          font-weight: 700;
          color: #f56c6c;
          
          small {
            font-size: 12px;
            font-weight: 400;
          }
        }
      }
    }
    
    .load-more {
      text-align: center;
      padding: 24px;
    }
  }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(100%);
}
</style>
