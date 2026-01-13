<template>
  <div class="class-record-page">
    <div class="page-wrapper">
      <!-- 页面标题 -->
      <div class="page-title-bar">
        <div class="title-left">
          <h1>课时记录</h1>
          <p class="subtitle">查看您的授课/上课历史</p>
        </div>
        <el-button v-if="isTutor" type="primary" @click="openCheckInDialog" class="checkin-btn">
          <el-icon><Position /></el-icon>
          立即打卡
        </el-button>
      </div>

      <el-row :gutter="24">
        <!-- 左侧日历面板 -->
        <el-col :span="10" class="calendar-col">
          <el-card class="calendar-card" shadow="hover">
            <template #header>
              <div class="calendar-header">
                <el-icon><Calendar /></el-icon>
                <span>课程日历</span>
              </div>
            </template>
            <el-calendar v-model="currentDate">
              <template #date-cell="{ data }">
                <div class="calendar-cell" :class="{ 'is-selected': data.isSelected, 'has-class': hasLegacy(data.day) }">
                  <span class="day-num">{{ data.day.split('-').slice(2).join('') }}</span>
                  <div v-if="hasLegacy(data.day)" class="class-dot"></div>
                </div>
              </template>
            </el-calendar>

            <!-- 日历图例 -->
            <div class="calendar-legend">
              <div class="legend-item">
                <span class="dot active"></span>
                <span>有课程</span>
              </div>
              <div class="legend-item">
                <span class="dot selected"></span>
                <span>已选日期</span>
              </div>
            </div>
          </el-card>

          <!-- 统计卡片 -->
          <el-card class="stats-card" shadow="hover">
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-value">{{ records.length }}</div>
                <div class="stat-label">总课时</div>
              </div>
              <div class="stat-item">
                <div class="stat-value success">{{ records.filter(r => r.status === 1).length }}</div>
                <div class="stat-label">已确认</div>
              </div>
              <div class="stat-item">
                <div class="stat-value warning">{{ records.filter(r => r.status === 0).length }}</div>
                <div class="stat-label">待确认</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 右侧记录列表 -->
        <el-col :span="14">
          <el-card class="records-card" shadow="hover">
            <template #header>
              <div class="records-header">
                <div class="header-title">
                  <el-icon><Clock /></el-icon>
                  <span>{{ selectedDateInfo }} 课时记录</span>
                  <el-tag v-if="currentDayRecords.length > 0" type="info" size="small">
                    {{ currentDayRecords.length }} 节课
                  </el-tag>
                </div>
              </div>
            </template>

            <div class="record-list" v-loading="loading">
              <el-empty v-if="records.length === 0 && !loading" description="暂无课时记录" />
              
              <div v-for="item in records" :key="item.id" class="record-card" :class="{ 'highlight-card': isSameDay(item.startTime, currentDate) }">
                <div class="card-timeline">
                  <div class="date-badge">
                    <span class="day">{{ formatDay(item.startTime) }}</span>
                    <span class="month">{{ formatMonth(item.startTime) }}</span>
                  </div>
                  <div class="time-info">
                    <span class="time-start">{{ formatTime(item.startTime) }}</span>
                    <div class="time-connector">
                      <div class="connector-line"></div>
                      <el-icon><ArrowDown /></el-icon>
                    </div>
                    <span class="time-end">{{ formatTime(item.endTime) }}</span>
                  </div>
                </div>
                
                <div class="card-content">
                  <div class="content-header">
                    <span class="lesson-idx">第 {{ item.lessonIndex }} 课时</span>
                    <el-tag :type="getStatusType(item.status)" effect="light" round>
                      {{ item.statusText }}
                    </el-tag>
                  </div>
                  
                  <div class="content-body">
                    <div class="info-row">
                      <el-icon><Document /></el-icon>
                      <span><strong>教学内容：</strong>{{ item.contentSummary || '无内容记录' }}</span>
                    </div>
                    <div class="info-row">
                      <el-icon><EditPen /></el-icon>
                      <span><strong>布置作业：</strong>{{ item.homeworkAssigned || '无作业' }}</span>
                    </div>
                  </div>

                  <div class="content-footer" v-if="isParent && item.status === 0">
                    <el-button type="primary" size="small" @click="handleConfirm(item.id)">
                      <el-icon><Check /></el-icon> 确认课时
                    </el-button>
                    <el-button type="danger" plain size="small" @click="handleDispute(item.id)">
                      <el-icon><Warning /></el-icon> 申诉
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 打卡弹窗 -->
    <el-dialog v-model="showCheckIn" title="上课打卡" width="500px" destroy-on-close class="checkin-dialog">
      <el-form :model="form" label-width="80px" label-position="top">
        <el-form-item label="选择课程">
          <el-select v-model="form.orderId" placeholder="请选择当前课程" :disabled="!!preSelectedOrderId" style="width: 100%">
            <el-option 
              v-for="order in activeOrders" 
              :key="order.id" 
              :label="`${order.subject} - ${order.studentName || '学生'}`" 
              :value="order.id" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="当前位置">
          <div class="location-box">
            <div class="addr-text">
              <el-icon v-if="locationLoading" class="is-loading"><Loading /></el-icon>
              <el-icon v-else><Location /></el-icon>
              <span>{{ form.address || '等待定位...' }}</span>
            </div>
            <el-button link type="primary" @click="getLocation">刷新定位</el-button>
          </div>
        </el-form-item>

        <el-form-item label="拍照打卡">
          <el-upload
            class="photo-uploader"
            action="#"
            :show-file-list="false"
            :http-request="handleFileUpload"
          >
            <div v-if="form.photoUrl" class="photo-preview">
              <img :src="form.photoUrl" />
              <div class="photo-overlay">
                <el-icon><RefreshRight /></el-icon>
                <span>重新拍照</span>
              </div>
            </div>
            <div v-else class="photo-placeholder">
              <el-icon :size="32"><Camera /></el-icon>
              <span>点击拍照</span>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="教学内容">
          <el-input v-model="form.contentSummary" type="textarea" :rows="3" placeholder="简述今日教学重点..." />
        </el-form-item>

        <el-form-item label="布置作业">
          <el-input v-model="form.homeworkAssigned" placeholder="如有作业请填写" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCheckIn = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCheckIn">
          <el-icon><Check /></el-icon> 确认打卡
        </el-button>
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
  min-height: calc(100vh - 114px);
  background: linear-gradient(180deg, #f0f4ff 0%, #f5f7fa 100%);
  padding: 32px 0;
}

