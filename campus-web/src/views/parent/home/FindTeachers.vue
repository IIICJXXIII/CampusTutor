<template>
  <div class="find-teachers-page">
    <div class="page-header">
      <h1 class="page-title">找老师</h1>
    </div>
    
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索老师、科目、学校..."
        :prefix-icon="Search"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="toggleView">
        <el-icon><component :is="viewMode === 'map' ? 'List' : 'Location'" /></el-icon>
        {{ viewMode === 'map' ? '列表模式' : '地图找老师' }}
      </el-button>
    </div>
    
    <!-- 筛选条件 -->
    <div class="filter-bar">
      <el-select v-model="filters.subject" placeholder="科目" clearable @change="loadTutors">
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
      
      <el-select v-model="filters.grade" placeholder="年级" clearable @change="loadTutors">
        <el-option label="小学" value="小学" />
        <el-option label="初中" value="初中" />
        <el-option label="高中" value="高中" />
      </el-select>
      
      <el-select v-model="filters.gender" placeholder="性别" clearable @change="loadTutors">
        <el-option label="男" :value="1" />
        <el-option label="女" :value="2" />
      </el-select>
      
      <el-select v-model="filters.sort" placeholder="排序" @change="loadTutors">
        <el-option label="综合排序" value="default" />
        <el-option label="评分最高" value="rating" />
        <el-option label="课时最多" value="hours" />
        <el-option label="价格最低" value="price_asc" />
        <el-option label="价格最高" value="price_desc" />
      </el-select>
    </div>
    
    <!-- 地图视图 -->
    <div v-if="viewMode === 'map'" class="map-section">
      <div id="tutor-map" class="tutor-map"></div>
      <div class="map-controls">
        <el-button circle size="small" @click="relocate">
          <el-icon><Aim /></el-icon>
        </el-button>
      </div>
      <!-- 地图上选中教师弹窗 -->
      <transition name="slide-up">
        <div v-if="selectedTutor" class="tutor-popup" @click="viewDetail(selectedTutor.userId)">
          <div class="popup-header">
            <el-avatar :size="48" :src="selectedTutor.avatar">{{ selectedTutor.name?.charAt(0) }}</el-avatar>
            <div>
              <div class="popup-name">{{ selectedTutor.name }}</div>
              <div class="popup-school">{{ selectedTutor.university }}</div>
            </div>
            <el-icon class="popup-close" @click.stop="selectedTutor = null"><Close /></el-icon>
          </div>
          <div class="popup-tags">
            <el-tag v-for="s in (selectedTutor.subjects || []).slice(0, 3)" :key="s" size="small">{{ s }}</el-tag>
          </div>
          <div class="popup-footer">
            <span class="popup-price">¥{{ selectedTutor.minPrice || 60 }}/时起</span>
            <el-button type="primary" size="small" @click.stop="contactTutor(selectedTutor)">联系老师</el-button>
          </div>
        </div>
      </transition>
    </div>

    <!-- 教师列表 -->
    <div v-if="loading && viewMode === 'list'" class="loading-container">
      <el-skeleton :rows="4" animated />
    </div>
    
    <div v-else-if="tutors.length === 0 && viewMode === 'list'" class="empty-container">
      <el-empty description="暂无符合条件的老师">
        <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
      </el-empty>
    </div>
    
    <div v-else-if="viewMode === 'list'" class="tutor-list">
      <div
        v-for="tutor in tutors"
        :key="tutor.id"
        class="tutor-card"
        @click="viewDetail(tutor.userId)"
      >
        <!-- 推荐标签 -->
        <div v-if="tutor.matchScore > 70 || (tutor.matchTags && tutor.matchTags.length)" class="recommend-badge">
          <el-tag type="warning" size="small" effect="dark">
            <el-icon><MagicStick /></el-icon>
            {{ tutor.matchScore ? `匹配 ${Math.round(tutor.matchScore)}%` : '智能推荐' }}
          </el-tag>
        </div>
        <div class="tutor-avatar">
          <el-avatar :size="72" :src="tutor.avatar">
            {{ tutor.name?.charAt(0) }}
          </el-avatar>
          <el-tag v-if="tutor.verified" type="success" size="small" class="verified-tag">
            已认证
          </el-tag>
        </div>
        
        <div class="tutor-info">
          <div class="tutor-name">
            {{ tutor.name }}
            <el-tag size="small" :type="tutor.gender === 1 ? 'primary' : 'danger'">
              {{ tutor.gender === 1 ? '男' : '女' }}
            </el-tag>
          </div>
          <div class="tutor-school">{{ tutor.university }} · {{ tutor.major }}</div>
          <div class="tutor-subjects">
            <el-tag 
              v-for="subject in (tutor.subjects || []).slice(0, 4)" 
              :key="subject"
              size="small"
              type="info"
            >
              {{ subject }}
            </el-tag>
          </div>
          <div class="tutor-stats">
            <span class="stat-item">
              <el-icon><Star /></el-icon>
              {{ (tutor.rating || 5).toFixed(1) }}分
            </span>
            <span class="stat-item">
              <el-icon><Clock /></el-icon>
              {{ tutor.totalHours || 0 }}小时
            </span>
            <span class="stat-item price">
              ¥{{ tutor.minPrice || 60 }}-{{ tutor.maxPrice || 120 }}/时
            </span>
          </div>
        </div>
        
        <div class="tutor-actions">
          <el-button type="primary" size="small" @click.stop="contactTutor(tutor)">
            联系老师
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination-container">
      <el-pagination
        v-model:current-page="page"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadTutors"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Star, Clock, Location, List, Aim, Close, MagicStick } from '@element-plus/icons-vue'
