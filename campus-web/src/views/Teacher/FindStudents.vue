<template>
  <div class="find-students-page">
    <div class="page-container">
      <el-row :gutter="24">
        <!-- 左侧筛选栏 -->
        <el-col :span="6" class="filter-col">
          <el-card class="filter-card" shadow="hover">
            <template #header>
              <div class="filter-header">
                <span>筛选条件</span>
                <el-button link type="primary" size="small" @click="resetFilter">重置</el-button>
              </div>
            </template>
            
            <el-form :model="filterForm" label-position="top" size="default">
              <el-form-item label="科目">
                <el-select v-model="filterForm.subject" placeholder="请选择科目" clearable style="width: 100%">
                  <el-option v-for="item in subjects" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>

              <el-form-item label="年级">
                <el-select v-model="filterForm.grade" placeholder="请选择年级" clearable style="width: 100%">
                  <el-option v-for="item in grades" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>

              <el-form-item label="授课方式">
                <el-radio-group v-model="filterForm.teachMode">
                  <el-radio :value="null">不限</el-radio>
                  <el-radio :value="1">上门</el-radio>
                  <el-radio :value="2">网课</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-button type="primary" class="search-btn" @click="handleSearch" :loading="loading">
                搜索需求
              </el-button>
            </el-form>
          </el-card>
        </el-col>

        <!-- 右侧内容区 -->
        <el-col :span="18">
          <div class="toolbar">
            <div class="total-count">共找到 <b>{{ students.length }}</b> 个需求</div>
            <el-radio-group v-model="viewMode" size="default" @change="handleViewChange">
              <el-radio-button value="list">
                <el-icon><List /></el-icon> 列表
              </el-radio-button>
              <el-radio-button value="map">
                <el-icon><MapLocation /></el-icon> 地图
              </el-radio-button>
            </el-radio-group>
          </div>

          <div v-show="viewMode === 'list'" class="list-container" v-loading="loading">
            <div v-if="students.length === 0 && !loading" class="empty-state">
              <el-empty description="附近暂无需求，试着扩大搜索范围" />
            </div>
            
            <div class="student-grid">
              <el-card 
                v-for="item in students" 
                :key="item.id" 
                class="student-card"
                shadow="hover"
                @click="goDetail(item.id)"
              >
                <div class="card-header">
                  <span class="grade-subject">{{ item.grade }} · {{ item.subject }}</span>
                  <span class="price">¥{{ item.expectPrice }}/h</span>
                </div>
                <div class="card-body">
                  <p class="desc line-clamp-2">{{ item.detail || '暂无详细描述...' }}</p>
                  <div class="tags">
                    <el-tag size="small" type="info" effect="plain">{{ item.teachMode === 1 ? '上门' : '网课' }}</el-tag>
                    <el-tag size="small" type="warning" effect="plain" v-if="item.distance">
                      距您 {{ formatDistance(item.distance) }}
                    </el-tag>
                  </div>
                </div>
                <div class="card-footer">
                  <span class="location"><el-icon><Location /></el-icon> {{ item.address || '位置保密' }}</span>
                  <span class="time">{{ formatTime(item.createTime) }}</span>
                </div>
              </el-card>
            </div>
          </div>

          <div v-show="viewMode === 'map'" class="map-container">
            <div id="amap-container" class="amap-box"></div>
            
            <transition name="slide-up">
              <div v-if="selectedDemand" class="map-float-card" @click="goDetail(selectedDemand.id)">
                <div class="float-header">
                  <span class="title">{{ selectedDemand.grade }} {{ selectedDemand.subject }}</span>
                  <span class="close-btn" @click.stop="selectedDemand = null">×</span>
                </div>
                <div class="float-content">
                  <div class="price-dist">
                    <span class="price">¥{{ selectedDemand.expectPrice }}</span>
                    <span class="dist">约 {{ formatDistance(selectedDemand.distance) }}</span>
                  </div>
                  <div class="address">{{ selectedDemand.address }}</div>
                </div>
                <el-button type="primary" size="small" class="action-btn">查看详情</el-button>
              </div>
            </transition>

            <div class="map-controls">
              <div class="control-btn" @click="locateUser" title="回到我的位置">
                <el-icon><Aim /></el-icon>
              </div>
              <div class="control-btn" @click="refreshMapData" title="搜索该区域">
                <el-icon><Refresh /></el-icon>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { findNearbyDemands } from '@/api/match'
import request from '@/api/request'
import AMapLoader from '@amap/amap-jsapi-loader'

