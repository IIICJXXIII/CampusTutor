<template>
  <div class="find-students">
    <div class="search-card">
      <div class="search-header">
        <div class="search-title-row">
          <el-icon class="search-icon"><Search /></el-icon>
          <span class="search-title">筛选条件</span>
          <el-tag v-if="hasActiveFilters" size="small" type="primary" effect="plain">
            {{ activeFilterCount }}项筛选
          </el-tag>
        </div>
        <div class="search-actions-top">
          <el-button
            :type="viewMode === 'map' ? 'default' : 'primary'"
            size="small"
            class="view-toggle-btn"
            @click="toggleView"
          >
            <el-icon><component :is="viewMode === 'map' ? 'List' : 'Location'" /></el-icon>
            {{ viewMode === 'map' ? '列表' : '地图' }}
          </el-button>
          <el-button
            v-if="hasActiveFilters"
            size="small"
            link
            type="primary"
            @click="resetFilters"
          >
            清除筛选
          </el-button>
        </div>
      </div>
      <el-divider class="search-divider" />
      <div class="search-filters">
        <el-select
          v-model="searchForm.subject"
          placeholder="全部科目"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <template #prefix>
            <el-icon><Reading /></el-icon>
          </template>
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
        <el-select
          v-model="searchForm.grade"
          placeholder="全部年级"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <template #prefix>
            <el-icon><User /></el-icon>
          </template>
          <el-option label="小学" value="小学" />
          <el-option label="初中" value="初中" />
          <el-option label="高中" value="高中" />
        </el-select>
        <el-select
          v-if="hasLocation"
          v-model="searchForm.radius"
          placeholder="距离范围"
          class="filter-select"
          @change="handleSearch"
        >
          <template #prefix>
            <el-icon><Location /></el-icon>
          </template>
          <el-option label="5公里内" :value="5" />
          <el-option label="10公里内" :value="10" />
          <el-option label="20公里内" :value="20" />
          <el-option label="50公里内" :value="50" />
          <el-option label="不限距离" :value="100" />
        </el-select>
        <div v-if="hasLocation" class="location-badge">
          <el-icon><Aim /></el-icon>
          <span class="location-text">已定位</span>
        </div>
      </div>
      <div class="search-footer">
        <div class="result-hint" v-if="demands.length > 0">
          找到 <strong>{{ demands.length }}</strong> 个需求
        </div>
        <el-button type="primary" @click="handleSearch" class="search-btn">
          <el-icon><Search /></el-icon>搜索
        </el-button>
      </div>
    </div>

    <div v-if="!hasLocation && !loading" class="no-location-tip">
      <el-alert
        title="未获取到您的位置信息"
        type="warning"
        :closable="false"
        show-icon
      >
        <template #default>
          <p>位置信息用于筛选附近需求，当前无法进行距离筛选。</p>
          <el-button
            type="primary"
            size="small"
            :loading="locating"
            @click="requestLocation"
            style="margin-top: 8px;"
          >
            {{ locating ? '定位中...' : '授权获取位置' }}
          </el-button>
        </template>
      </el-alert>
    </div>

    <div v-show="viewMode === 'map'" class="map-container">
      <div id="demand-map" class="demand-map"></div>
      <div class="map-controls">
        <el-button circle @click="relocate">
          <el-icon><Aim /></el-icon>
        </el-button>
      </div>
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
                <span class="price">¥{{ selectedDemand.expectPrice }}/小时</span>
              </el-descriptions-item>
              <el-descriptions-item label="距离">{{ selectedDemand.distanceStr }}</el-descriptions-item>
            </el-descriptions>
            <div class="popup-actions">
              <el-button @click="viewDetail(selectedDemand)">查看详情</el-button>
              <el-button @click="contactParent(selectedDemand)">联系家长</el-button>
              <el-button type="primary" @click="handleApply(selectedDemand)">申请接单</el-button>
            </div>
          </div>
        </div>
      </transition>
    </div>

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
              <span class="distance">{{ demand.distanceStr }}</span>
            </div>
            <h3 class="card-title">{{ demand.title || `${demand.grade}${demand.subject}辅导` }}</h3>
            <p class="card-desc">{{ demand.detail || '暂无描述' }}</p>
            <div class="card-info">
              <span><el-icon><User /></el-icon>{{ demand.grade }}</span>
              <span><el-icon><Clock /></el-icon>{{ demand.scheduleText }}</span>
            </div>
            <div class="card-footer">
              <span class="price">¥{{ demand.expectPrice }}<small>/小时</small></span>
              <el-button type="primary" size="small" @click.stop="handleApply(demand)">
                申请接单
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-empty v-if="demands.length === 0 && !loading && hasLocation" description="未找到相关需求">
        <template #image>
          <el-icon :size="64" color="#c0c4cc"><Search /></el-icon>
        </template>
        <p style="color: #909399; font-size: 14px; margin-top: 8px;">
          当前搜索范围内没有符合条件的需求，请尝试调整搜索条件或扩大搜索范围
        </p>
        <el-button type="primary" @click="searchForm.radius = 50; handleSearch()">
          扩大搜索范围至50公里
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, onUnmounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Location, List, Aim, Close, User, Clock, Reading } from '@element-plus/icons-vue'
import AMapLoader from '@amap/amap-jsapi-loader'
import { getNearbyDemands, applyForDemand } from '@shared/api/demand'
import { useUserStore } from '@shared/stores'
import { updateUserAddress } from '@shared/api/user'
import { reverseGeocode } from '@shared/api/map'

