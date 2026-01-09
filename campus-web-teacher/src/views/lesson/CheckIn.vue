<template>
  <div class="checkin-page">
    <el-page-header @back="goBack">
      <template #content>上课签到</template>
    </el-page-header>
    
    <div class="checkin-container">
      <!-- 课程信息 -->
      <div class="lesson-info-card">
        <div class="lesson-header">
          <h3>{{ lesson.studentName }} - {{ lesson.subject }}</h3>
          <el-tag :type="lesson.status === 2 ? 'warning' : 'info'">
            {{ lesson.status === 2 ? '上课中' : '待签到' }}
          </el-tag>
        </div>
        <div class="lesson-meta">
          <p><el-icon><Clock /></el-icon>{{ lesson.startTime }} - {{ lesson.endTime }}</p>
          <p><el-icon><Location /></el-icon>{{ lesson.address || '线上授课' }}</p>
        </div>
      </div>
      
      <!-- 定位卡片 -->
      <div class="location-card">
        <h4>当前位置</h4>
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
        
        <!-- 地图 -->
        <div id="checkin-map" class="map-container"></div>
      </div>
      
      <!-- 签到按钮 -->
      <div class="checkin-action">
        <el-button
          v-if="lesson.status !== 2"
          type="primary"
          size="large"
          :loading="submitting"
          :disabled="!currentLocation"
          @click="handleCheckIn"
        >
          <el-icon><Check /></el-icon>
          签到开始上课
        </el-button>
        
        <el-button
          v-else
          type="success"
          size="large"
          :loading="submitting"
          @click="handleCheckOut"
        >
          <el-icon><Check /></el-icon>
          签退结束上课
        </el-button>
      </div>
      
      <!-- 提示 -->
      <div class="tips">
        <h4>签到须知</h4>
        <ul>
          <li>请在上课地点附近进行签到</li>
          <li>签到后系统开始计算课时</li>
          <li>课程结束后请及时签退</li>
          <li>如遇问题请联系客服</li>
        </ul>
      </div>
    </div>
    
    <!-- 签退弹窗 -->
    <el-dialog v-model="checkoutVisible" title="结束上课" width="90%" max-width="500px">
      <el-form :model="checkoutForm" label-width="80px">
        <el-form-item label="课程小结">
          <el-input
            v-model="checkoutForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入本次课程的教学内容"
          />
        </el-form-item>
        <el-form-item label="学生表现">
          <el-rate v-model="checkoutForm.rating" show-text :texts="['很差', '较差', '一般', '良好', '优秀']" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkoutVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCheckout">确认签退</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Location, Check, Loading, WarningFilled } from '@element-plus/icons-vue'
import { getLessonDetail, checkIn, checkOut } from '@shared/api/teaching'
import AMapLoader from '@amap/amap-jsapi-loader'

const route = useRoute()
const router = useRouter()

const lesson = ref({})
const locationLoading = ref(true)
const locationError = ref('')
const currentLocation = ref(null)
const submitting = ref(false)
const checkoutVisible = ref(false)

let map = null
let marker = null

const checkoutForm = reactive({
  content: '',
  rating: 5
})

const loadLesson = async () => {
  const lessonId = route.query.lessonId || route.query.orderId
  if (!lessonId) {
    ElMessage.warning('缺少课程信息')
    return
  }
  
  try {
    const res = await getLessonDetail(lessonId)
    if (res.code === 200) {
      lesson.value = res.data
    }
  } catch (error) {
    console.error('加载课程失败', error)
  }
}

const initMap = async () => {
  try {
    const AMap = await AMapLoader.load({
      key: import.meta.env.VITE_AMAP_KEY || 'your-amap-key',
      version: '2.0',
      plugins: ['AMap.Geolocation']
    })
    
    map = new AMap.Map('checkin-map', {
      zoom: 15,
      center: [116.397428, 39.90923]
    })
    
    getLocation()
  } catch (error) {
    console.error('地图加载失败', error)
    locationError.value = '地图加载失败'
    locationLoading.value = false
  }
}

const getLocation = () => {
  locationLoading.value = true
  locationError.value = ''
  
  if (!navigator.geolocation) {
    locationError.value = '浏览器不支持定位'
    locationLoading.value = false
    return
  }
  
  navigator.geolocation.getCurrentPosition(
    async (position) => {
      const { longitude, latitude } = position.coords
      
      currentLocation.value = {
        lng: longitude,
        lat: latitude,
        address: '定位成功'
      }
      
      // 更新地图
      if (map) {
        map.setCenter([longitude, latitude])
        
        if (marker) {
          marker.setPosition([longitude, latitude])
        } else {
          marker = new window.AMap.Marker({
            position: [longitude, latitude],
            title: '当前位置'
          })
          map.add(marker)
        }
        
        // 逆地理编码获取地址
        const geocoder = new window.AMap.Geocoder()
        geocoder.getAddress([longitude, latitude], (status, result) => {
          if (status === 'complete' && result.regeocode) {
            currentLocation.value.address = result.regeocode.formattedAddress
          }
        })
      }
      
      locationLoading.value = false
    },
    (error) => {
      console.error('定位失败', error)
      locationError.value = '定位失败，请检查权限'
      locationLoading.value = false
    },
    {
      enableHighAccuracy: true,
      timeout: 10000
    }
  )
}

const handleCheckIn = async () => {
  submitting.value = true
  try {
    const res = await checkIn(lesson.value.id, {
      location: currentLocation.value?.address,
      longitude: currentLocation.value?.lng,
      latitude: currentLocation.value?.lat
    })
    
    if (res.code === 200) {
      ElMessage.success('签到成功，开始上课')
      lesson.value.status = 2
    }
  } catch (error) {
    ElMessage.error(error.message || '签到失败')
  } finally {
    submitting.value = false
  }
}

const handleCheckOut = () => {
  checkoutVisible.value = true
}

const submitCheckout = async () => {
  submitting.value = true
  try {
    const res = await checkOut(lesson.value.id, {
      content: checkoutForm.content,
      rating: checkoutForm.rating
    })
    
    if (res.code === 200) {
      ElMessage.success('签退成功')
      checkoutVisible.value = false
      router.push('/lessons')
    }
  } catch (error) {
    ElMessage.error(error.message || '签退失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadLesson()
  initMap()
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
    }
  }
  
  .checkin-action {
    margin-bottom: 24px;
    
    .el-button {
      width: 100%;
      height: 56px;
      font-size: 18px;
    }
  }
  
  .tips {
    background: #fff9e6;
    border-radius: 12px;
    padding: 16px 20px;
    
    h4 {
      font-size: 14px;
      font-weight: 600;
      color: #e6a23c;
      margin-bottom: 12px;
    }
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        font-size: 13px;
        color: #606266;
        margin-bottom: 6px;
      }
    }
  }
}
</style>
