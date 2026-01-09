<template>
  <div class="schedule-page">
    <el-page-header @back="goBack">
      <template #content>排课设置</template>
    </el-page-header>
    
    <div class="schedule-container">
      <div class="schedule-card">
        <p class="tip">设置您每周可授课的时间段，方便家长预约</p>
        
        <div class="schedule-grid">
          <div v-for="day in 7" :key="day" class="day-row">
            <div class="day-header">
              <el-checkbox 
                v-model="scheduleData[day].enabled"
                @change="toggleDay(day)"
              >
                {{ getDayName(day) }}
              </el-checkbox>
            </div>
            
            <div v-if="scheduleData[day].enabled" class="time-slots">
              <el-checkbox-group v-model="scheduleData[day].slots">
                <el-checkbox label="上午" />
                <el-checkbox label="下午" />
                <el-checkbox label="晚上" />
              </el-checkbox-group>
            </div>
            <div v-else class="time-slots disabled">
              休息
            </div>
          </div>
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

// 初始化7天的排课数据
const scheduleData = reactive({
  1: { enabled: false, slots: [] },
  2: { enabled: false, slots: [] },
  3: { enabled: false, slots: [] },
  4: { enabled: false, slots: [] },
  5: { enabled: false, slots: [] },
  6: { enabled: false, slots: [] },
  7: { enabled: false, slots: [] }
})

const getDayName = (day) => {
  const names = { 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日' }
  return names[day]
}

const toggleDay = (day) => {
  if (!scheduleData[day].enabled) {
    scheduleData[day].slots = []
  } else {
    scheduleData[day].slots = ['上午', '下午', '晚上']
  }
}

const loadSchedule = async () => {
  try {
    const res = await getScheduleConfig()
    if (res.code === 200 && res.data) {
      res.data.forEach(item => {
        if (scheduleData[item.dayOfWeek]) {
          scheduleData[item.dayOfWeek].enabled = true
          scheduleData[item.dayOfWeek].slots = item.timeSlots || []
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
    if (scheduleData[day].enabled && scheduleData[day].slots.length > 0) {
      schedules.push({
        dayOfWeek: day,
        timeSlots: scheduleData[day].slots
      })
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
  max-width: 600px;
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
    .day-row {
      display: flex;
      align-items: center;
      padding: 16px 0;
      border-bottom: 1px solid #ebeef5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .day-header {
        width: 100px;
      }
      
      .time-slots {
        flex: 1;
        
        &.disabled {
          color: #c0c4cc;
          font-size: 14px;
        }
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