const router = useRouter()
const userStore = useUserStore()

const viewMode = ref('list')
const loading = ref(false)
const locating = ref(false)
const demands = ref([])
const selectedDemand = ref(null)
const dataLoaded = ref(false)

let map = null
let markers = []
let refreshTimer = null

const currentPosition = reactive({
  longitude: null,
  latitude: null
})

const hasLocation = computed(() =>
  currentPosition.longitude != null && currentPosition.latitude != null
)

const hasActiveFilters = computed(() =>
  searchForm.subject !== '' || searchForm.grade !== '' || searchForm.radius !== 20
)

const activeFilterCount = computed(() => {
  let count = 0
  if (searchForm.subject) count++
  if (searchForm.grade) count++
  if (searchForm.radius !== 20) count++
  return count
})

const resetFilters = () => {
  searchForm.subject = ''
  searchForm.grade = ''
  searchForm.radius = 20
  handleSearch()
}

const searchForm = reactive({
  subject: '',
  grade: '',
  radius: 20
})

watch(() => userStore.userInfo, (info) => {
  if (info?.longitude && info?.latitude) {
    if (currentPosition.longitude !== info.longitude || currentPosition.latitude !== info.latitude) {
      currentPosition.longitude = info.longitude
      currentPosition.latitude = info.latitude
      if (dataLoaded.value) {
        loadDemands(true)
      }
    }
  }
}, { deep: true })

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

const parseSchedule = (value) => {
  if (!value) return ''
  try {
    const arr = typeof value === 'string' ? JSON.parse(value) : value
    if (Array.isArray(arr)) return arr.join('，')
    return ''
  } catch {
    return ''
  }
}

const mapDemandItem = (item) => ({
  id: item.id,
  title: item.title,
  subject: item.subject,
  grade: item.grade,
  expectPrice: item.expectPrice,
  distanceStr: item.distance != null ? `${Number(item.distance).toFixed(1)}km` : '',
  detail: item.detail,
  scheduleText: parseSchedule(item.scheduleRequire),
  address: item.address,
  status: item.status,
  teachMode: item.teachMode,
  longitude: item.longitude,
  latitude: item.latitude,
  publisherId: item.publisherId
})

const loadDemands = async (resetPage = true) => {
  if (loading.value) return
  if (!hasLocation.value) {
    demands.value = []
    dataLoaded.value = true
    return
  }

  loading.value = true
  try {
    const params = {
      longitude: currentPosition.longitude,
      latitude: currentPosition.latitude,
      radius: searchForm.radius
    }
    if (searchForm.subject) params.subject = searchForm.subject
    if (searchForm.grade) params.grade = searchForm.grade

    let rawList = []

    try {
      const nearbyRes = await getNearbyDemands(params)
      if (nearbyRes.code === 200) {
        rawList = nearbyRes.data?.records ?? nearbyRes.data ?? []
      }
    } catch (e) {
      console.warn('附近需求搜索失败:', e)
    }

    const list = rawList.map(mapDemandItem)

    if (resetPage) {
      demands.value = list
    } else {
      const existingIds = new Set(demands.value.map(d => d.id))
      const newItems = list.filter(d => !existingIds.has(d.id))
      demands.value.push(...newItems)
    }
    dataLoaded.value = true

    if (viewMode.value === 'map' && map) {
      updateMapMarkers()
    }
  } catch (error) {
    console.error('加载需求失败:', error)
  } finally {
    loading.value = false
  }
}

const requestLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.warning('您的设备不支持定位功能')
    return
  }

  locating.value = true

  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const { longitude, latitude } = position.coords

      currentPosition.longitude = longitude
      currentPosition.latitude = latitude

      let address = `${longitude.toFixed(6)}, ${latitude.toFixed(6)}`
      try {
        const res = await reverseGeocode(latitude, longitude)
        if (res.code === 200 && res.data) {
          address = res.data.formattedAddress || res.data.address || address
        }
      } catch (e) {
        console.warn('逆地理编码失败:', e)
      }

      try {
        await updateUserAddress({ longitude, latitude, address })
      } catch (e) {
        console.warn('更新用户地址失败:', e)
      }

      userStore.setUserInfo({
        ...userStore.userInfo,
        longitude,
        latitude,
        address
      })
      localStorage.setItem('locationPermission', 'granted')

      locating.value = false
      ElMessage.success('位置获取成功')
      loadDemands(true)
    },
    (error) => {
      locating.value = false
      if (error.code === error.PERMISSION_DENIED) {
        ElMessage.warning('您拒绝了位置授权，无法进行距离筛选')
        localStorage.setItem('locationPermission', 'denied')
      } else if (error.code === error.POSITION_UNAVAILABLE) {
        ElMessage.warning('无法获取位置信息，请检查设备定位服务是否开启')
      } else if (error.code === error.TIMEOUT) {
        ElMessage.warning('获取位置超时，请稍后重试')
      } else {
        ElMessage.warning('获取位置失败，请稍后重试')
      }
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 60000 }
  )
}

const initMap = async () => {
  if (!hasLocation.value) {
    ElMessage.warning('请先授权获取位置信息后再使用地图模式')
    return
  }

  try {
    window._AMapSecurityConfig = {
      securityJsCode: import.meta.env.VITE_AMAP_SECURITY_CODE,
    }

    const AMap = await AMapLoader.load({
      key: import.meta.env.VITE_AMAP_KEY || 'YOUR_AMAP_KEY',
      version: '2.0',
      plugins: ['AMap.Geolocation', 'AMap.Marker']
    })

    map = new AMap.Map('demand-map', {
      zoom: 13,
      center: [currentPosition.longitude, currentPosition.latitude],
      resizeEnable: true
    })

    map.on('zoomend', () => {
      updateMapMarkers()
    })

    map.on('moveend', () => {
      updateMapMarkers()
    })

    const geolocation = new AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000
    })

    geolocation.getCurrentPosition((status, result) => {
      if (status === 'complete') {
        const newLng = result.position.lng
        const newLat = result.position.lat
        const posChanged =
          Math.abs(newLng - currentPosition.longitude) > 0.001 ||
          Math.abs(newLat - currentPosition.latitude) > 0.001

        currentPosition.longitude = newLng
        currentPosition.latitude = newLat
        map.setCenter([newLng, newLat])

        if (posChanged) {
          loadDemands(true)
        } else {
          updateMapMarkers()
        }
      } else {
        updateMapMarkers()
      }
    })
  } catch (error) {
    console.error('地图加载失败:', error)
  }
}

const updateMapMarkers = () => {
  if (!map) return

  markers.forEach(m => map.remove(m))
  markers = []

  const AMap = window.AMap
  if (!AMap) return

  demands.value.forEach(demand => {
    if (demand.longitude && demand.latitude) {
      const marker = new AMap.Marker({
        position: [demand.longitude, demand.latitude],
        title: demand.title || demand.subject,
        offset: new AMap.Pixel(-40, -18),
        anchor: 'bottom-center',
        content: `<div style="background:#409eff;color:#fff;padding:2px 8px;border-radius:10px;font-size:12px;white-space:nowrap;transform:translateX(-50%);pointer-events:auto;cursor:pointer;">¥${demand.expectPrice || '?'} ${demand.subject} ${demand.grade || ''}</div>`,
        zooms: [3, 20]
      })

      marker.on('click', () => {
        selectedDemand.value = demand
      })

      map.add(marker)
      markers.push(marker)
    }
  })
}