// ==========================================
// 1. 配置高德地图 (请务必保留引号)
// ==========================================
// 替换为你的 Key (Web端 JS API)
// 注意：高德地图Web JS API Key需要在高德开放平台申请
// 申请地址：https://console.amap.com/dev/key/app
const MY_AMAP_KEY = '395b2de0b9cf263e585280d5d81821a4' 

const MY_SECURITY_KEY = '67ca393e1f0e458d3aa46ef4d1c92e3d'

// 2. 注入安全密钥 (必须在加载地图前执行)
window._AMapSecurityConfig = {
 securityJsCode: MY_SECURITY_KEY,
}
// ==========================================

const router = useRouter()
const viewMode = ref('list')
const loading = ref(false)
const searchQuery = ref('')
const students = ref([])
const selectedDemand = ref(null)

// 筛选表单
const filterForm = reactive({
  subject: '',
  grade: '',
  teachMode: null
})

const subjects = ['数学', '英语', '语文', '物理', '化学', '生物', '地理', '历史', '政治']
const grades = ['小学一年级', '小学二年级', '小学三年级', '小学四年级', '小学五年级', '小学六年级', 
              '初一', '初二', '初三', '高一', '高二', '高三']

const resetFilter = () => {
  filterForm.subject = ''
  filterForm.grade = ''
  filterForm.teachMode = null
  handleSearch()
}

// 使用 shallowRef 避免 Vue 深度代理地图实例导致卡顿
const map = shallowRef(null) 
let markers = []

// 初始化地图
const initMap = async () => {
  if (map.value) return

  try {
    const AMap = await AMapLoader.load({
      key:'60486ea8e5b0ac4d166a78e68fdeaba1', // 使用上面定义的常量
      version: "2.0",
      plugins: ['AMap.Geolocation', 'AMap.ToolBar', 'AMap.Scale']
    })

    const mapInstance = new AMap.Map('amap-container', {
      zoom: 13,
      center: [116.4074, 39.9042], // 默认北京
      viewMode: '3D'
    })
    
    map.value = mapInstance
    mapInstance.addControl(new AMap.ToolBar())
    mapInstance.addControl(new AMap.Scale())

    // 定位插件
    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
      buttonPosition: 'RB',
      zoomToAccuracy: true
    })
    mapInstance.addControl(geolocation)

    geolocation.getCurrentPosition((status, result) => {
      if (status === 'complete') {
        mapInstance.setCenter([result.position.lng, result.position.lat])
        fetchData(result.position.lng, result.position.lat) 
      } else {
        ElMessage.warning('定位失败，使用默认位置')
        fetchData(116.4074, 39.9042)
      }
    })

    mapInstance.on('moveend', () => {
      if (viewMode.value === 'map') {
        const center = mapInstance.getCenter()
        fetchData(center.getLng(), center.getLat())
      }
    })

  } catch (e) {
    console.error('地图加载失败:', e)
    ElMessage.error('地图加载失败，请检查Key配置')
  }
}

// 获取数据
const fetchData = async (lng, lat) => {
  // 如果没有传坐标且地图存在，取地图中心
  if ((!lng || !lat) && map.value) {
    const center = map.value.getCenter()
    lng = center.getLng()
    lat = center.getLat()
  }
  // 默认坐标
  if (!lng) lng = 116.4074
  if (!lat) lat = 39.9042

  loading.value = true
  try {
    const params = {
      longitude: lng,
      latitude: lat,
      radius: 50,
      subject: filterForm.subject || searchQuery.value || null,
      grade: filterForm.grade || null,
      teachMode: filterForm.teachMode || null
    }

    let res = await findNearbyDemands(params)
    
    // 如果LBS搜索返回空，降级到列表接口获取所有上架需求
    if (res.code === 200 && (!res.data || res.data.length === 0)) {
      console.log('LBS搜索无结果，降级到列表接口')
      const listRes = await request.get('/demand/list', { params: { page: 1, size: 20, status: 1 } })
      if (listRes.code === 200 && listRes.data?.records) {
        students.value = listRes.data.records
      } else {
        students.value = []
      }
    } else if (res.code === 200) {
      students.value = res.data || []
    }
    
    // 仅在地图模式下渲染标记
    if (viewMode.value === 'map') {
      renderMarkers()
    }
  } catch (error) {
    console.error('获取数据失败', error)
  } finally {
    loading.value = false
  }
}

