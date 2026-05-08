<template>
  <div class="checkin-page">
    <el-page-header @back="goBack">
      <template #content>课时打卡</template>
    </el-page-header>
    
    <div class="checkin-container" v-loading="loading">
      <!-- 课程信息 -->
      <div v-if="order" class="lesson-info-card">
        <div class="lesson-header">
          <h3>{{ order.studentName }} - {{ order.subject }}</h3>
          <el-tag :type="viewState === 'FINISHED' ? 'success' : 'warning'">
            {{ statusText }}
          </el-tag>
        </div>
        <div class="lesson-meta">
          <p><el-icon><Clock /></el-icon>已上课时：{{ order.usedHours }} / {{ order.totalHours }} 小时</p>
          <p><el-icon><Location /></el-icon>{{ order.address || '线上授课 / 待定地址' }}</p>
        </div>
      </div>
      
      <!-- 状态：尚未开展打卡 -->
      <template v-if="viewState === 'PENDING_CHECKIN'">
        <div class="location-card">
          <h4>课时打卡（获取当前位置）</h4>
          <div v-if="locationLoading" class="location-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在获取位置...</span>
          </div>
          <div v-else-if="currentLocation" class="location-info">
            <el-icon><Location /></el-icon>
            <span>{{ currentLocation.address }}</span>
          </div>
          <div v-else class="location-error">
            <el-icon><WarningFilled /></el-icon>
            <span>{{ locationError || '无法获取位置' }}</span>
            <el-button type="primary" link @click="getLocation">重新获取</el-button>
          </div>
          <div id="checkin-map" class="map-container"></div>
        </div>
        
        <div class="photo-card">
          <h4>签到拍照 <el-tag type="danger" size="small">必填</el-tag></h4>
          <div class="photo-upload">
            <input
              ref="photoInput"
              type="file"
              accept="image/*"
              capture="environment"
              style="display:none"
              @change="onPhotoSelected"
            />
            <div v-if="!photoPreview" class="photo-placeholder" @click="$refs.photoInput.click()">
              <el-icon :size="40"><Camera /></el-icon>
              <span>点击拍照或选择照片</span>
            </div>
            <div v-else class="photo-preview" @click="$refs.photoInput.click()">
              <img :src="photoPreview" alt="签到照片" />
              <el-button size="small" type="danger" circle class="retake-btn" @click.stop="clearPhoto">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <div class="checkin-action">
          <el-button type="primary" size="large" :loading="submitting" :disabled="!currentLocation" @click="handleCheckIn">
            <el-icon><Check /></el-icon> 上课打卡
          </el-button>
        </div>
      </template>

      <!-- 状态：上课中，等待下课 -->
      <template v-else-if="viewState === 'PENDING_CHECKOUT'">
        <div class="checkout-card location-card">
          <h4>结课打卡（当前上课中）</h4>
          <p style="font-size:14px;color:#666;margin-bottom:16px;">请在课程结束后填写本次课程摘要并完成打卡。</p>
          <el-form :model="checkoutForm" label-position="top">
            <el-form-item label="课程摘要（必填）">
              <el-input v-model="checkoutForm.contentSummary" type="textarea" :rows="4" placeholder="请输入本次课程的教学内容" />
            </el-form-item>
            <el-form-item label="布置作业（选填）">
              <el-input v-model="checkoutForm.homeworkAssigned" type="textarea" :rows="2" placeholder="请输入布置的作业内容" />
            </el-form-item>
          </el-form>
        </div>
        <div class="checkin-action">
          <el-button type="success" size="large" :loading="submitting" @click="handleCheckOut">
            <el-icon><Check /></el-icon> 结课打卡
          </el-button>
        </div>
      </template>

      <!-- 状态：等待家长确认 -->
      <template v-else-if="viewState === 'WAITING_CONFIRM'">
        <el-empty description="上一节课正在等待家长确认，暂无法进行新的打卡" />
        <div class="checkin-action" style="text-align:center;">
          <el-button @click="goBack">返回订单</el-button>
        </div>
      </template>
      
      <!-- 状态：课时已满 -->
      <template v-else-if="viewState === 'FINISHED'">
        <el-empty description="该订单所有课时已完成，无需继续打卡" />
        <div class="checkin-action" style="text-align:center;">
          <el-button @click="goBack">返回订单</el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Location, Check, Loading, WarningFilled, Camera, Close } from '@element-plus/icons-vue'
