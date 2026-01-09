<template>
  <div class="find-students">
    <!-- 搜索筛选 -->
    <div class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="科目">
          <el-select v-model="searchForm.subject" placeholder="选择科目" clearable>
            <el-option label="语文" value="语文" />
            <el-option label="数学" value="数学" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="历史" value="历史" />
            <el-option label="地理" value="地理" />
            <el-option label="政治" value="政治" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="searchForm.grade" placeholder="选择年级" clearable>
            <el-option label="小学" value="小学" />
            <el-option label="初中" value="初中" />
            <el-option label="高中" value="高中" />
          </el-select>
        </el-form-item>
        <el-form-item label="距离">
          <el-select v-model="searchForm.radius" placeholder="选择距离">
            <el-option label="3公里内" :value="3" />
            <el-option label="5公里内" :value="5" />
            <el-option label="10公里内" :value="10" />
            <el-option label="不限" :value="50" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="toggleView">
            <el-icon><component :is="viewMode === 'map' ? 'List' : 'Location'" /></el-icon>
            {{ viewMode === 'map' ? '列表模式' : '地图模式' }}
          </el-button>
        </el-form-item>
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
              <span class="distance">{{ demand.distance }}km</span>
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
import { Search, Location, List, Aim, Close, User, Clock } from '@element-plus/icons-vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { getNearbyDemands, getDemandList } from '@shared/api/demand'
import { acceptOrder } from '@shared/api/order'

const router = useRouter()

const viewMode = ref('list') // 'map' | 'list'
const loading = ref(false)
const demands = ref([])
const selectedDemand = ref(null)
const hasMore = ref(true)
const page = ref(1)

let map = null
let currentPosition = { longitude: 116.397428, latitude: 39.90923 }

const searchForm = reactive({
  subject: '',
  grade: '',
  radius: 5
})

const getSubjectType = (subject) => {
  const typeMap = {
    '语文': '',
    '数学': 'success',
    '英语': 'warning',
    '物理': 'danger',
    '化学': 'info'
  }
  return typeMap[subject] || ''
}

const initMap = async () => {
  try {
    const AMap = await AMapLoader.load({
      key: 'YOUR_AMAP_KEY', // 替换为实际的高德地图 Key
      version: '2.0',
      plugins: ['AMap.Geolocation', 'AMap.Marker']
    })
    
    map = new AMap.Map('demand-map', {
      zoom: 14,
      center: [currentPosition.longitude, currentPosition.latitude]
    })
    
    // 获取当前位置
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
    const res = await getNearbyDemands({
      longitude: currentPosition.longitude,
      latitude: currentPosition.latitude,
      radius: searchForm.radius,
      subject: searchForm.subject,
      grade: searchForm.grade,
      page: page.value,
      size: 20
    })
    
    if (res.code === 200) {
      const rawList = res.data?.records ?? res.data ?? []
      // 后端字段 -> 前端展示字段映射
      const list = rawList.map(item => ({
        id: item.id,
        title: item.title,
        subject: item.subject,
        grade: item.grade,
        salary: item.expectPrice,
        distance: item.distance || item.km || null,
        description: item.detail || item.description,
        frequency: item.scheduleRequire ? parseSchedule(item.scheduleRequire) : '',
        address: item.address,
        status: item.status,
        teachMode: item.teachMode
      }))
      if (page.value === 1) {
        demands.value = list
      } else {
        demands.value.push(...list)
      }
      hasMore.value = list.length >= 20
      
      // 在地图上显示标记
      if (map && viewMode.value === 'map') {
        addMarkersToMap(list)
      }
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
  list.forEach(demand => {
    if (demand.longitude && demand.latitude) {
      const marker = new AMap.Marker({
        position: [demand.longitude, demand.latitude],
        title: demand.title || demand.subject
      })
      
      marker.on('click', () => {
        selectedDemand.value = demand
      })
      
      map.add(marker)
    }
  })
}

const handleSearch = () => {
  page.value = 1
  loadNearbyDemands()
}

const toggleView = () => {
  viewMode.value = viewMode.value === 'map' ? 'list' : 'map'
  if (viewMode.value === 'map' && !map) {
    initMap()
  }
}

const relocate = () => {
  if (map) {
    map.setCenter([currentPosition.longitude, currentPosition.latitude])
  }
}

const loadMore = () => {
  page.value++
  loadNearbyDemands()
}

const viewDetail = (demand) => {
  router.push(`/demand/${demand.id}`)
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
  // 默认加载列表数据
  loadNearbyDemands()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})
</script>

<style lang="scss" scoped>
.find-students {
  .search-card {
    background: #fff;
    padding: 16px 24px;
    border-radius: 12px;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    @media (max-width: 768px) {
      padding: 12px;
      
      :deep(.el-form-item) {
        margin-bottom: 8px;
        margin-right: 0;
        width: 100%;
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