// 渲染地图标记
const renderMarkers = () => {
  if (!map.value || !window.AMap) return
  
  map.value.remove(markers)
  markers = []

  students.value.forEach(item => {
    if (!item.longitude || !item.latitude) return

    // 纯 HTML 样式的 Marker
    const markerContent = `
      <div style="background:#f56c6c; color:white; padding:4px 8px; border-radius:4px; font-size:12px; font-weight:bold; box-shadow:0 2px 4px rgba(0,0,0,0.3); white-space:nowrap;">
        ¥${item.expectPrice}
        <div style="position:absolute; left:50%; bottom:-6px; transform:translateX(-50%); border-left:6px solid transparent; border-right:6px solid transparent; border-top:6px solid #f56c6c;"></div>
      </div>`

    const marker = new window.AMap.Marker({
      position: [item.longitude, item.latitude],
      content: markerContent,
      offset: new window.AMap.Pixel(0, -30),
      extData: item
    })

    marker.on('click', (e) => {
      selectedDemand.value = e.target.getExtData()
    })

    markers.push(marker)
  })

  map.value.add(markers)
}

// 交互事件
const handleSearch = () => fetchData()

const handleViewChange = (val) => {
  if (val === 'map') {
    setTimeout(() => initMap(), 100)
  }
}

const locateUser = () => {
  if (map.value) ElMessage.info('正在定位...')
}

const refreshMapData = () => {
  handleSearch()
  ElMessage.success('已刷新')
}

const goDetail = (id) => {
  console.log('查看详情:', id)
  router.push(`/student/${id}`) // 修正为正确的路由路径 /student/:id
}

const formatDistance = (d) => {
  if (!d) return ''
  return d < 1 ? (d * 1000).toFixed(0) + 'm' : d.toFixed(1) + 'km'
}

const formatTime = (t) => t ? t.split('T')[0] : ''

onMounted(() => {
  fetchData()
})

onBeforeUnmount(() => {
  if (map.value) map.value.destroy()
})
</script>

<style scoped lang="scss">
.find-students-page {
  min-height: calc(100vh - 114px);
  background-color: #f5f7fa;
  padding-bottom: 40px;
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
}

/* 筛选侧边栏 */
.filter-card {
  border-radius: 8px;
  border: none;
  position: sticky;
  top: 20px;

  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }

  .search-btn {
    width: 100%;
    margin-top: 10px;
    font-weight: 600;
  }
}

/* 顶部工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  background: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);

  .total-count {
    color: #606266;
    b {
      color: #409eff;
      font-size: 16px;
    }
  }
}

.list-container {
  min-height: 400px;
}

.student-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.student-card {
  border-radius: 12px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    .grade-subject { font-weight: 600; font-size: 16px; color: #303133; }
    .price { color: #f56c6c; font-weight: 700; font-size: 16px; }
  }

  .card-body {
    .desc {
      font-size: 13px; color: #606266; margin-bottom: 8px; height: 36px; line-height: 1.4;
    }

    .tags { display: flex; gap: 6px; margin-bottom: 12px; }
  }

  .card-footer {
    display: flex; justify-content: space-between; font-size: 12px; color: #909399; border-top: 1px solid #f0f2f5; padding-top: 8px;
    .location { display: flex; align-items: center; gap: 4px; }
  }
}

.map-container {
  position: relative;
  width: 100%;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;

  .amap-box { width: 100%; height: 100%; }

  .map-controls {
    position: absolute; bottom: 120px; right: 16px; display: flex; flex-direction: column; gap: 8px; z-index: 100;
    .control-btn {
      width: 40px; height: 40px; background: #fff; border-radius: 8px; display: flex; align-items: center; justify-content: center;
      box-shadow: 0 2px 8px rgba(0,0,0,0.15); cursor: pointer; font-size: 20px; color: #606266;
      &:hover { color: #409eff; }
    }
  }

  .map-float-card {
    position: absolute; bottom: 20px; left: 16px; right: 16px; background: #fff; border-radius: 12px; padding: 16px;
    box-shadow: 0 4px 16px rgba(0,0,0,0.15); z-index: 100; max-width: 400px; margin: 0 auto;

    .float-header {
      display: flex; justify-content: space-between; margin-bottom: 8px;
      .title { font-weight: 700; font-size: 16px; }
      .close-btn { font-size: 20px; color: #909399; cursor: pointer; }
    }

    .float-content {
      margin-bottom: 12px;
      .price-dist {
        display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 4px;
        .price { color: #f56c6c; font-size: 18px; font-weight: 700; }
        .dist { font-size: 12px; color: #e6a23c; }
      }
      .address {
        font-size: 12px; color: #909399;
        white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
      }
    }
    .action-btn { width: 100%; }
  }
}

.line-clamp-2 {
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}

.slide-up-enter-active, .slide-up-leave-active { transition: all 0.3s ease-out; }
.slide-up-enter-from, .slide-up-leave-to { transform: translateY(100%); opacity: 0; }

/* 响应式调整 */
@media (max-width: 992px) {
  .filter-col {
    display: none;
  }

  .student-grid {
    grid-template-columns: 1fr;
  }
}
</style>