import { getTutorList } from '@shared/api/match'
import { recordSearch } from '@shared/api/behavior'

const router = useRouter()
const loading = ref(false)
const tutors = ref([])
const searchKeyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const viewMode = ref('list') // 'list' | 'map'
const selectedTutor = ref(null)

let map = null
let markers = []
let AMap = null

const filters = reactive({
  subject: '',
  grade: '',
  gender: '',
  sort: 'default'
})

const loadTutors = async () => {
  loading.value = true
  try {
    // 处理排序参数
    let sortBy = undefined
    let sortOrder = undefined
    if (filters.sort) {
      if (filters.sort === 'rating') {
        sortBy = 'rating'
        sortOrder = 'desc'
      } else if (filters.sort === 'hours') {
        sortBy = 'orderCount' // 暂时用订单数代替课时数
        sortOrder = 'desc'
      } else if (filters.sort === 'price_asc') {
        sortBy = 'price'
        sortOrder = 'asc'
      } else if (filters.sort === 'price_desc') {
        sortBy = 'price'
        sortOrder = 'desc'
      } else {
        sortBy = 'score'
        sortOrder = 'desc'
      }
    }

    const params = {
      page: page.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined,
      subject: filters.subject || undefined,
      grade: filters.grade || undefined,
      gender: filters.gender || undefined,
      sortBy: sortBy,
      sortOrder: sortOrder
    }
    
    const res = await getTutorList(params)
    if (res.code === 200) {
      const rawRecords = res.data?.records || []
      // 后端字段 -> 前端展示字段映射
      tutors.value = rawRecords.map(t => ({
        ...t,
        name: t.realName || t.name,
        avatar: t.avatarUrl || t.avatar,
        university: t.universityName || t.university,
        subjects: t.teachSubjects || t.subjects || [],
        grades: t.teachGrades || t.grades || [],
        minPrice: t.expectPrice || t.minPrice || 60,
        maxPrice: t.expectPrice || t.maxPrice || 120,
        verified: t.certStatus === 2 || t.verified,
        totalHours: t.totalHours || 0,
        rating: t.rating || 5,
        gender: t.gender,
        matchScore: t.matchScore,
        matchTags: t.matchTags
      }))
      total.value = res.data?.total || 0
    }
  } catch (error) {
    console.error('加载教师列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadTutors()
  recordSearch().catch(() => {})
}

const resetFilters = () => {
  searchKeyword.value = ''
  filters.subject = ''
  filters.grade = ''
  filters.gender = ''
  filters.sort = 'default'
  page.value = 1
  loadTutors()
}

const viewDetail = (userId) => {
  router.push(`/parent/teachers/${userId}`)
}

const contactTutor = (tutor) => {
  router.push(`/chat/${tutor.userId}`)
}

// ========== 地图相关 ==========
const toggleView = async () => {
  viewMode.value = viewMode.value === 'list' ? 'map' : 'list'
  if (viewMode.value === 'map') {
    await nextTick()
    initMap()
  }
}

const initMap = async () => {
  if (map) {
    addMarkersToMap()
    return
  }
  const amapKey = import.meta.env.VITE_AMAP_KEY
  const securityCode = import.meta.env.VITE_AMAP_SECURITY_CODE // 👈 新增：读取安全密钥

  if (!amapKey || amapKey === 'YOUR_AMAP_KEY') {
    ElMessage.warning('地图功能需要配置高德地图 Key，请在 .env 文件中设置 VITE_AMAP_KEY')
    viewMode.value = 'list'
    return
  }

  // 🚨 核心修复：在真正加载高德 SDK 之前，必须在全局注入安全密钥！
  window._AMapSecurityConfig = {
    securityJsCode: securityCode,
  }

  try {
    const AMapLoader = (await import('@amap/amap-jsapi-loader')).default
    AMap = await AMapLoader.load({
      key: amapKey,
      version: '2.0',
      plugins: ['AMap.Geolocation', 'AMap.Marker']
    })
    const userStore = (await import('@shared/stores')).useUserStore()
    const storedLng = userStore.userInfo?.longitude
    const storedLat = userStore.userInfo?.latitude
    const centerLng = storedLng || 112.938888
    const centerLat = storedLat || 28.228333

    map = new AMap.Map('tutor-map', {
      zoom: 13,
      center: [centerLng, centerLat]
    })
    // 尝试获取当前位置
    const geo = new AMap.Geolocation({ enableHighAccuracy: true, timeout: 8000 })
    geo.getCurrentPosition((status, result) => {
      if (status === 'complete') {
        map.setCenter([result.position.lng, result.position.lat])
      }
    })
    addMarkersToMap()
  } catch (e) {
    console.error('地图加载失败:', e)
    ElMessage.error('地图加载失败，请检查网络或地图 Key 配置')
    viewMode.value = 'list'
  }
}

const addMarkersToMap = () => {
  if (!map || !AMap) return
  // 清除旧标记
  markers.forEach(m => map.remove(m))
  markers = []
  const center = map.getCenter()
  tutors.value.forEach(tutor => {
    // 优先使用后端返回的真实经纬度，若无则在中心点附近随机偏移
    const lng = tutor.longitude || (center.lng + (Math.random() - 0.5) * 0.04)
    const lat = tutor.latitude || (center.lat + (Math.random() - 0.5) * 0.04)
    const marker = new AMap.Marker({
      position: [lng, lat],
      title: tutor.realName || tutor.name
    })
    marker.on('click', () => {
      selectedTutor.value = {
        ...tutor,
        name: tutor.realName || tutor.name,
        avatar: tutor.avatarUrl,
        university: tutor.universityName,
        subjects: tutor.teachSubjects,
        minPrice: tutor.expectPrice
      }
    })
    map.add(marker)
    markers.push(marker)
  })
}

const relocate = () => {
  if (!map || !AMap) return
  const geo = new AMap.Geolocation({ enableHighAccuracy: true, timeout: 8000 })
  geo.getCurrentPosition((status, result) => {
    if (status === 'complete') {
      map.setCenter([result.position.lng, result.position.lat])
    }
  })
}

// 当教师列表更新时，刷新地图标记
watch(tutors, () => {
  if (viewMode.value === 'map' && map) addMarkersToMap()
})

onMounted(() => {
  loadTutors()
})

onUnmounted(() => {
  if (map) { map.destroy(); map = null }
})
</script>

<style lang="scss" scoped>
.find-teachers-page {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0;
  }
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  
  .el-input {
    flex: 1;
  }
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
  
  .el-select {
    width: 120px;
  }
}

.loading-container,
.empty-container {
  padding: 60px 0;
}

.tutor-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tutor-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    transform: translateY(-2px);
  }
}

