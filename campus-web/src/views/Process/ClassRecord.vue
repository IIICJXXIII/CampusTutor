<template>
  <div class="class-record-page">
    
    <div class="calendar-card">
      <el-calendar v-model="currentDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell" :class="{ 'is-selected': data.isSelected }">
            <span class="day-num">{{ data.day.split('-').slice(2).join('') }}</span>
            
            <div v-if="hasLegacy(data.day)" class="class-dot"></div>
          </div>
        </template>
      </el-calendar>
    </div>

    <div class="page-header">
      <div class="header-left">
        <h3>{{ selectedDateInfo }}</h3>
        <span class="sub-text" v-if="currentDayRecords.length > 0">
          当日共 {{ currentDayRecords.length }} 节课
        </span>
      </div>
      
      <el-button v-if="isTutor" type="primary" @click="openCheckInDialog" round>
        <el-icon style="margin-right:4px"><Position /></el-icon> 立即打卡
      </el-button>
    </div>

    <div class="record-list" v-loading="loading">
      <el-empty v-if="records.length === 0 && !loading" description="暂无课时记录" />
      
      <div v-for="item in records" :key="item.id" class="record-card" :class="{ 'highlight-card': isSameDay(item.startTime, currentDate) }">
        <div class="card-left">
          <div class="date-box">
            <span class="day">{{ formatDay(item.startTime) }}</span>
            <span class="month">{{ formatMonth(item.startTime) }}</span>
          </div>
          <div class="time-line">
            <div class="time-start">{{ formatTime(item.startTime) }}</div>
            <div class="duration-line"></div>
            <div class="time-end">{{ formatTime(item.endTime) }}</div>
          </div>
        </div>
        
        <div class="card-right">
          <div class="info-header">
            <span class="lesson-idx">第 {{ item.lessonIndex }} 课时</span>
            <el-tag :type="getStatusType(item.status)" size="small">
              {{ item.statusText }}
            </el-tag>
          </div>
          
          <div class="info-content">
            <p><strong>内容：</strong>{{ item.contentSummary || '无内容记录' }}</p>
            <p><strong>作业：</strong>{{ item.homeworkAssigned || '无作业' }}</p>
          </div>

          <div class="info-footer">
            <el-button 
              v-if="isParent && item.status === 0" 
              type="primary" 
              size="small" 
              @click="handleConfirm(item.id)"
            >
              确认课时
            </el-button>
            <el-button 
              v-if="isParent && item.status === 0" 
              type="danger" 
              plain
              size="small" 
              @click="handleDispute(item.id)"
            >
              申诉
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showCheckIn" title="上课打卡" width="90%" destroy-on-close>
      <el-form :model="form" label-width="70px">
        <el-form-item label="课程">
          <el-select v-model="form.orderId" placeholder="请选择当前课程" :disabled="!!preSelectedOrderId" style="width: 100%">
            <el-option 
              v-for="order in activeOrders" 
              :key="order.id" 
              :label="`${order.subject} - ${order.studentName || '学生'}`" 
              :value="order.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="位置">
          <div class="location-box">
            <div class="addr-text">
              <el-icon v-if="locationLoading" class="is-loading"><Loading /></el-icon>
              <span v-else>{{ form.address || '等待定位...' }}</span>
            </div>
            <el-button link type="primary" @click="getLocation" size="small">刷新</el-button>
          </div>
        </el-form-item>

        <el-form-item label="拍照">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :http-request="handleFileUpload"
          >
            <img v-if="form.photoUrl" :src="form.photoUrl" class="uploaded-img" />
            <el-icon v-else class="avatar-uploader-icon"><Camera /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="内容">
          <el-input v-model="form.contentSummary" type="textarea" placeholder="简述今日教学重点" />
        </el-form-item>

        <el-form-item label="作业">
          <el-input v-model="form.homeworkAssigned" placeholder="如有作业请填写" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCheckIn = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCheckIn">确认打卡</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'
import { checkIn, confirmLesson, getMyTeachingRecords } from '@/api/teaching'
import { getTutorOrders } from '@/api/order' 
import { uploadFile } from '@/api/file'
import { reverseGeocode } from '@/api/map'
import dayjs from 'dayjs'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const records = ref([])
const currentDate = ref(new Date())

const isTutor = computed(() => userStore.isTutor)
const isParent = computed(() => userStore.isParent)

// 显示选中日期的文本
const selectedDateInfo = computed(() => dayjs(currentDate.value).format('MM月DD日'))

// 计算当日有多少节课
const currentDayRecords = computed(() => {
  return records.value.filter(item => isSameDay(item.startTime, currentDate.value))
})

// === 核心：日历标记逻辑 ===
// 检查 data.day (格式 YYYY-MM-DD) 是否有课
const hasLegacy = (dayStr) => {
  return records.value.some(item => {
    if (!item.startTime) return false
    // 统一格式化为 YYYY-MM-DD 进行比对，确保准确
    return dayjs(item.startTime).format('YYYY-MM-DD') === dayStr
  })
}

// 检查两个日期是否是同一天
const isSameDay = (time1, time2) => {
  if (!time1 || !time2) return false
  return dayjs(time1).isSame(dayjs(time2), 'day')
}

// === 下面是打卡逻辑 (保持不变) ===
const showCheckIn = ref(false)
const submitting = ref(false)
const activeOrders = ref([])
const preSelectedOrderId = ref(null)
const locationLoading = ref(false)