.page-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 32px;
}

/* 页面标题栏 */
.page-title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  background: #fff;
  padding: 24px 32px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);

  .title-left {
    h1 {
      font-size: 26px;
      font-weight: 800;
      color: #1a1a2e;
      margin: 0 0 6px 0;
    }

    .subtitle {
      font-size: 14px;
      color: #909399;
      margin: 0;
    }
  }

  .checkin-btn {
    height: 48px;
    padding: 0 28px;
    font-size: 15px;
    font-weight: 600;
    border-radius: 24px;
    background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
    border: none;
    box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);

    .el-icon {
      margin-right: 8px;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 12px 32px rgba(64, 158, 255, 0.4);
    }
  }
}

/* 日历面板 */
.calendar-card {
  border-radius: 16px;
  border: none;
  margin-bottom: 20px;
  overflow: hidden;

  .calendar-header {
    display: flex;
    align-items: center;
    gap: 10px;
    font-weight: 700;
    font-size: 16px;
    color: #303133;

    .el-icon {
      color: #409eff;
      font-size: 18px;
    }
  }

  :deep(.el-calendar__header) {
    padding: 12px 16px;
    border-bottom: 1px solid #f0f2f5;
    
    .el-calendar__title { 
      font-size: 15px; 
      font-weight: 600;
      color: #303133;
    }
    .el-button { 
      font-size: 12px;
    }
  }
  
  :deep(.el-calendar__body) {
    padding: 12px;
  }
  
  :deep(.el-calendar-table .el-calendar-day) {
    height: 44px;
    padding: 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  :deep(.el-calendar-table thead th) {
    font-size: 12px;
    color: #909399;
    font-weight: 500;
  }
}

.calendar-cell {
  width: 36px;
  height: 36px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  border-radius: 8px;
  transition: all 0.2s;
  cursor: pointer;
  
  .day-num {
    font-size: 13px;
    font-weight: 500;
    color: #303133;
    z-index: 1;
  }

  .class-dot {
    width: 5px;
    height: 5px;
    background: linear-gradient(135deg, #409eff, #667eea);
    border-radius: 50%;
    position: absolute;
    bottom: 3px;
  }

  &.has-class {
    background: rgba(64, 158, 255, 0.08);
  }

  &.is-selected {
    background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
    .day-num { 
      color: #fff; 
      font-weight: 700; 
    }
    .class-dot {
      background: #fff;
    }
  }

  &:hover:not(.is-selected) {
    background: rgba(64, 158, 255, 0.15);
  }
}

.calendar-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid #f0f2f5;
  margin-top: 8px;

  .legend-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: #909399;

    .dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;

      &.active {
        background: linear-gradient(135deg, #409eff, #667eea);
      }

      &.selected {
        background: #67c23a;
      }
    }
  }
}