import { getOrderLessons, checkIn, checkOut } from '@shared/api/teaching'
import { getOrderDetail } from '@shared/api/order'
import AMapLoader from '@amap/amap-jsapi-loader'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const viewState = ref('LOADING') // PENDING_CHECKIN, PENDING_CHECKOUT, WAITING_CONFIRM, FINISHED
const order = ref(null)
const activeRecord = ref(null)

const locationLoading = ref(true)
const locationError = ref('')
const currentLocation = ref(null)
const submitting = ref(false)
const photoInput = ref(null)
const photoFile = ref(null)
const photoPreview = ref(null)

const checkoutForm = reactive({
  contentSummary: '',
  homeworkAssigned: ''
})

let map = null
let marker = null

const statusText = computed(() => {
  const map = {
    'PENDING_CHECKIN': '待上课打卡',
    'PENDING_CHECKOUT': '上课中',
    'WAITING_CONFIRM': '待家长确认',
    'FINISHED': '课时已满'
  }
  return map[viewState.value] || '未知状态'
})

const loadData = async () => {
  const selectedOrderId = route.query.orderId
  if (!selectedOrderId) {
    ElMessage.warning('缺少订单信息')
    return
  }
  
  loading.value = true
  try {
    // 1. 加载订单
    const orderRes = await getOrderDetail(selectedOrderId)
    if (orderRes.code === 200) {
      order.value = orderRes.data
    }
    
    // 2. 加载课时记录
    const lessonsRes = await getOrderLessons(selectedOrderId)
    const lessons = lessonsRes.data || []

    // 3. 状态推导（使用明确的状态码）
    if (order.value && order.value.usedHours >= order.value.totalHours) {
      viewState.value = 'FINISHED'
    } else {
      const pendingStart = lessons.find(l => l.status === 0) // 待上课
      const inProgress = lessons.find(l => l.status === 1)    // 上课中
      const pendingConfirm = lessons.find(l => l.status === 2) // 待确认

      if (inProgress) {
        activeRecord.value = inProgress
        viewState.value = 'PENDING_CHECKOUT'
      } else if (pendingStart) {
        activeRecord.value = pendingStart
        viewState.value = 'PENDING_CHECKIN'
      } else if (pendingConfirm) {
        activeRecord.value = pendingConfirm
        viewState.value = 'WAITING_CONFIRM'
      } else {
        viewState.value = 'PENDING_CHECKIN'
      }
    }
    
    // 4. 如果是打卡态，加载地图
    if (viewState.value === 'PENDING_CHECKIN') {
      setTimeout(() => {
        initMap()
      }, 500) // 等待DOM渲染
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加载失败，可能无权限浏览此订单')
  } finally {
    loading.value = false
  }
}

const initMap = async () => {
  try {
    const amapKey = import.meta.env.VITE_AMAP_KEY
    if (!amapKey) {
      ElMessage.warning('地图功能需要配置高德地图 Key')
      getLocationFallback()
      return
    }
    const AMap = await AMapLoader.load({
      key: amapKey,
      version: '2.0',
      plugins: ['AMap.Geolocation', 'AMap.Geocoder']
    })
    
    map = new AMap.Map('checkin-map', {
      zoom: 15,
      center: [116.397428, 39.90923]
    })
    
    getLocation()
  } catch (error) {
    console.error('地图加载失败', error)
    getLocationFallback()
  }
}

const getLocation = () => {
  locationLoading.value = true
  locationError.value = ''
  
  if (window.AMap && window.AMap.Geolocation) {
    const geolocation = new window.AMap.Geolocation({
      enableHighAccuracy: true,
      timeout: 10000,
    })
    geolocation.getCurrentPosition((status, result) => {
      if (status === 'complete') {
        const { position } = result
        updateLocationData(position.lng, position.lat)
      } else {
        getLocationFallback()
      }
    })
  } else {
     getLocationFallback()
  }
}

const getLocationFallback = () => {
  if (!navigator.geolocation) {
    locationError.value = '浏览器不支持定位'
    applyMockLocation()
    return
  }
  
  navigator.geolocation.getCurrentPosition(
    (position) => {
      updateLocationData(position.coords.longitude, position.coords.latitude)
    },
    (error) => {
      console.error('浏览器原生定位失败', error)
      applyMockLocation()
    },
    { enableHighAccuracy: true, timeout: 5000 }
  )
}

const applyMockLocation = () => {
  locationLoading.value = false
  currentLocation.value = {
    lng: 116.397428,
    lat: 39.90923,
    address: '模拟地址位置 (暂无有效GPS配置)'
  }
  ElMessage.warning('实际定位失败，已使用模拟坐标以便打卡测试')
}

const updateLocationData = (lng, lat) => {
  currentLocation.value = {
    lng: lng,
    lat: lat,
    address: '正在解析地址...'
  }
  
  if (map) {
    map.setCenter([lng, lat])
    if (!marker) {
      marker = new window.AMap.Marker({
        position: [lng, lat]
      })
      map.add(marker)
    } else {
      marker.setPosition([lng, lat])
    }
  }

  // 逆地理编码获取真实Address
  if (window.AMap && window.AMap.Geocoder) {
    const geocoder = new window.AMap.Geocoder()
    geocoder.getAddress([lng, lat], (status, result) => {
      if (status === 'complete' && result.regeocode) {
        currentLocation.value.address = result.regeocode.formattedAddress
      } else {
         currentLocation.value.address = '位置获取成功，但解析地址失败'
      }
    })
  } else {
    currentLocation.value.address = '位置获取成功'
  }
  locationLoading.value = false
}

const onPhotoSelected = (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  photoFile.value = file
  const reader = new FileReader()
  reader.onload = (e) => { photoPreview.value = e.target.result }
  reader.readAsDataURL(file)
}

const clearPhoto = () => {
  photoFile.value = null
  photoPreview.value = null
  if (photoInput.value) photoInput.value.value = ''
}

const handleCheckIn = async () => {
  if (!photoFile.value) {
    ElMessage.warning('请拍摄现场照片')
    return
  }
  submitting.value = true
  try {
    const formData = new FormData()
    formData.append('orderId', order.value.id)
    formData.append('latitude', currentLocation.value?.lat || 0)
    formData.append('longitude', currentLocation.value?.lng || 0)
    formData.append('address', currentLocation.value?.address || '')
    formData.append('photo', photoFile.value)

    const res = await checkIn(formData)

    if (res.code === 200) {
      ElMessage.success('上课打卡成功')
      loadData()
    }
  } catch (error) {
    ElMessage.error(error.message || '签到失败')
  } finally {
    submitting.value = false
  }
}

const handleCheckOut = async () => {
  if (!checkoutForm.contentSummary) {
    ElMessage.warning('请输入课程摘要')
    return
  }

  submitting.value = true
  try {
    const res = await checkOut(activeRecord.value.id, checkoutForm)
    if (res.code === 200) {
      ElMessage.success('结课打卡成功，等待家长确认')
      loadData()
    }
  } catch (error) {
    ElMessage.error(error.message || '结课失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  if (map) {
    map.destroy()
  }
})
</script>

<style lang="scss" scoped>
.checkin-page {
  max-width: 600px;
  margin: 0 auto;
  
  .checkin-container {
    margin-top: 24px;
    padding-bottom: 40px;
  }
  
  .lesson-info-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    
    .lesson-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      h3 {
        font-size: 18px;
        font-weight: 600;
      }
    }
    
    .lesson-meta {
      p {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 14px;
        color: #606266;
        margin-bottom: 6px;
        
        .el-icon {
          color: #909399;
        }
      }
    }
  }
  
  .location-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    
    h4 {
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 12px;
    }
    
    .location-loading, .location-info, .location-error {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 8px;
      margin-bottom: 16px;
    }
    
    .location-loading {
      color: #409eff;
    }
    
    .location-info {
      color: #67c23a;
    }
    
    .location-error {
      color: #f56c6c;
    }
    
    .map-container {
      height: 200px;
      border-radius: 8px;
      overflow: hidden;
      background: #eee;
    }
  }

  .photo-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    
    h4 {
      font-size: 14px;
      font-weight: 600;
      margin-bottom: 12px;
    }
    
    .photo-placeholder {
      display: flex;
      align-items: center;
      padding: 10px;
      border: 1px dashed #dcdfe6;
      border-radius: 8px;
      
      img {
        width: 100px;
        height: auto;
        border-radius: 4px;
      }
    }
  }
  
  .checkin-action {
    margin-top: 24px;
    
    .el-button {
      width: 100%;
      height: 56px;
      font-size: 18px;
    }
  }
}
</style>