const form = ref({
  orderId: null,
  latitude: 0,
  longitude: 0,
  address: '',
  photoUrl: '',
  contentSummary: '',
  homeworkAssigned: ''
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyTeachingRecords()
    records.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const openCheckInDialog = async () => {
  try {
    const res = await getTutorOrders({ page: 1, size: 50 })
    const allOrders = res.data?.records || []
    activeOrders.value = allOrders.filter(o => o.status === 1 || o.status === 2)
    
    if (activeOrders.value.length === 0) {
      ElMessage.warning('没有进行中的订单')
      return
    }
  } catch (e) {}

  showCheckIn.value = true
  if (preSelectedOrderId.value) form.value.orderId = Number(preSelectedOrderId.value)
  else if (activeOrders.value.length > 0) form.value.orderId = activeOrders.value[0].id 
  getLocation()
}

const getLocation = () => {
  locationLoading.value = true
  form.value.address = '定位中...'
  if (!navigator.geolocation) {
    locationLoading.value = false
    return
  }
  navigator.geolocation.getCurrentPosition(
    async (position) => {
      form.value.latitude = position.coords.latitude
      form.value.longitude = position.coords.longitude
      try {
        const res = await reverseGeocode(form.value.latitude, form.value.longitude)
        form.value.address = res.data?.result?.address || '位置已获取'
      } catch (e) {
        form.value.address = '位置已获取'
      }
      locationLoading.value = false
    },
    () => {
      form.value.latitude = 39.9042
      form.value.longitude = 116.4074
      form.value.address = '定位失败(默认坐标)'
      locationLoading.value = false
    }
  )
}

const handleFileUpload = async (options) => {
  try {
    const res = await uploadFile(options.file, 'clock-in')
    if (res.code === 200) form.value.photoUrl = res.data
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const submitCheckIn = async () => {
  if (!form.value.orderId) return ElMessage.warning('请选择订单')
  if (!form.value.photoUrl) return ElMessage.warning('请拍照')
  
  submitting.value = true
  try {
    await checkIn(form.value)
    ElMessage.success('打卡成功')
    showCheckIn.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || '打卡失败')
  } finally {
    submitting.value = false
  }
}

const handleConfirm = async (id) => {
  try { await confirmLesson(id); ElMessage.success('已确认'); fetchData(); } catch(e){}
}
const handleDispute = (id) => { ElMessage.info('开发中'); }

onMounted(() => {
  fetchData()
  if (route.query.orderId) preSelectedOrderId.value = route.query.orderId
  if (route.query.action === 'checkin' && isTutor.value) openCheckInDialog()
})

const formatDay = (t) => t ? dayjs(t).date() : ''
const formatMonth = (t) => t ? (dayjs(t).month() + 1) + '月' : ''
const formatTime = (t) => t ? dayjs(t).format('HH:mm') : ''
const getStatusType = (s) => ({0:'warning',1:'success'}[s] || 'info')
</script>

<style lang="scss" scoped>
.class-record-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 12px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 日历卡片 */
.calendar-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  margin-bottom: 16px;

  /* 强制调整 Element 日历样式以适应移动端/小屏 */
  :deep(.el-calendar__header) {
    padding: 12px 16px;
    border-bottom: 1px solid #f0f0f0;
    
    .el-calendar__title { font-size: 16px; font-weight: 600; }
    .el-button { font-size: 12px; }
  }
  
  :deep(.el-calendar__body) {
    padding: 8px;
  }
  
  :deep(.el-calendar-table .el-calendar-day) {
    height: 48px; /* 减小高度 */
    padding: 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }
}

.calendar-cell {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  
  .day-num {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    z-index: 1;
  }

  /* 标记点 */
  .class-dot {
    width: 6px;
    height: 6px;
    background-color: #409eff;
    border-radius: 50%;
    margin-top: 2px;
  }

  &.is-selected {
    background-color: #ecf5ff;
    border-radius: 4px;
    .day-num { color: #409eff; font-weight: bold; }
  }
}

/* 页面操作头 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 0 4px;

  .header-left {
    h3 { margin: 0; font-size: 18px; color: #303133; }
    .sub-text { font-size: 12px; color: #909399; margin-top: 2px; }
  }
}

/* 列表卡片 */
.record-card {
  display: flex;
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.03);
  border: 1px solid transparent;
  transition: all 0.3s;

  &.highlight-card {
    border-color: #409eff;
    background-color: #f0f9eb;
  }

  .card-left {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-right: 16px;
    border-right: 1px solid #eee;
    min-width: 70px;
    
    .day { font-size: 20px; font-weight: bold; color: #303133; }
    .month { font-size: 12px; color: #909399; }
    .time-line { margin-top: 8px; font-size: 12px; color: #606266; text-align: center; }
    .duration-line { height: 10px; width: 2px; background: #ddd; margin: 2px auto; }
  }

  .card-right {
    flex: 1;
    padding-left: 16px;
    
    .info-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      .lesson-idx { font-weight: 600; font-size: 15px; }
    }
    
    .info-content {
      font-size: 13px;
      color: #666;
      margin-bottom: 8px;
      p { margin: 2px 0; }
    }
    
    .info-footer {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      border-top: 1px dashed #eee;
      padding-top: 8px;
    }
  }
}

/* 弹窗样式 */
.location-box {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  .addr-text { flex: 1; font-size: 12px; color: #666; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-right: 8px;}
}
.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  width: 80px;
  height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  .uploaded-img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
  .avatar-uploader-icon { font-size: 24px; color: #8c939d; }
}
</style>