/* 统计卡片 */
.stats-card {
  border-radius: 16px;
  border: none;

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    text-align: center;
    gap: 8px;

    .stat-item {
      padding: 16px 8px;
      background: #f8fafc;
      border-radius: 12px;

      .stat-value {
        font-size: 28px;
        font-weight: 800;
        color: #303133;
        font-family: 'SF Mono', monospace;

        &.success { color: #67c23a; }
        &.warning { color: #e6a23c; }
      }

      .stat-label {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

/* 记录卡片区域 */
.records-card {
  border-radius: 16px;
  border: none;
  min-height: 500px;

  .records-header {
    .header-title {
      display: flex;
      align-items: center;
      gap: 10px;
      font-weight: 700;
      font-size: 16px;
      color: #303133;

      .el-icon {
        color: #409eff;
        font-size: 18px;
      }
    }
  }
}

.record-list {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 8px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 3px;
  }
}

.record-card {
  display: flex;
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  margin-bottom: 16px;
  border: 2px solid #f0f2f5;
  transition: all 0.3s;

  &:hover {
    border-color: #e0e6ff;
    box-shadow: 0 8px 24px rgba(64, 158, 255, 0.08);
    transform: translateY(-2px);
  }

  &.highlight-card {
    border-color: #409eff;
    background: linear-gradient(135deg, #f0f7ff 0%, #fff 100%);
  }
}

.card-timeline {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-right: 24px;
  border-right: 2px solid #f0f2f5;
  min-width: 80px;

  .date-badge {
    background: linear-gradient(135deg, #409eff 0%, #667eea 100%);
    color: #fff;
    border-radius: 12px;
    padding: 12px 16px;
    text-align: center;
    margin-bottom: 12px;

    .day {
      font-size: 24px;
      font-weight: 800;
      display: block;
    }

    .month {
      font-size: 11px;
      opacity: 0.9;
    }
  }

  .time-info {
    text-align: center;
    font-size: 12px;
    color: #606266;

    .time-start, .time-end {
      font-weight: 600;
      font-family: 'SF Mono', monospace;
    }

    .time-connector {
      padding: 4px 0;
      color: #c0c4cc;

      .connector-line {
        width: 2px;
        height: 8px;
        background: #e4e7ed;
        margin: 0 auto 2px;
      }
    }
  }
}

.card-content {
  flex: 1;
  padding-left: 24px;

  .content-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;

    .lesson-idx {
      font-weight: 700;
      font-size: 16px;
      color: #303133;
    }
  }

  .content-body {
    margin-bottom: 16px;

    .info-row {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      font-size: 13px;
      color: #606266;
      margin-bottom: 8px;
      line-height: 1.6;

      .el-icon {
        color: #909399;
        margin-top: 3px;
      }

      strong {
        color: #303133;
      }
    }
  }

  .content-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding-top: 12px;
    border-top: 1px dashed #e4e7ed;
  }
}

/* 打卡弹窗 */
.location-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #f8fafc 0%, #f5f7fa 100%);
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid #e4e7ed;

  .addr-text {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    color: #606266;

    .el-icon {
      color: #409eff;
    }
  }
}

.photo-uploader {
  :deep(.el-upload) {
    width: 140px;
    height: 140px;
    border: 2px dashed #dcdfe6;
    border-radius: 12px;
    cursor: pointer;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.photo-preview {
  width: 100%;
  height: 100%;
  position: relative;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .photo-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #fff;
    opacity: 0;
    transition: opacity 0.3s;
    gap: 4px;

    .el-icon {
      font-size: 24px;
    }

    span {
      font-size: 12px;
    }
  }

  &:hover .photo-overlay {
    opacity: 1;
  }
}

.photo-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  gap: 8px;

  span {
    font-size: 12px;
  }
}

/* 响应式 */
@media (max-width: 1024px) {
  .page-wrapper {
    padding: 0 20px;
  }

  .calendar-col {
    display: none;
  }

  :deep(.el-col-14) {
    max-width: 100% !important;
    flex: 0 0 100% !important;
  }
}

@media (max-width: 768px) {
  .class-record-page {
    padding: 16px 0;
  }

  .page-wrapper {
    padding: 0 16px;
  }

  .page-title-bar {
    flex-direction: column;
    gap: 16px;
    text-align: center;
    padding: 20px;

    .checkin-btn {
      width: 100%;
    }
  }

  .record-card {
    flex-direction: column;
  }

  .card-timeline {
    flex-direction: row;
    border-right: none;
    border-bottom: 2px solid #f0f2f5;
    padding-right: 0;
    padding-bottom: 16px;
    margin-bottom: 16px;
    justify-content: space-between;
  }

  .card-content {
    padding-left: 0;
  }
}
</style>