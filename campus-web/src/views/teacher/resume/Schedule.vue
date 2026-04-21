<template>
  <div class="schedule-page">
    <el-page-header @back="goBack">
      <template #content>排课设置</template>
    </el-page-header>
    
    <div class="schedule-container">
      <div class="schedule-card">
        <p class="tip">点击格子选择您每周可授课的时间段，方便家长预约</p>
        
        <div class="schedule-grid">
          <!-- 表头 -->
          <div class="grid-header">
            <div class="time-label"></div>
            <div v-for="day in 7" :key="day" class="day-label">{{ getDayName(day) }}</div>
          </div>
          <!-- 时段行 -->
          <div v-for="(slot, si) in timeSlots" :key="si" class="grid-row">
            <div class="time-label">{{ slot }}</div>
            <div
              v-for="day in 7"
              :key="day"
              class="grid-cell"
              :class="{ active: isSelected(day, slot) }"
              @click="toggleSlot(day, slot)"
            />
          </div>
        </div>
        
        <div class="legend">
          <span class="legend-item"><span class="dot active"></span>可授课</span>
          <span class="legend-item"><span class="dot"></span>不可用</span>
        </div>
        
        <div class="actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存设置
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getScheduleConfig, saveScheduleConfig } from '@shared/api/tutor'

const router = useRouter()
const saving = ref(false)

const timeSlots = [
  '08:00-08:40', '08:50-09:30', '09:40-10:20', '10:30-11:10', '11:20-12:00',
  '14:00-14:40', '14:50-15:30', '15:40-16:20', '16:30-17:10',
  '18:30-19:10', '19:20-20:00', '20:10-20:50', '21:00-21:40'
]

// day -> Set of selected slot strings
const scheduleData = reactive({})
for (let d = 1; d <= 7; d++) scheduleData[d] = new Set()

const getDayName = (day) => {
  const names = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }
  return names[day]
}

const isSelected = (day, slot) => scheduleData[day].has(slot)

const toggleSlot = (day, slot) => {
  if (scheduleData[day].has(slot)) {
    scheduleData[day].delete(slot)
  } else {
    scheduleData[day].add(slot)
  }
}

const loadSchedule = async () => {
  try {
    const res = await getScheduleConfig()
    if (res.code === 200 && res.data) {
      res.data.forEach(item => {
        if (scheduleData[item.dayOfWeek]) {
          if (item.timeSlots) {
            // 兼容旧格式
            item.timeSlots.forEach(s => scheduleData[item.dayOfWeek].add(s))
          } else if (item.startTime && item.endTime) {
            // 后端返回的 startTime/endTime 格式，拼回 "HH:mm-HH:mm"
            scheduleData[item.dayOfWeek].add(`${item.startTime}-${item.endTime}`)
          }
        }
      })
    }
  } catch (error) {
    console.error('加载排课失败', error)
  }
}

const handleSave = async () => {
  const schedules = []
  for (let day = 1; day <= 7; day++) {
    if (scheduleData[day].size > 0) {
      // 将每个时段拆分为 startTime/endTime 格式
      for (const slot of scheduleData[day]) {
        const [startTime, endTime] = slot.split('-')
        schedules.push({
          dayOfWeek: day,
          startTime,
          endTime,
          available: 1
        })
      }
    }
  }
  
  saving.value = true
  try {
    const res = await saveScheduleConfig(schedules)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      router.back()
    }
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadSchedule()
})
</script>

<style lang="scss" scoped>
.schedule-page {
  max-width: 800px;
  margin: 0 auto;
  
  .schedule-container {
    margin-top: 24px;
  }
  
  .schedule-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    .tip {
      font-size: 14px;
      color: #909399;
      margin-bottom: 24px;
    }
  }
  
  .schedule-grid {
    overflow-x: auto;
    
    .grid-header, .grid-row {
      display: flex;
      align-items: center;
    }
    
    .grid-header {
      font-weight: 600;
      font-size: 13px;
      margin-bottom: 4px;
      
      .day-label {
        flex: 1;
        min-width: 52px;
        text-align: center;
      }
    }
    
    .time-label {
      width: 100px;
      min-width: 100px;
      font-size: 12px;
      color: #606266;
      text-align: right;
      padding-right: 8px;
    }
    
    .grid-row {
      margin-bottom: 3px;
    }
    
    .grid-cell {
      flex: 1;
      min-width: 52px;
      height: 28px;
      margin: 0 2px;
      border-radius: 4px;
      background: #f0f2f5;
      cursor: pointer;
      transition: background 0.15s;
      
      &:hover {
        background: #d9ecff;
      }
      
      &.active {
        background: #409eff;
      }
    }
  }
  
  .legend {
    display: flex;
    gap: 16px;
    margin-top: 16px;
    font-size: 13px;
    color: #606266;
    
    .legend-item {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    
    .dot {
      width: 12px;
      height: 12px;
      border-radius: 3px;
      background: #f0f2f5;
      
      &.active {
        background: #409eff;
      }
    }
  }
  
  .actions {
    margin-top: 32px;
    display: flex;
    justify-content: center;
    gap: 16px;
  }
}
</style>