.tutor-avatar {
  position: relative;
  
  .verified-tag {
    position: absolute;
    bottom: -4px;
    left: 50%;
    transform: translateX(-50%);
    white-space: nowrap;
  }
}

.tutor-info {
  flex: 1;
  min-width: 0;
  
  .tutor-name {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 6px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .tutor-school {
    font-size: 14px;
    color: #666;
    margin-bottom: 8px;
  }
  
  .tutor-subjects {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 10px;
  }
  
  .tutor-stats {
    display: flex;
    gap: 16px;
    font-size: 13px;
    color: #666;
    
    .stat-item {
      display: flex;
      align-items: center;
      gap: 4px;
    }
    
    .price {
      color: #f56c6c;
      font-weight: 600;
    }
  }
}

.tutor-actions {
  display: flex;
  align-items: center;
}

.recommend-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 1;
  .el-icon { margin-right: 2px; vertical-align: middle; }
}

.tutor-card {
  position: relative;
}

// ========== 地图视图 ==========
.map-section {
  position: relative;
  margin-bottom: 24px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.tutor-map {
  width: 100%;
  height: 500px;
}

.map-controls {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
}

.tutor-popup {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-radius: 16px 16px 0 0;
  padding: 16px 20px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.12);
  z-index: 20;
  cursor: pointer;

  .popup-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 10px;

    .popup-name { font-size: 16px; font-weight: 600; }
    .popup-school { font-size: 13px; color: #999; }
    .popup-close { margin-left: auto; cursor: pointer; font-size: 18px; color: #999; }
  }

  .popup-tags {
    display: flex;
    gap: 6px;
    margin-bottom: 10px;
  }

  .popup-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .popup-price { color: #f56c6c; font-weight: 600; font-size: 16px; }
  }
}

.slide-up-enter-active, .slide-up-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}
.slide-up-enter-from, .slide-up-leave-to {
  transform: translateY(100%);
  opacity: 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