const handleSearch = () => {
  loadDemands(true)
}

const toggleView = () => {
  viewMode.value = viewMode.value === 'map' ? 'list' : 'map'
  if (viewMode.value === 'map') {
    if (!hasLocation.value) {
      ElMessage.warning('请先授权获取位置信息后再使用地图模式')
      viewMode.value = 'list'
      return
    }
    if (!map) {
      setTimeout(() => initMap(), 100)
    } else {
      updateMapMarkers()
    }
  }
}

const relocate = () => {
  if (map && hasLocation.value) {
    map.setCenter([currentPosition.longitude, currentPosition.latitude])
    loadDemands(true)
  }
}

const viewDetail = (demand) => {
  router.push(`/teacher/demand/${demand.id}`)
}

const contactParent = (demand) => {
  if (demand.publisherId) {
    router.push(`/chat/${demand.publisherId}`)
  } else {
    ElMessage.info('暂无家长联系信息，请通过查看详情联系')
  }
}

const handleApply = async (demand) => {
  try {
    await ElMessageBox.confirm(
      `确定要申请接单「${demand.subject}辅导」吗？提交后需等待家长审核。`,
      '申请接单',
      { confirmButtonText: '确定申请', cancelButtonText: '取消', type: 'info' }
    )

    const res = await applyForDemand(demand.id, {
      totalHours: 10,
      remark: ''
    })
    if (res.code === 200) {
      ElMessage.success('申请已提交，请等待家长审核')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '申请失败')
    }
  }
}

const startAutoRefresh = () => {
  if (refreshTimer) return
  refreshTimer = setInterval(() => {
    if (!loading.value && dataLoaded.value && hasLocation.value) {
      loadDemands(true)
    }
  }, 30000)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const initPosition = async () => {
  const stored = userStore.userInfo
  if (stored?.longitude && stored?.latitude) {
    currentPosition.longitude = stored.longitude
    currentPosition.latitude = stored.latitude
    return
  }

  const permission = localStorage.getItem('locationPermission')
  if (permission !== 'granted') {
    currentPosition.longitude = null
    currentPosition.latitude = null
    return
  }

  if (!navigator.geolocation) {
    currentPosition.longitude = null
    currentPosition.latitude = null
    return
  }

  return new Promise((resolve) => {
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        currentPosition.longitude = pos.coords.longitude
        currentPosition.latitude = pos.coords.latitude
        resolve()
      },
      () => {
        currentPosition.longitude = null
        currentPosition.latitude = null
        resolve()
      },
      { enableHighAccuracy: true, timeout: 5000, maximumAge: 60000 }
    )
  })
}

onMounted(async () => {
  await initPosition()
  loadDemands(true)
  startAutoRefresh()
})

onActivated(() => {
  const stored = userStore.userInfo
  if (stored?.longitude && stored?.latitude) {
    if (currentPosition.longitude !== stored.longitude || currentPosition.latitude !== stored.latitude) {
      currentPosition.longitude = stored.longitude
      currentPosition.latitude = stored.latitude
    }
  }
  if (dataLoaded.value) {
    loadDemands(true)
  }
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
  if (map) {
    map.destroy()
    map = null
  }
})
</script>

<style lang="scss" scoped>
.find-students {
  .search-card {
    background: #fff;
    border-radius: 16px;
    margin-bottom: 16px;
    padding: 20px 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid #f0f0f0;

    .search-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .search-title-row {
        display: flex;
        align-items: center;
        gap: 8px;

        .search-icon {
          font-size: 18px;
          color: var(--el-color-primary);
        }

        .search-title {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
        }
      }

      .search-actions-top {
        display: flex;
        align-items: center;
        gap: 8px;

        .view-toggle-btn {
          border-radius: 20px;
          font-size: 13px;
          padding: 6px 14px;
        }
      }
    }

    .search-divider {
      margin: 14px 0;
    }

    .search-filters {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: center;

      .filter-select {
        min-width: 180px;
        flex: 0 0 auto;

        :deep(.el-input__wrapper) {
          border-radius: 10px;
          background: #f8f9fb;
          border: 1px solid #e8eaed;
          box-shadow: none;
          transition: all 0.2s;

          &:hover {
            border-color: var(--el-color-primary-light-3);
            background: #fff;
          }

          &.is-focus {
            border-color: var(--el-color-primary);
            background: #fff;
            box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
          }
        }

        :deep(.el-input__prefix) {
          color: #a8abb2;
          margin-right: 4px;
        }
      }

      .location-badge {
        display: flex;
        align-items: center;
        gap: 4px;
        padding: 6px 12px;
        background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
        border-radius: 20px;
        font-size: 13px;
        color: #388e3c;

        .location-text {
          font-weight: 500;
        }
      }
    }

    .search-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 16px;

      .result-hint {
        font-size: 14px;
        color: #606266;

        strong {
          color: var(--el-color-primary);
          font-size: 16px;
        }
      }

      .search-btn {
        border-radius: 10px;
        padding: 10px 28px;
        font-size: 14px;
        font-weight: 500;
        box-shadow: 0 2px 8px rgba(var(--el-color-primary-rgb, 64, 158, 255), 0.3);
        transition: all 0.2s;

        &:hover {
          transform: translateY(-1px);
          box-shadow: 0 4px 14px rgba(var(--el-color-primary-rgb, 64, 158, 255), 0.4);
        }
      }
    }

    @media (max-width: 768px) {
      padding: 16px;
      border-radius: 12px;

      .search-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 10px;

        .search-actions-top {
          width: 100%;
          justify-content: space-between;
        }
      }

      .search-filters {
        flex-direction: column;
        gap: 10px;

        .filter-select {
          width: 100%;
          min-width: unset;
        }

        .location-badge {
          width: 100%;
          justify-content: center;
        }
      }

      .search-footer {
        flex-direction: column;
        gap: 12px;

        .search-btn {
          width: 100%;
        }
      }
    }
  }

  .no-location-tip {
    margin-bottom: 16px;

    :deep(.el-alert) {
      border-radius: 12px;
      border: none;
      background: linear-gradient(135deg, #fff7e6 0%, #fff3d6 100%);
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
      border-radius: 14px;
      padding: 18px;
      margin-bottom: 16px;
      cursor: pointer;
      transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      border: 1px solid #f0f0f0;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 28px rgba(0, 0, 0, 0.1);
        border-color: var(--el-color-primary-light-7);

        .card-footer .el-button {
          box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb, 64, 158, 255), 0.35);
        }
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        :deep(.el-tag) {
          border-radius: 6px;
          font-weight: 500;
        }

        .distance {
          font-size: 12px;
          color: #909399;
          background: #f5f7fa;
          padding: 2px 10px;
          border-radius: 10px;
        }
      }

      .card-title {
        font-size: 16px;
        font-weight: 600;
        margin-bottom: 8px;
        color: #303133;
        line-height: 1.4;
      }

      .card-desc {
        font-size: 13px;
        color: #909399;
        margin-bottom: 14px;
        line-height: 1.6;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .card-info {
        display: flex;
        flex-wrap: wrap;
        gap: 14px;
        font-size: 13px;
        color: #606266;
        margin-bottom: 14px;
        padding: 10px 12px;
        background: #fafbfc;
        border-radius: 8px;

        span {
          display: flex;
          align-items: center;
          gap: 5px;

          .el-icon {
            color: #a8abb2;
            font-size: 15px;
          }
        }
      }

      .card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 12px;
        border-top: 1px solid #f5f5f5;

        .price {
          font-size: 22px;
          font-weight: 700;
          color: #f56c6c;
          letter-spacing: -0.5px;

          small {
            font-size: 12px;
            font-weight: 400;
            color: #909399;
          }
        }

        :deep(.el-button) {
          border-radius: 8px;
          font-weight: 500;
          padding: 8px 18px;
        }
      }
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
